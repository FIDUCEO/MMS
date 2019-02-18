package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.location.PixelLocatorFactory;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.fiduceo.reader.Geometries;
import com.bc.fiduceo.reader.RawDataReader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AVHRR_FCDR_Reader extends FCDR_Reader {

    private static final int NUM_SPLITS = 2;
    // @todo 2 tb/tb move intervals to config 2019-02-18
    private static final Interval STEP_INTERVAL = new Interval(40, 100);
    private static final String LONGITUDE_VAR_NAME = "longitude";
    private static final String LATITUDE_VAR_NAME = "latitude";

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
    private PixelLocator pixelLocator;

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
        pixelLocator = null;

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
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final ArrayDouble lonStorage = (ArrayDouble) arrayCache.getScaled(LONGITUDE_VAR_NAME, "scale_factor", "add_offset");
            final ArrayDouble latStorage = (ArrayDouble) arrayCache.getScaled(LATITUDE_VAR_NAME, "scale_factor", "add_offset");
            final int[] shape = lonStorage.getShape();
            final int width = shape[1];
            final int height = shape[0];
            pixelLocator = PixelLocatorFactory.getSwathPixelLocator(lonStorage, latStorage, width, height);
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        final Array longitudes = arrayCache.get("lon");
        final int[] shape = longitudes.getShape();
        final int height = shape[0];
        final int width = shape[1];
        final int subsetHeight = getBoundingPolygonCreator(STEP_INTERVAL).getSubsetHeight(height, NUM_SPLITS);
        final PixelLocator pixelLocator = getPixelLocator();

        return PixelLocatorFactory.getSubScenePixelLocator(sceneGeometry, width, height, subsetHeight, pixelLocator);
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        if (timeLocator == null) {
            timeLocator = new AVHRR_FCDR_TimeLocator(arrayCache.get("Time"));
        }
        return timeLocator;
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
        final Array rawTimeArray = readRaw(x, y, interval, "Time");

        final Number fillValue = getFillValue("Time");
        final int[] shape = rawTimeArray.getShape();
        int height = shape[0];
        int width = shape[1];
        final ArrayInt.D2 integerTimeArray = new ArrayInt.D2(height, width);
        final int targetFillValue = (int) NetCDFUtils.getDefaultFillValue(DataType.INT, false);
        final Index index = rawTimeArray.getIndex();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                index.set(i, j);
                final double rawTime = rawTimeArray.getDouble(index);
                if (!fillValue.equals(rawTime)) {
                    integerTimeArray.set(i, j, (int) Math.round(rawTime));
                } else {
                    integerTimeArray.set(i, j, targetFillValue);
                }
            }
        }

        return integerTimeArray;
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

    @Override
    public String getLongitudeVariableName() {
        return LONGITUDE_VAR_NAME;
    }

    @Override
    public String getLatitudeVariableName() {
        return LATITUDE_VAR_NAME;
    }
}
