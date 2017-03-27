/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.atsr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.fiduceo.reader.Geometries;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.datamodel.TiePointGrid;
import org.esa.snap.core.datamodel.TimeCoding;
import org.esa.snap.dataio.envisat.EnvisatConstants;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ucar.ma2.DataType.FLOAT;
import static ucar.ma2.DataType.INT;
import static ucar.ma2.DataType.SHORT;

class ATSR_L1B_Reader implements Reader {

    private static final Interval INTERVAL = new Interval(5, 20);
    private static final String REG_EX = "AT(1|2|S)_TOA_1P[A-Z0-9]{4}\\d{8}_\\d{6}_\\d{12}_\\d{5}_\\d{5}_\\d{4}.(N|E)(1|2)";

    private final GeometryFactory geometryFactory;

    private Product product;
    private ATSR_PixelLocator pixelLocator;

    ATSR_L1B_Reader(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public void open(File file) throws IOException {
        product = ProductIO.readProduct(file, EnvisatConstants.ENVISAT_FORMAT_NAME);
        if (product == null) {
            throw new IOException("Unable to read ATSR product: " + file.getAbsolutePath());
        }
        pixelLocator = null;
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

        extractSensingTimes(acquisitionInfo);

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        final Geometries geometries = calculateGeometries();
        acquisitionInfo.setBoundingGeometry(geometries.getBoundingGeometry());
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries, geometryFactory);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final GeoCoding geoCoding = product.getSceneGeoCoding();

            pixelLocator = new ATSR_PixelLocator(geoCoding);
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        // subscene is only relevant for segmented geometries which we do not have tb 2016-08-11
        return getPixelLocator();
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        return new ATSR_TimeLocator(product);
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        if (product.containsTiePointGrid(variableName)) {
            // we do not want raw data access on tie-point grids tb 2016-08-11
            return readScaled(centerX, centerY, interval, variableName);
        }

        final RasterDataNode dataNode = getRasterDataNode(variableName);

        final double noDataValue = getNoDataValue(dataNode);
        final DataType targetDataType = NetCDFUtils.getNetcdfDataType(dataNode.getDataType());
        final int[] shape = getShape(interval);
        final Array readArray = Array.factory(targetDataType, shape);
        final Array targetArray = Array.factory(targetDataType, shape);

        final int width = interval.getX();
        final int height = interval.getY();

        final int xOffset = centerX - width / 2;
        final int yOffset = centerY - height / 2;

        //System.out.println("node = " + dataNode.getName() + " x: " + xOffset + " y: " + yOffset + " w: " + width + " h: " + height);
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
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
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
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        // @todo 3 tb/** this method should be combined with the functionality implemented in WindowReader classes. 2016-08-10
        final int width = interval.getX();
        final int height = interval.getY();
        final int[] timeArray = new int[width * height];

        final PixelPos pixelPos = new PixelPos();
        final TimeCoding sceneTimeCoding = product.getSceneTimeCoding();
        final int sceneRasterHeight = product.getSceneRasterHeight();
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int halfHeight = height / 2;
        final int halfWidth = width / 2;
        int writeOffset = 0;
        for (int yRead = y - halfHeight; yRead <= y + halfHeight; yRead++) {
            int lineTimeSeconds = Integer.MIN_VALUE;
            if (yRead >= 0 && yRead < sceneRasterHeight) {
                pixelPos.setLocation(x, yRead + 0.5);
                final double lineMjd = sceneTimeCoding.getMJD(pixelPos);
                final long lineTime = TimeUtils.mjd2000ToDate(lineMjd).getTime();
                lineTimeSeconds = (int) Math.round(lineTime * 0.001);
            }

            for (int xRead = x - halfWidth; xRead <= x + halfWidth; xRead++) {
                if (xRead >= 0 && xRead < sceneRasterWidth) {
                    timeArray[writeOffset] = lineTimeSeconds;
                } else {
                    timeArray[writeOffset] = Integer.MIN_VALUE;
                }
                ++writeOffset;
            }
        }

        final int[] shape = getShape(interval);
        return (ArrayInt.D2) Array.factory(INT, shape, timeArray);
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException {
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
    public Dimension getProductSize() throws IOException {
        final int width = product.getSceneRasterWidth();
        final int height = product.getSceneRasterHeight();

        return new Dimension("product_size", width, height);
    }

    private void extractSensingTimes(AcquisitionInfo acquisitionInfo) {
        final ProductData.UTC startTime = product.getStartTime();
        acquisitionInfo.setSensingStart(startTime.getAsDate());

        final ProductData.UTC endTime = product.getEndTime();
        acquisitionInfo.setSensingStop(endTime.getAsDate());
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

        final BoundingPolygonCreator boundingPolygonCreator = new BoundingPolygonCreator(INTERVAL, geometryFactory);
        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(lonArray, latArray);
        if (!boundingGeometry.isValid()) {
            throw new IOException("Invalid bounding geometry: implement splitted approach then");
        }
        geometries.setBoundingGeometry(boundingGeometry);

        final LineString timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(lonArray, latArray);
        geometries.setTimeAxesGeometry(timeAxisGeometry);

        return geometries;
    }

    // package access for testing only tb 2016-08-10
    static int[] getShape(Interval interval) {
        final int[] shape = new int[2];
        shape[0] = interval.getY();
        shape[1] = interval.getX();

        return shape;
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
            default:
                throw new RuntimeException("unsupported data type: " + targetDataType);
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

    // package access for testing only tb 2016-09-12
    static double getGeophysicalNoDataValue(RasterDataNode dataNode) {
        if (dataNode.isNoDataValueUsed()) {
            return dataNode.getGeophysicalNoDataValue();
        } else {
            final int dataType = dataNode.getDataType();
            return ReaderUtils.getDefaultFillValue(dataType).doubleValue();
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

    private void readRawProductData(RasterDataNode dataNode, Array readArray, int width, int height, int xOffset, int yOffset) throws IOException {
        final DataType dataType = readArray.getDataType();

        final Rectangle subsetRectangle = new Rectangle(xOffset, yOffset, width, height);
        final Rectangle productRectangle = new Rectangle(0, 0, product.getSceneRasterWidth(), product.getSceneRasterHeight());
        final Rectangle intersection = productRectangle.intersection(subsetRectangle);

        final int rasterSize = intersection.width * intersection.height;
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
        } else {
            throw new RuntimeException("Data type not supported");
        }
        return productData;
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

    private void copyTargetData(Array readArray, Array targetArray, int width, int height, int xOffset, int yOffset, double noDataValue) {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();
        final Index index = targetArray.getIndex();
        for (int x = 0; x < width; x++) {
            final int currentX = xOffset + x;
            for (int y = 0; y < height; y++) {
                final int currentY = yOffset + y;
                index.set(y, x);
                if (currentX >= 0 && currentX < sceneRasterWidth && currentY >= 0 && currentY < sceneRasterHeight) {
                    targetArray.setObject(index, readArray.getObject(index));
                } else {
                    targetArray.setObject(index, noDataValue);
                }
            }
        }
    }
}
