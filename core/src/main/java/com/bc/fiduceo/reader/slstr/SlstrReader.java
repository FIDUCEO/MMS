package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.reader.snap.SNAP_Reader;
import com.bc.fiduceo.reader.snap.VariableProxy;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.datamodel.*;
import ucar.ma2.*;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ucar.ma2.DataType.INT;

public class SlstrReader extends SNAP_Reader {

    private static final String REGEX = "S3([AB])_SL_1_RBT_.*(.SEN3)?";
    private static final Interval INTERVAL = new Interval(100, 100);
    private static final int NUM_SPLITS = 1;

    private final VariableNames variableNames;
    private long[] subs_times;

    SlstrReader(ReaderContext readerContext) {
        super(readerContext);

        variableNames = new VariableNames();
    }

    // package access for testing only tb 2019-05-13
    static long[] subSampleTimes(long[] timeStamps) {
        final long[] subs_times = new long[timeStamps.length / 2];

        int writeIndex = 0;
        for (int i = 0; i < timeStamps.length; i++) {
            if (i % 2 == 0) {
                subs_times[writeIndex] = timeStamps[i];
                ++writeIndex;
            }
        }
        return subs_times;
    }

    @Override
    public void open(File file) throws IOException {
        open(file, "Sen3");
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = read(INTERVAL, NUM_SPLITS);

        setOrbitNodeInfo(acquisitionInfo);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REGEX;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() {
        ensureTimingVector();

        return new TimeLocator_MicrosSince2000(subs_times);
    }

    @Override
    public List<Variable> getVariables() {
        final List<Variable> result = new ArrayList<>();

        final Band[] bands = product.getBands();
        for (final Band band : bands) {
            if (variableNames.isValidName(band.getName())) {
                final VariableProxy variableProxy = new VariableProxy(band);
                result.add(variableProxy);
            }
        }

        final TiePointGrid[] tiePointGrids = product.getTiePointGrids();
        for (final TiePointGrid tiePointGrid : tiePointGrids) {
            if (variableNames.isValidName(tiePointGrid.getName())) {
                final VariableProxy variableProxy = new VariableProxy(tiePointGrid);
                result.add(variableProxy);
            }
        }

        return result;
    }

    @Override
    public Dimension getProductSize() {
        // we only use 1km resolution here tb 2019-05-16
        final int width = product.getSceneRasterWidth() / 2;
        final int height = product.getSceneRasterHeight() / 2;

        return new Dimension("product_size", width, height);
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

        final int sceneRasterWidth = product.getSceneRasterWidth() / 2;
        final int sceneRasterHeight = product.getSceneRasterHeight() / 2;


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

        readRawProductData(dataNode, readArray, width, height, xOffset, yOffset);

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

    protected void readProductData(RasterDataNode dataNode, Array targetArray, int width, int height, int xOffset, int yOffset) throws IOException {
        final int sceneRasterWidth = product.getSceneRasterWidth() / 2;
        final int sceneRasterHeight = product.getSceneRasterHeight() / 2;

        readSubsetData(dataNode, targetArray, width, height, xOffset, yOffset, sceneRasterWidth, sceneRasterHeight);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        final int width = interval.getX();
        final int height = interval.getY();
        final int[] timeArray = new int[width * height];

        ensureTimingVector();

        final int sceneRasterHeight = product.getSceneRasterHeight() / 2;
        final int sceneRasterWidth = product.getSceneRasterWidth() / 2;
        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
        final int halfHeight = height / 2;
        final int halfWidth = width / 2;
        int writeOffset = 0;

        for (int yRead = y - halfHeight; yRead <= y + halfHeight; yRead++) {
            int lineTimeSeconds = fillValue;
            if (yRead >= 0 && yRead < sceneRasterHeight) {
                final long lineTimeMillis = TimeLocator_MicrosSince2000.convertToUnixEpochMillis(subs_times[yRead]);
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
        return "longitude_tx";
    }

    @Override
    public String getLatitudeVariableName() {
        return "latitude_tx";
    }

    @Override
    protected RasterDataNode getRasterDataNode(String variableName) {
        if (!variableNames.isValidName(variableName)) {
            throw new RuntimeException("Requested variable not contained in product: " + variableName);
        }

        return super.getRasterDataNode(variableName);
    }

    private void setOrbitNodeInfo(AcquisitionInfo acquisitionInfo) {
        acquisitionInfo.setNodeType(NodeType.UNDEFINED);
        final MetadataElement metadataRoot = product.getMetadataRoot();
        final MetadataElement manifest = metadataRoot.getElement("Manifest");
        if (manifest != null) {
            final MetadataElement metadataSection = manifest.getElement("metadataSection");
            if (metadataSection != null) {
                final MetadataElement orbitReference = metadataSection.getElement("orbitReference");
                if (orbitReference != null) {
                    final MetadataElement orbitNumber = orbitReference.getElement("orbitNumber");
                    if (orbitNumber != null) {
                        final String groundTrackDirection = orbitNumber.getAttribute("groundTrackDirection").getData().getElemString();
                        if (groundTrackDirection.equalsIgnoreCase("descending")) {
                            acquisitionInfo.setNodeType(NodeType.DESCENDING);
                        } else if (groundTrackDirection.equalsIgnoreCase("ascending")) {
                            acquisitionInfo.setNodeType(NodeType.ASCENDING);
                        }
                    }
                }
            }
        }
    }

    private void ensureTimingVector() {
        if (subs_times == null) {
            final MetadataElement metadataRoot = product.getMetadataRoot();
            final MetadataElement time_stamp_a = metadataRoot.getElement("time_stamp_a");
            final MetadataAttribute values = time_stamp_a.getAttribute("value");
            final ProductData valuesData = values.getData();
            final long[] timeStamps = (long[]) valuesData.getElems();
            subs_times = subSampleTimes(timeStamps);
        }
    }
}
