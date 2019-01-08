package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.*;
import ucar.ma2.DataType;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;

public class AVHRR_FCDR_Reader implements Reader {

    private static final int NUM_SPLITS = 2;

    // these variales do not have dimensionality that can be handled by the core MMS engine. They need to be
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

    private final GeometryFactory geometryFactory;

    private NetcdfFile netcdfFile;
    private File file;
    private BoundingPolygonCreator boundingPolygonCreator;
    private ArrayCache arrayCache;
    private TimeLocator timeLocator;

    AVHRR_FCDR_Reader(ReaderContext readerContext) {
        this.geometryFactory = readerContext.getGeometryFactory();
    }

    @Override
    public void open(File file) throws IOException {
        this.file = file;
        netcdfFile = NetcdfFile.open(file.getPath());
        arrayCache = new ArrayCache(netcdfFile);
        timeLocator = null;
    }

    @Override
    public void close() throws IOException {
        file = null;
        timeLocator = null;
        boundingPolygonCreator = null;
        arrayCache = null;

        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        final String fileName = file.getName();
        acquisitionInfo.setSensingStart(parseStartDate(fileName));
        acquisitionInfo.setSensingStop(parseStopDate(fileName));

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        final Geometries geometries = calculateGeometries();
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
        throw new RuntimeException("not implemented");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        if (timeLocator == null) {
            timeLocator = new AVHRR_FCDR_TimeLocator(arrayCache.get("Time"));
        }
        return timeLocator;
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        throw new RuntimeException("not implemented");
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
        throw new RuntimeException("not implemented");
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
        // 1-D variables: @todo 1 tb/tb check if we need to handle these separately 2019-01-09
        // Time
        // quality_scanline_bitmask
        // scanline_map_to_origl1bfile
        // scanline_origl1b
        final List<Variable> fileVariables = netcdfFile.getVariables();
        for (String var_name : VARIABLE_NAMES_TO_REMOVE) {
            final Variable variable = netcdfFile.findVariable(var_name);
            fileVariables.remove(variable);
        }

        return fileVariables;
    }

    // @todo 1 tb/tb can be a NetCDF generic method 2019-01-07
    @Override
    public Dimension getProductSize() {
        final Variable ch1 = netcdfFile.findVariable("Ch1");
        final int[] shape = ch1.getShape();
        return new com.bc.fiduceo.core.Dimension("Ch1", shape[1], shape[0]);
    }

    @Override
    public String getLongitudeVariableName() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getLatitudeVariableName() {
        throw new RuntimeException("not implemented");
    }

    static Date parseStartDate(String fileName) {
        return parseDateFromName(fileName, 30);
    }

    static Date parseStopDate(String fileName) {
        return parseDateFromName(fileName, 45);
    }

    private static Date parseDateFromName(String fileName, int offset) {
        final int endIdx = offset + 14;
        final String dateString = fileName.substring(offset, endIdx);

        return TimeUtils.parse(dateString, "yyyyMMddHHmmss");
    }

    private Geometries calculateGeometries() throws IOException {
        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator();
        final Geometries geometries = new Geometries();

        final Array longitudes = arrayCache.getScaled("longitude", "scale_factor", "add_offset");
        final Array latitudes = arrayCache.getScaled("latitude", "scale_factor", "add_offset");
        Geometry timeAxisGeometry;
        Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(longitudes, latitudes);
        if (!boundingGeometry.isValid()) {
            boundingGeometry = boundingPolygonCreator.createBoundingGeometrySplitted(longitudes, latitudes, NUM_SPLITS, false);
            if (!boundingGeometry.isValid()) {
                throw new RuntimeException("Invalid bounding geometry detected");
            }
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometrySplitted(longitudes, latitudes, NUM_SPLITS);
        } else {
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitudes, latitudes);
        }

        geometries.setBoundingGeometry(boundingGeometry);
        geometries.setTimeAxesGeometry(timeAxisGeometry);
        return geometries;
    }

    private BoundingPolygonCreator getBoundingPolygonCreator() {
        if (boundingPolygonCreator == null) {
            // @todo 2 tb/tb move intervals to config 2018-12-17
            boundingPolygonCreator = new BoundingPolygonCreator(new Interval(40, 100), geometryFactory);
        }

        return boundingPolygonCreator;
    }

    // @todo 2 tb/tb make this method part of a generic NetCDF reader 2019-01-07
    // @todo 1 tb/tb duplicated AVHRR_GAC 2019-01-07
    private Number getFillValue(String variableName) throws IOException {
        final Number fillValue = arrayCache.getNumberAttributeValue(CF_FILL_VALUE_NAME, variableName);
        if (fillValue != null) {
            return fillValue;
        }
        final Array array = arrayCache.get(variableName);
        return NetCDFUtils.getDefaultFillValue(array);
    }
}
