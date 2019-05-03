package com.bc.fiduceo.reader.avhrr_frac;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.reader.snap.SNAP_Reader;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.s3tbx.dataio.avhrr.AvhrrConstants;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.datamodel.VirtualBand;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static ucar.ma2.DataType.*;

public class AVHRR_FRAC_Reader extends SNAP_Reader {

    private static final String REG_EX = "NSS.FRAC.M2.D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.SV";
    private static final Interval INTERVAL = new Interval(5, 20);
    private static final int NUM_SPLITS = 2;

    private File tempFile;
    private final ReaderContext readerContext;

    AVHRR_FRAC_Reader(ReaderContext readerContext) {
        super(readerContext);
        this.readerContext = readerContext;
    }

    @Override
    public void open(File file) throws IOException {
        if (ReaderUtils.isCompressed(file)) {
            tempFile = readerContext.createTempFile("avhrr_frac", "tmp");
            ReaderUtils.decompress(file, tempFile);
            open(tempFile, AvhrrConstants.PRODUCT_TYPE);
        } else {
            open(file, AvhrrConstants.PRODUCT_TYPE);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (tempFile != null) {
            readerContext.deleteTempFile(tempFile);
            tempFile = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        return read(INTERVAL, NUM_SPLITS);
    }

    @Override
    public String getRegEx() {
        return REG_EX;
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
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException {
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
    public String getLongitudeVariableName() {
        return "longitude";
    }

    @Override
    public String getLatitudeVariableName() {
        return "latitude";
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
        if (rasterSize < 1) {
            return; // no intersecting area with product raster tb 2019-01-17
        }

        final ProductData productData = createProductData(dataType, rasterSize);

        dataNode.readRasterData(intersection.x, intersection.y, intersection.width, intersection.height, productData);
        for (int i = 0; i < rasterSize; i++) {
            readArray.setObject(i, productData.getElemDoubleAt(i));
        }
    }
}
