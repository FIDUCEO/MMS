package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.CF_OFFSET_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_SCALE_FACTOR_NAME;

class HIRS_FCDR_Reader extends FCDR_Reader {

    // @todo 2 tb/tb move intervals to config 2019-02-18
    private static final Interval STEP_INTERVAL = new Interval(8, 30);
    private static final int NUM_BT_CHANNELS = 19;
    private static final int CHANNEL_DIMENSION_INDEX = 0;
    private static final int CHANNEL_QUALITY_DIMENSION_INDEX = 1;

    private static final String REG_EX = "FIDUCEO_FCDR_L1C_HIRS(2|3|4)_(METOPA|NOAA[0-9]{2})_[0-9]{14}_[0-9]{14}_EASY_v0.8rc1_fv\\d\\.\\d\\.\\d\\.nc";

    // these variables do not have dimensionality that can be handled by the core MMS engine. They need to be
    // transferred using a post-processing step tb 2019-02-18
    private static String[] VARIABLE_NAMES_TO_REMOVE = {
            "SRF_wavelengths",
            "SRF_weights",
            "channel_correlation_matrix_independent",
            "channel_correlation_matrix_structured",
            "cross_element_correlation_coefficients",
            "cross_line_correlation_coefficients",
            "lookup_table_BT",
            "lookup_table_radiance",
            "x",
            "y"};

    private HIRS_FCDR_TimeLocator timeLocator;

    HIRS_FCDR_Reader(ReaderContext readerContext) {
        super(readerContext);
    }

    @Override
    public void open(File file) throws IOException {
        super.open(file);
    }

    @Override
    public void close() throws IOException {
        timeLocator = null;

        super.close();
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        final String fileName = file.getName();
        acquisitionInfo.setSensingStart(FCDRUtils.parseStartDate(fileName));
        acquisitionInfo.setSensingStop(FCDRUtils.parseStopDate(fileName));

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator(STEP_INTERVAL);
        final Geometries geometries = calculateGeometries(true, boundingPolygonCreator);
        final Geometry boundingGeometry = geometries.getBoundingGeometry();
        acquisitionInfo.setBoundingGeometry(boundingGeometry);
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        return getSubScenePixelLocator(sceneGeometry, STEP_INTERVAL);
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        if (timeLocator == null) {
            final Array time = arrayCache.get("time");
            final double scale_factor = (double) arrayCache.getNumberAttributeValue(CF_SCALE_FACTOR_NAME, "time");
            final double offset = (double) arrayCache.getNumberAttributeValue(CF_OFFSET_NAME, "time");

            timeLocator = new HIRS_FCDR_TimeLocator(time, scale_factor, offset);
        }
        return timeLocator;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException {
        final String fullVariableName = ReaderUtils.stripChannelSuffix(variableName);

        Array array = arrayCache.get(fullVariableName);
        final int rank = array.getRank();

        if (rank == 3) {
            final int channelIndex = ReaderUtils.getChannelIndex(variableName);
            final int[] shape = array.getShape();
            shape[0] = 1;   // we only want one z-layer
            final int[] offsets = {channelIndex, 0, 0};
            array = NetCDFUtils.section(array, offsets, shape);
        }

        final Number fillValue = getFillValue(fullVariableName);

        final Dimension productSize = getProductSize();
        return RawDataReader.read(centerX, centerY, interval, fillValue, array, productSize);
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException {
        final Array array = readRaw(centerX, centerY, interval, variableName);
        final String fullVariableName = ReaderUtils.stripChannelSuffix(variableName);

        final double scaleFactor = getScaleFactorCf(fullVariableName);
        final double offset = getOffset(fullVariableName);
        if (ReaderUtils.mustScale(scaleFactor, offset)) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, offset);
            return MAMath.convert2Unpacked(array, scaleOffset);
        }
        return array;
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        return readAcquisitionTime(x, y, interval, "time");
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException {
        final List<Variable> fileVariables = netcdfFile.getVariables();
        for (String var_name : VARIABLE_NAMES_TO_REMOVE) {
            final Variable variable = netcdfFile.findVariable(var_name);
            fileVariables.remove(variable);
        }

        final List<Variable> result = new ArrayList<>();
        for (final Variable variable : fileVariables) {
            final String variableName = variable.getFullName();
            switch (variableName) {
                case "bt":
                    addLayered3DVariables(result, variable, NUM_BT_CHANNELS, CHANNEL_DIMENSION_INDEX);
                    break;

                case "u_common":
                    addLayered3DVariables(result, variable, NUM_BT_CHANNELS, CHANNEL_DIMENSION_INDEX);
                    break;

                case "u_independent":
                    addLayered3DVariables(result, variable, NUM_BT_CHANNELS, CHANNEL_DIMENSION_INDEX);
                    break;

                case "u_structured":
                    addLayered3DVariables(result, variable, NUM_BT_CHANNELS, CHANNEL_DIMENSION_INDEX);
                    break;

                case "quality_channel_bitmask":
                    addChannelVectorVariables(result, variable, NUM_BT_CHANNELS, CHANNEL_QUALITY_DIMENSION_INDEX);
                    break;

                default:
                    result.add(variable);
            }
        }

        return result;
    }

    @Override
    public Dimension getProductSize() {
        final Variable lon = netcdfFile.findVariable("longitude");
        final int[] shape = lon.getShape();
        return new com.bc.fiduceo.core.Dimension("Ch1", shape[1], shape[0]);
    }
}
