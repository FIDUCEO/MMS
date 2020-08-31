package com.bc.fiduceo.reader.avhrr_frac;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.time.TimeLocator;
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

import java.io.File;
import java.io.IOException;

import static ucar.ma2.DataType.INT;

public class AVHRR_FRAC_Reader extends SNAP_Reader {

    private static final String REG_EX = "NSS.FRAC.M2.D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.[A-Z]{2,2}(.gz){0,1}";
    private static final Interval INTERVAL = new Interval(5, 20);
    private static final int NUM_SPLITS = 2;
    private final ReaderContext readerContext;
    private File tempFile;

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

    protected void readProductData(RasterDataNode dataNode, Array targetArray, int width, int height, int xOffset, int yOffset) throws IOException {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();

        readSubsetData(dataNode, targetArray, width, height, xOffset, yOffset, sceneRasterWidth, sceneRasterHeight);
    }
}
