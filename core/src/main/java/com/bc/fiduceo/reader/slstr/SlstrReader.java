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
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
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

public class SlstrReader extends SNAP_Reader {

    private static final String REGEX = "S3([AB])_SL_1_RBT_.*(.SEN3)?";
    private static final Interval INTERVAL = new Interval(100, 100);
    private static final int NUM_SPLITS = 1;

    private List<String> variableNames;
    private long[] subs_times;

    SlstrReader(ReaderContext readerContext) {
        super(readerContext);

        initVariableNamesList();
        subs_times = null;
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
            if (variableNames.contains(band.getName())) {
                final VariableProxy variableProxy = new VariableProxy(band);
                result.add(variableProxy);
            }
        }

        final TiePointGrid[] tiePointGrids = product.getTiePointGrids();
        for (final TiePointGrid tiePointGrid : tiePointGrids) {
            if (variableNames.contains(tiePointGrid.getName())) {
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
        throw new RuntimeException("not implemented");
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

    private void readProductData(RasterDataNode dataNode, Array targetArray, int width, int height, int xOffset, int yOffset) throws IOException {
        final Rectangle subsetRectangle = new Rectangle(xOffset, yOffset, width, height);
        final Rectangle productRectangle = new Rectangle(0, 0, product.getSceneRasterWidth()/2, product.getSceneRasterHeight()/2);
        final Rectangle intersection = productRectangle.intersection(subsetRectangle);

        final DataType dataType = targetArray.getDataType();
        final Array readingArray = createReadingArray(dataType, new int[]{intersection.width, intersection.height});

        if (dataType == FLOAT) {
            dataNode.readPixels(intersection.x, intersection.y, intersection.width, intersection.height, (float[]) readingArray.getStorage());
        } else if (dataType == INT || dataType == SHORT) {
            dataNode.readPixels(intersection.x, intersection.y, intersection.width, intersection.height, (int[]) readingArray.getStorage());
        }
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
    protected RasterDataNode getRasterDataNode(String variableName) {
        if(!variableNames.contains(variableName)) {
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

    private void initVariableNamesList() {
        variableNames = new ArrayList<>();
        variableNames.add("latitude_tx");
        variableNames.add("longitude_tx");
        variableNames.add("sat_azimuth_tn");
        variableNames.add("sat_zenith_tn");
        variableNames.add("solar_azimuth_tn");
        variableNames.add("solar_zenith_tn");
        variableNames.add("S7_BT_in");
        variableNames.add("S8_BT_in");
        variableNames.add("S9_BT_in");
        variableNames.add("S7_exception_in");
        variableNames.add("S8_exception_in");
        variableNames.add("S9_exception_in");
        variableNames.add("S1_radiance_an");
        variableNames.add("S2_radiance_an");
        variableNames.add("S3_radiance_an");
        variableNames.add("S4_radiance_an");
        variableNames.add("S5_radiance_an");
        variableNames.add("S6_radiance_an");
        variableNames.add("S1_exception_an");
        variableNames.add("S2_exception_an");
        variableNames.add("S3_exception_an");
        variableNames.add("S4_exception_an");
        variableNames.add("S5_exception_an");
        variableNames.add("S6_exception_an");
        variableNames.add("sat_azimuth_to");
        variableNames.add("sat_zenith_to");
        variableNames.add("solar_azimuth_to");
        variableNames.add("solar_zenith_to");
        variableNames.add("S7_BT_io");
        variableNames.add("S8_BT_io");
        variableNames.add("S9_BT_io");
        variableNames.add("S7_exception_io");
        variableNames.add("S8_exception_io");
        variableNames.add("S9_exception_io");
        variableNames.add("S1_radiance_ao");
        variableNames.add("S2_radiance_ao");
        variableNames.add("S3_radiance_ao");
        variableNames.add("S4_radiance_ao");
        variableNames.add("S5_radiance_ao");
        variableNames.add("S6_radiance_ao");
        variableNames.add("S1_exception_ao");
        variableNames.add("S2_exception_ao");
        variableNames.add("S3_exception_ao");
        variableNames.add("S4_exception_ao");
        variableNames.add("S5_exception_ao");
        variableNames.add("S6_exception_ao");
        variableNames.add("confidence_in");
        variableNames.add("pointing_in");
        variableNames.add("bayes_in");
        variableNames.add("cloud_in");
        variableNames.add("bayes_io");
        variableNames.add("cloud_io");
    }
}
