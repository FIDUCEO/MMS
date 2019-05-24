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

import static com.bc.fiduceo.reader.slstr.VariableType.NADIR_1km;
import static ucar.ma2.DataType.INT;

public class SlstrReader extends SNAP_Reader {

    private static final String REGEX = "S3([AB])_SL_1_RBT_.*(.SEN3)?";
    private static final Interval INTERVAL = new Interval(100, 100);
    private static final int NUM_SPLITS = 1;

    private final VariableNames variableNames;
    private long[] subs_times;
    private TransformFactory transformFactory;

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

        transformFactory = new TransformFactory(product.getSceneRasterWidth(), product.getSceneRasterHeight());
    }

    @Override
    public void close() throws IOException {
        transformFactory = null;

        super.close();
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
        final Transform transform = transformFactory.get(NADIR_1km);
        return transform.getRasterSize();
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

        final VariableType variableType = variableNames.getVariableType(variableName);
        final Transform transform = transformFactory.get(variableType);
        final Dimension rasterSize = transform.getRasterSize();

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
                if (currentX >= 0 && currentX < rasterSize.getNx() && currentY >= 0 && currentY < rasterSize.getNy()) {
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
        final VariableType variableType = variableNames.getVariableType(variableName);
        final Transform transform = transformFactory.get(variableType);

        final int mappedX = transform.mapCoordinate(centerX);
        final int mappedY = transform.mapCoordinate(centerY);
        final Interval mappedInterval = transform.mapInterval(interval);

        final RasterDataNode dataNode = getRasterDataNode(variableName);
        final double noDataValue = SlstrReader.getGeophysicalNoDataValue(dataNode);

        final Array originalArray = super.readScaled(mappedX, mappedY, mappedInterval, variableName);

        return transform.process(originalArray, noDataValue);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) {
        final int width = interval.getX();
        final int height = interval.getY();
        final int[] timeArray = new int[width * height];

        ensureTimingVector();

        final Transform transform = transformFactory.get(NADIR_1km);
        final Dimension rasterSize = transform.getRasterSize();
        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
        final int halfHeight = height / 2;
        final int halfWidth = width / 2;
        int writeOffset = 0;

        for (int yRead = y - halfHeight; yRead <= y + halfHeight; yRead++) {
            int lineTimeSeconds = fillValue;
            if (yRead >= 0 && yRead < rasterSize.getNy()) {
                final long lineTimeMillis = TimeLocator_MicrosSince2000.convertToUnixEpochMillis(subs_times[yRead]);
                lineTimeSeconds = (int) Math.round(lineTimeMillis * 0.001);
            }

            for (int xRead = x - halfWidth; xRead <= x + halfWidth; xRead++) {
                if (xRead >= 0 && xRead < rasterSize.getNx()) {
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

    protected void readProductData(RasterDataNode dataNode, Array targetArray, int width, int height, int xOffset, int yOffset) throws IOException {
        final VariableType variableType = variableNames.getVariableType(dataNode.getName());
        final Transform transform = transformFactory.get(variableType);
        final Dimension rasterSize = transform.getRasterSize();

        readSubsetData(dataNode, targetArray, width, height, xOffset, yOffset, rasterSize.getNx(), rasterSize.getNy());
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
