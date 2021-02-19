package com.bc.fiduceo.reader.snap;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.gpf.GPF;
import ucar.ma2.*;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ucar.ma2.DataType.*;

public abstract class SNAP_Reader implements Reader {

    protected final GeometryFactory geometryFactory;

    private Product uncachedProduct;
    protected Product product;
    protected PixelLocator pixelLocator;

    protected SNAP_Reader(ReaderContext readerContext) {
        this.geometryFactory = readerContext.getGeometryFactory();
    }

    protected void open(File file, String formatName) throws IOException {
        product = ProductIO.readProduct(file, formatName);
        if (product == null) {
            throw new IOException("Unable to read product of type '" + formatName + "`': " + file.getAbsolutePath());
        }
        pixelLocator = null;
        uncachedProduct = null;
    }

    protected void openCached(File file, String formatName) throws IOException {
        uncachedProduct = ProductIO.readProduct(file, formatName);
        if (uncachedProduct == null) {
            throw new IOException("Unable to read product of type '" + formatName + "`': " + file.getAbsolutePath());
        }
        final Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("cacheSize", 2048);
        final Map<String, Product> productMap = new HashMap<>();
        productMap.put("source", uncachedProduct);
        this.product = GPF.createProduct("TileCache", parameterMap, productMap);
        pixelLocator = null;
    }

    @Override
    public void close() throws IOException {
        pixelLocator = null;
        if (product != null) {
            product.dispose();
            product = null;
        }
        if (uncachedProduct != null) {
            uncachedProduct.dispose();
            uncachedProduct = null;
        }
    }

    public AcquisitionInfo read(Interval interval, int numSplits) throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        extractSensingTimes(acquisitionInfo);

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        final Geometries geometries = calculateGeometries(interval, numSplits);
        acquisitionInfo.setBoundingGeometry(geometries.getBoundingGeometry());
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);

        return acquisitionInfo;
    }

    @Override
    public Dimension getProductSize() {
        final int width = product.getSceneRasterWidth();
        final int height = product.getSceneRasterHeight();

        return new Dimension("product_size", width, height);
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

    abstract protected void readProductData(RasterDataNode dataNode, Array targetArray, int width, int height, int xOffset, int yOffset) throws IOException;

    private void extractSensingTimes(AcquisitionInfo acquisitionInfo) {
        final ProductData.UTC startTime = product.getStartTime();
        acquisitionInfo.setSensingStart(startTime.getAsDate());

        final ProductData.UTC endTime = product.getEndTime();
        acquisitionInfo.setSensingStop(endTime.getAsDate());
    }

    protected RasterDataNode getRasterDataNode(String variableName) {
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

    private Geometries calculateGeometries(Interval interval, int numSplits) throws IOException {
        final Geometries geometries = new Geometries();

        final TiePointGrid longitude = product.getTiePointGrid(getLongitudeVariableName());
        final TiePointGrid latitude = product.getTiePointGrid(getLatitudeVariableName());

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
        final BoundingPolygonCreator boundingPolygonCreator = new BoundingPolygonCreator(interval, geometryFactory);
        Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(lonArray, latArray);
        if (!boundingGeometry.isValid()) {
            boundingGeometry = boundingPolygonCreator.createBoundingGeometrySplitted(lonArray, latArray, numSplits, false);
            if (!boundingGeometry.isValid()) {
                throw new RuntimeException("Invalid bounding geometry detected");
            }
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometrySplitted(lonArray, latArray, numSplits);
        } else {
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(lonArray, latArray);
        }

        geometries.setBoundingGeometry(boundingGeometry);
        geometries.setTimeAxesGeometry(timeAxisGeometry);

        return geometries;
    }

    protected void readSubsetData(RasterDataNode dataNode, Array targetArray, int width, int height, int xOffset, int yOffset, int sceneRasterWidth, int sceneRasterHeight) throws IOException {
        final Rectangle subsetRectangle = new Rectangle(xOffset, yOffset, width, height);
        final Rectangle productRectangle = new Rectangle(0, 0, sceneRasterWidth, sceneRasterHeight);
        final Rectangle intersection = productRectangle.intersection(subsetRectangle);
        final double noDataValue = getGeophysicalNoDataValue(dataNode);
        if (intersection.isEmpty()) {
            MAMath.setDouble(targetArray, noDataValue);
            return;
        }

        final DataType dataType = targetArray.getDataType();
        final Array readingArray = createReadingArray(dataType, new int[]{intersection.width, intersection.height});

        if (dataType == FLOAT) {
            dataNode.readPixels(intersection.x, intersection.y, intersection.width, intersection.height, (float[]) readingArray.getStorage());
        } else if (dataType == INT || dataType == SHORT || dataType == BYTE) {
            dataNode.readPixels(intersection.x, intersection.y, intersection.width, intersection.height, (int[]) readingArray.getStorage());
        }

        final Index index = targetArray.getIndex();
        int readIndex = 0;
        for (int y = 0; y < height; y++) {
            final int currentY = yOffset + y;
            for (int x = 0; x < width; x++) {
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

    protected void readRawProductData(RasterDataNode dataNode, Array readArray, int width, int height, int xOffset, int yOffset) throws IOException {
        final DataType dataType = readArray.getDataType();

        final Rectangle subsetRectangle = new Rectangle(xOffset, yOffset, width, height);
        final int sceneRasterWidth = dataNode.getRasterWidth();
        final int sceneRasterHeight = dataNode.getRasterHeight();
        final Rectangle productRectangle = new Rectangle(0, 0, sceneRasterWidth, sceneRasterHeight);
        final Rectangle intersection = productRectangle.intersection(subsetRectangle);

        if (intersection.isEmpty()) {
            return; // no intersecting area with product raster tb 2019-01-17
        }

        final int rasterSize = intersection.width * intersection.height;
        final ProductData productData = createProductData(dataType, rasterSize);
        final double noDataValue = getNoDataValue(dataNode);

        dataNode.readRasterData(intersection.x, intersection.y, intersection.width, intersection.height, productData);
//        for (int i = 0; i < rasterSize; i++) {
//            readArray.setObject(i, productData.getElemDoubleAt(i));
//        }
        int readIndex = 0;
        final Index index = readArray.getIndex();
        for (int y = 0; y < height; y++) {
            final int currentY = yOffset + y;
            for (int x = 0; x < width; x++) {
                final int currentX = xOffset + x;
                index.set(y, x);
                if (currentX >= 0 && currentX < sceneRasterWidth && currentY >= 0 && currentY < sceneRasterHeight) {
                    readArray.setObject(index, productData.getElemDoubleAt(readIndex));
                    ++readIndex;
                }else {
                    readArray.setObject(index, noDataValue);
                }

            }
        }
    }

    // package access for testing only tb 2019-05-17
    static ProductData createProductData(DataType dataType, int rasterSize) {
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

    // package access for testing only tb 2019-05-17
    protected static double getGeophysicalNoDataValue(RasterDataNode dataNode) {
        if (dataNode.isNoDataValueUsed()) {
            return dataNode.getGeophysicalNoDataValue();
        } else {
            final int dataType = dataNode.getDataType();
            return ReaderUtils.getDefaultFillValue(dataType).doubleValue();
        }
    }

    protected static double getNoDataValue(RasterDataNode dataNode) {
        if (dataNode.isNoDataValueUsed()) {
            return dataNode.getNoDataValue();
        } else {
            final int dataType = dataNode.getDataType();
            return ReaderUtils.getDefaultFillValue(dataType).doubleValue();
        }
    }

    // package access for testing only tb 2019-05-17
    protected static Array createReadingArray(DataType targetDataType, int[] shape) {
        switch (targetDataType) {
            case FLOAT:
                return Array.factory(DataType.FLOAT, shape);
            case INT:
            case SHORT:
            case BYTE:
                return Array.factory(DataType.INT, shape);
            default:
                throw new RuntimeException("unsupported data type: " + targetDataType);
        }
    }

    protected static int[] getShape(Interval interval) {
        final int[] shape = new int[2];
        shape[0] = interval.getY();
        shape[1] = interval.getX();

        return shape;
    }
}
