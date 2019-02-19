package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.location.PixelLocatorFactory;
import com.bc.fiduceo.reader.*;
import ucar.ma2.*;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AVHRR_FCDR_Reader extends FCDR_Reader {

    // @todo 2 tb/tb move intervals to config 2019-02-18
    private static final Interval STEP_INTERVAL = new Interval(40, 100);

    // these variables do not have dimensionality that can be handled by the core MMS engine. They need to be
    // transferred using a post-processing step tb 2019-01-08
    private static String[] VARIABLE_NAMES_TO_REMOVE = {"SRF_wavelengths",
            "SRF_weights",
            "channel",
            "channel_correlation_matrix_independent",
            "channel_correlation_matrix_structured",
            "cross_element_correlation_coefficients",
            "cross_line_correlation_coefficients",
            "lookup_table_BT",
            "lookup_table_radiance",
            "quality_channel_bitmask",
            "x",
            "y"};

    private TimeLocator timeLocator;

    AVHRR_FCDR_Reader(ReaderContext readerContext) {
        super(readerContext);
    }

    @Override
    public void open(File file) throws IOException {
        super.open(file);
        timeLocator = null;
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

        final Geometries geometries = calculateGeometries(false, STEP_INTERVAL);
        final Geometry boundingGeometry = geometries.getBoundingGeometry();
        acquisitionInfo.setBoundingGeometry(boundingGeometry);
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return "FIDUCEO_FCDR_L1C_AVHRR_(METOPA|NOAA[0-9]{2})_[0-9]{14}_[0-9]{14}_EASY_vBeta_fv\\d\\.\\d\\.\\d\\.nc";
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        if (timeLocator == null) {
            timeLocator = new AVHRR_FCDR_TimeLocator(arrayCache.get("Time"));
        }
        return timeLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        return getSubScenePixelLocator(sceneGeometry, STEP_INTERVAL);
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Array rawArray = arrayCache.get(variableName);
        final Number fillValue = getFillValue(variableName);

        final Dimension productSize = getProductSize();
        return RawDataReader.read(centerX, centerY, interval, fillValue, rawArray, productSize.getNx());
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Array array = readRaw(centerX, centerY, interval, variableName);

        final double scaleFactor = getScaleFactorCf(variableName);
        final double offset = getOffset(variableName);
        if (ReaderUtils.mustScale(scaleFactor, offset)) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, offset);
            return MAMath.convert2Unpacked(array, scaleOffset);
        }
        return array;
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        return readAcquisitionTime(x, y, interval, "Time");
    }

    @Override
    public List<Variable> getVariables() {
        final List<Variable> fileVariables = netcdfFile.getVariables();
        for (String var_name : VARIABLE_NAMES_TO_REMOVE) {
            final Variable variable = netcdfFile.findVariable(var_name);
            fileVariables.remove(variable);
        }

        return fileVariables;
    }

    @Override
    public Dimension getProductSize() {
        final Variable ch1 = netcdfFile.findVariable("Ch1");
        final int[] shape = ch1.getShape();
        return new com.bc.fiduceo.core.Dimension("Ch1", shape[1], shape[0]);
    }
}
