package com.bc.fiduceo.reader.avhrr_frac;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.snap.SNAP_PixelLocator;
import com.bc.fiduceo.reader.snap.VariableProxy;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.s3tbx.dataio.avhrr.AvhrrConstants;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.*;
import ucar.ma2.*;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ucar.ma2.DataType.*;

public class AVHRR_FRAC_Reader implements Reader {

    private static final String REG_EX = "NSS.FRAC.M2.D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.SV";
    private static final Interval INTERVAL = new Interval(5, 20);
    private static final int NUM_SPLITS = 2;

    private final GeometryFactory geometryFactory;

    private Product product;
    private PixelLocator pixelLocator;

    AVHRR_FRAC_Reader(ReaderContext readerContext) {
        geometryFactory = readerContext.getGeometryFactory();
    }

    @Override
    public void open(File file) throws IOException {
        product = ProductIO.readProduct(file, AvhrrConstants.PRODUCT_TYPE);
        if (product == null) {
            throw new IOException("Unable to read AVHRR_FRAC product: " + file.getAbsolutePath());
        }
    }

    @Override
    public void close() throws IOException {
        pixelLocator = null;
        if (product != null) {
            product.dispose();
            product = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        setSensingTimes(acquisitionInfo);

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        final Geometries geometries = calculateGeometries();
        acquisitionInfo.setBoundingGeometry(geometries.getBoundingGeometry());
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public PixelLocator getPixelLocator() {
        if (pixelLocator == null) {
            final GeoCoding geoCoding = product.getSceneGeoCoding();

            pixelLocator = new SNAP_PixelLocator(geoCoding);
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) {
        return getPixelLocator();   // SNAP does not support region-specific geolocations tb 2019-01-17
    }

    @Override
    public TimeLocator getTimeLocator() {
        final ProductData.UTC startTime = product.getStartTime();
        final ProductData.UTC endTime = product.getEndTime();
        return new AVHRR_FRAC_TimeLocator(startTime.getAsDate(), endTime.getAsDate(), product.getSceneRasterHeight());
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        if (product.containsTiePointGrid(variableName)) {
            // we do not want raw data access on tie-point grids tb 2016-08-11
            return readScaled(centerX, centerY, interval, variableName);
        }

        final RasterDataNode dataNode = getRasterDataNode(variableName);
        if (dataNode instanceof VirtualBand) {
            // we can not access raw data of virtual bands tb 2019-01-17
            return readScaled(centerX, centerY, interval, variableName);
        }

        final double noDataValue = getNoDataValue(dataNode);
        final DataType targetDataType = NetCDFUtils.getNetcdfDataType(dataNode.getDataType());
        final int[] shape = getShape(interval);
        final Array readArray = Array.factory(targetDataType, shape);
        final Array targetArray = Array.factory(targetDataType, shape);

        final int width = interval.getX();
        final int height = interval.getY();

        final int xOffset = centerX - width / 2;
        final int yOffset = centerY - height / 2;

        readRawProductData(dataNode, readArray, width, height, xOffset, yOffset);

        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();

        final Index index = targetArray.getIndex();
        int readIndex = 0;
        for (int y = 0; y < width; y++) {
            final int currentY = yOffset + y;
            for (int x = 0; x < height; x++) {
                final int currentX = xOffset + x;
                index.set(y, x);
                if (currentX >= 0 && currentX < sceneRasterWidth && currentY >= 0 && currentY < sceneRasterHeight) {
                    targetArray.setObject(index, readArray.getObject(readIndex));
                    ++readIndex;
                } else {
                    targetArray.setObject(index, noDataValue);
                }
            }
        }

        return targetArray;
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException {
        final RasterDataNode dataNode = getRasterDataNode(variableName);

        final DataType sourceDataType = NetCDFUtils.getNetcdfDataType(dataNode.getGeophysicalDataType());
        final int[] shape = getShape(interval);
        final Array readArray = createReadingArray(sourceDataType, shape);

        final int width = interval.getX();
        final int height = interval.getY();

        final int xOffset = centerX - width / 2;
        final int yOffset = centerY - height / 2;

        readProductData(dataNode, readArray, width, height, xOffset, yOffset);

        return readArray;
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) {
        final int width = interval.getX();
        final int height = interval.getY();
        final int[] timeArray = new int[width * height];

        final int sceneRasterHeight = product.getSceneRasterHeight();
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int halfHeight = height / 2;
        final int halfWidth = width / 2;
        int writeOffset = 0;
        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
        final TimeLocator timeLocator = getTimeLocator();

        for (int yRead = y - halfHeight; yRead <= y + halfHeight; yRead++) {
            int lineTimeSeconds = fillValue;
            if (yRead >= 0 && yRead < sceneRasterHeight) {
                final long lineTimeMillis = timeLocator.getTimeFor(x, yRead);
                lineTimeSeconds = (int) Math.round(lineTimeMillis * 0.001);
            }

            for (int xRead = x - halfWidth; xRead <= x + halfWidth; xRead++) {
                if (xRead >= 0 && xRead < sceneRasterWidth) {
                    timeArray[writeOffset] = lineTimeSeconds;
                } else {
                    timeArray[writeOffset] = fillValue;
                }
                ++writeOffset;
            }
        }

        final int[] shape = getShape(interval);
        return (ArrayInt.D2) Array.factory(INT, shape, timeArray);
    }

    @Override
    public List<Variable> getVariables() {
        final List<Variable> result = new ArrayList<>();

        final Band[] bands = product.getBands();
        for (final Band band : bands) {
            final VariableProxy variableProxy = new VariableProxy(band);
            result.add(variableProxy);
        }

        final TiePointGrid[] tiePointGrids = product.getTiePointGrids();
        for (final TiePointGrid tiePointGrid : tiePointGrids) {
            final VariableProxy variableProxy = new VariableProxy(tiePointGrid);
            result.add(variableProxy);
        }

        return result;
    }

    @Override
    public Dimension getProductSize() {
        final int width = product.getSceneRasterWidth();
        final int height = product.getSceneRasterHeight();

        return new Dimension("product_size", width, height);
    }

    @Override
    public String getLongitudeVariableName() {
        return "longitude";
    }

    @Override
    public String getLatitudeVariableName() {
        return "latitude";
    }

    private Geometries calculateGeometries() throws IOException {
        final Geometries geometries = new Geometries();

        final TiePointGrid longitude = product.getTiePointGrid("longitude");
        final TiePointGrid latitude = product.getTiePointGrid("latitude");

        final int[] shape = new int[2];
        shape[0] = longitude.getGridHeight();
        shape[1] = longitude.getGridWidth();

        final DataType netcdfDataType = NetCDFUtils.getNetcdfDataType(longitude.getDataType());
        if (netcdfDataType == null) {
            throw new IOException("Unsupported data type: " + longitude.getDataType());
        }

        final ProductData longitudeGridData = longitude.getGridData();
        final ProductData latitudeGridData = latitude.getGridData();
        final Array lonArray = Array.factory(netcdfDataType, shape, longitudeGridData.getElems());
        final Array latArray = Array.factory(netcdfDataType, shape, latitudeGridData.getElems());

        Geometry timeAxisGeometry;
        final BoundingPolygonCreator boundingPolygonCreator = new BoundingPolygonCreator(INTERVAL, geometryFactory);
        Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(lonArray, latArray);
        if (!boundingGeometry.isValid()) {
            boundingGeometry = boundingPolygonCreator.createBoundingGeometrySplitted(lonArray, latArray, NUM_SPLITS, false);
            if (!boundingGeometry.isValid()) {
                throw new RuntimeException("Invalid bounding geometry detected");
            }
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometrySplitted(lonArray, latArray, NUM_SPLITS);
        } else {
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(lonArray, latArray);
        }

        geometries.setBoundingGeometry(boundingGeometry);
        geometries.setTimeAxesGeometry(timeAxisGeometry);

        return geometries;
    }

    private void setSensingTimes(AcquisitionInfo acquisitionInfo) {
        final ProductData.UTC startTime = product.getStartTime();
        acquisitionInfo.setSensingStart(startTime.getAsDate());

        final ProductData.UTC endTime = product.getEndTime();
        acquisitionInfo.setSensingStop(endTime.getAsDate());
    }

    // package access for testing only tb 2016-08-10
    static int[] getShape(Interval interval) {
        final int[] shape = new int[2];
        shape[0] = interval.getY();
        shape[1] = interval.getX();

        return shape;
    }

    private RasterDataNode getRasterDataNode(String variableName) {
        final RasterDataNode dataNode;
        if (product.containsBand(variableName)) {
            dataNode = product.getBand(variableName);
        } else if (product.containsTiePointGrid(variableName)) {
            dataNode = product.getTiePointGrid(variableName);
        } else {
            dataNode = product.getMaskGroup().get(variableName);
        }
        if (dataNode == null) {
            throw new RuntimeException("Requested variable not contained in product: " + variableName);
        }
        return dataNode;
    }

    // package access for testing only tb 2016-08-11
    static Array createReadingArray(DataType targetDataType, int[] shape) {
        switch (targetDataType) {
            case FLOAT:
                return Array.factory(DataType.FLOAT, shape);
            case INT:
                return Array.factory(DataType.INT, shape);
            case SHORT:
                return Array.factory(DataType.INT, shape);
            case BYTE:
                return Array.factory(DataType.BYTE, shape);
            default:
                throw new RuntimeException("unsupported data type: " + targetDataType);
        }
    }

    private void readProductData(RasterDataNode dataNode, Array targetArray, int width, int height, int xOffset, int yOffset) throws IOException {
        final Rectangle subsetRectangle = new Rectangle(xOffset, yOffset, width, height);
        final Rectangle productRectangle = new Rectangle(0, 0, product.getSceneRasterWidth(), product.getSceneRasterHeight());
        final Rectangle intersection = productRectangle.intersection(subsetRectangle);

        final DataType dataType = targetArray.getDataType();
        final Array readingArray = createReadingArray(dataType, new int[]{intersection.width, intersection.height});

        if (dataType == FLOAT) {
            dataNode.readPixels(intersection.x, intersection.y, intersection.width, intersection.height, (float[]) readingArray.getStorage());
        } else if (dataType == INT || dataType == SHORT) {
            dataNode.readPixels(intersection.x, intersection.y, intersection.width, intersection.height, (int[]) readingArray.getStorage());
        }

        final double noDataValue = getGeophysicalNoDataValue(dataNode);
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();
        final Index index = targetArray.getIndex();
        int readIndex = 0;
        for (int y = 0; y < width; y++) {
            final int currentY = yOffset + y;
            for (int x = 0; x < height; x++) {
                final int currentX = xOffset + x;
                index.set(y, x);
                if (currentX >= 0 && currentX < sceneRasterWidth && currentY >= 0 && currentY < sceneRasterHeight) {
                    targetArray.setObject(index, readingArray.getObject(readIndex));
                    ++readIndex;
                } else {
                    targetArray.setObject(index, noDataValue);
                }
            }
        }
    }

    // package access for testing only tb 2016-09-12
    static double getGeophysicalNoDataValue(RasterDataNode dataNode) {
        if (dataNode.isNoDataValueUsed()) {
            return dataNode.getGeophysicalNoDataValue();
        } else {
            final int dataType = dataNode.getDataType();
            return ReaderUtils.getDefaultFillValue(dataType).doubleValue();
        }
    }

    // package access for testing only tb 2016-09-12
    static double getNoDataValue(RasterDataNode dataNode) {
        if (dataNode.isNoDataValueUsed()) {
            return dataNode.getNoDataValue();
        } else {
            final int dataType = dataNode.getDataType();
            return ReaderUtils.getDefaultFillValue(dataType).doubleValue();
        }
    }

    private void readRawProductData(RasterDataNode dataNode, Array readArray, int width, int height, int xOffset, int yOffset) throws IOException {
        final DataType dataType = readArray.getDataType();

        final Rectangle subsetRectangle = new Rectangle(xOffset, yOffset, width, height);
        final Rectangle productRectangle = new Rectangle(0, 0, product.getSceneRasterWidth(), product.getSceneRasterHeight());
        final Rectangle intersection = productRectangle.intersection(subsetRectangle);

        final int rasterSize = intersection.width * intersection.height;
        if (rasterSize < 1) {
            return; // no intersecting area with product raster tb 2019-01-17
        }

        final ProductData productData = createProductData(dataType, rasterSize);

        dataNode.readRasterData(intersection.x, intersection.y, intersection.width, intersection.height, productData);
        for (int i = 0; i < rasterSize; i++) {
            readArray.setObject(i, productData.getElemDoubleAt(i));
        }
    }

    private ProductData createProductData(DataType dataType, int rasterSize) {
        final ProductData productData;
        if (dataType == FLOAT) {
            productData = ProductData.createInstance(ProductData.TYPE_FLOAT32, rasterSize);
        } else if (dataType == INT) {
            productData = ProductData.createInstance(ProductData.TYPE_INT32, rasterSize);
        } else if (dataType == SHORT) {
            productData = ProductData.createInstance(ProductData.TYPE_INT16, rasterSize);
        } else if (dataType == BYTE) {
            productData = ProductData.createInstance(ProductData.TYPE_INT8, rasterSize);
        } else {
            throw new RuntimeException("Data type not supported");
        }
        return productData;
    }
}
