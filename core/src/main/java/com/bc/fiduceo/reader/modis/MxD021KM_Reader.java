package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.hdf.HdfEOSUtil;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.reader.netcdf.StandardLayerExtension;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_TAI1993Scan;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import com.sun.org.apache.regexp.internal.RE;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Structure;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.bc.fiduceo.reader.modis.ModisConstants.LATITUDE_VAR_NAME;
import static com.bc.fiduceo.reader.modis.ModisConstants.LONGITUDE_VAR_NAME;

class MxD021KM_Reader extends NetCDFReader {

    private static final String REG_EX = "M([OY])D021KM.A\\d{7}.\\d{4}.\\d{3}.\\d{13}.hdf";
    private static final String GEOLOCATION_GROUP = "MODIS_SWATH_Type_L1B/Geolocation_Fields";
    private static final String DATA_GROUP = "MODIS_SWATH_Type_L1B/Data_Fields";
    private static final String SWATH_METADATA = "Level_1B_Swath_Metadata";
    private static final String SECTOR_START_TIME = "EV_Sector_Start_Time";

    private final GeometryFactory geometryFactory;

    private Dimension productSize;
    private TimeLocator timeLocator;

    MxD021KM_Reader(ReaderContext readerContext) {
        geometryFactory = readerContext.getGeometryFactory();
        productSize = null;
        timeLocator = null;
    }

    @Override
    public void open(File file) throws IOException {
        super.open(file);
        final int[] ymd = extractYearMonthDayFromFilename(file.getName());

    }

    @Override
    public void close() throws IOException {
        productSize = null;
        timeLocator = null;
        super.close();
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        HdfEOSUtil.extractAcquisitionTimes(acquisitionInfo, netcdfFile);

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);
        extractGeometries(acquisitionInfo);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        return null;
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return null;
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException {
        final List<Variable> variablesInFile = netcdfFile.getVariables();

        final ArrayList<Variable> exportVariables = new ArrayList<>();
        for (Variable variable : variablesInFile) {
            final String variableName = variable.getShortName();
            if (variableName.contains("Metadata") ||
                    variableName.contains("Band_") ||
                    variableName.contains("Noise_in_Thermal_Detectors") ||
                    variableName.contains("Change_in_relative_responses_of_thermal_detectors") ||
                    variableName.contains("DC_Restore_Change_for_Thermal_Bands") ||
                    variableName.contains("DC_Restore_Change_for_Reflective_") ||
                    variableName.contains("nscans") ||
                    variableName.contains("Max_EV_frames")) {
                continue;
            }

            if (variableName.contains("EV_1KM_RefSB")) {
                addLayered3DVariables(exportVariables, variable, 15, variableName, new ModisL1ReflectiveExtension());
                continue;
            }

            if (variableName.contains("EV_1KM_Emissive")) {
                addLayered3DVariables(exportVariables, variable, 16, variableName, new ModisL1EmissiveExtension());
                continue;
            }

            if (variableName.contains("EV_250_Aggr1km_RefSB")) {
                addLayered3DVariables(exportVariables, variable, 2, variableName, new StandardLayerExtension());
                continue;
            }

            if (variableName.contains("EV_500_Aggr1km_RefSB")) {
                addLayered3DVariables(exportVariables, variable, 5, variableName, new StandardLayerExtension(2));
                continue;
            }

            // - MxD03 Variables
            // Latitude, Longitude, Height, SensorZenith, SensorAzimuth, Range, SolarZenith, SolarAzimuth
            // gflags

            exportVariables.add(variable);
        }

        return exportVariables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        if (productSize == null) {
            final Array longitude = arrayCache.get(DATA_GROUP, "EV_Band26");
            final int[] shape = longitude.getShape();
            productSize = new Dimension("shape", shape[1], shape[0]);
        }
        return productSize;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        if (timeLocator == null) {
            final Variable level_1B_swath_metadata = netcdfFile.findVariable(null, SWATH_METADATA);
            if (level_1B_swath_metadata == null) {
                throw new IOException(SWATH_METADATA + " not found.");
            }

            final Structure l1SwathMeta = (Structure) level_1B_swath_metadata;
            final Structure sectorStartTime = l1SwathMeta.select(SECTOR_START_TIME);
            final Variable startTime = sectorStartTime.findVariable(SECTOR_START_TIME);
            final Array startTimeArray = startTime.read();
            // @todo 2 tb/tb read number of lines per scan from data 2020-05-14
            timeLocator = new TimeLocator_TAI1993Scan(startTimeArray, 10);
        }

        return timeLocator;
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final String yearString = fileName.substring(10, 14);
        final String doyString = fileName.substring(14, 17);
        final String doyPattern = yearString + "-" + doyString;

        final Date date = TimeUtils.parseDOYBeginOfDay(doyPattern);
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.setTime(date);

        final int[] ymd = new int[3];
        ymd[0] = utcCalendar.get(Calendar.YEAR);
        ymd[1] = utcCalendar.get(Calendar.MONTH) + 1;
        ymd[2] = utcCalendar.get(Calendar.DAY_OF_MONTH);
        return ymd;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
       // @todo 1 tb/tb add distinction for MOD03 data and the sensor state variables
        final String fullVariableName = ReaderUtils.stripChannelSuffix(variableName);
        Array array = arrayCache.get(DATA_GROUP, fullVariableName);
        final Number fillValue = arrayCache.getNumberAttributeValue(NetCDFUtils.CF_FILL_VALUE_NAME, DATA_GROUP, fullVariableName);
        if (fillValue == null) {
            throw new RuntimeException("Fill value for not found for variable: " + variableName);
        }

        final int rank = array.getRank();
        if (rank == 3) {
            final int layerIndex = getLayerIndex(variableName);
            final int[] shape = array.getShape();
            shape[0] = 1;   // we only want one z-layer
            final int[] offsets = {layerIndex, 0, 0};
            array = NetCDFUtils.section(array, offsets, shape);
        }

        return RawDataReader.read(centerX, centerY, interval, fillValue, array, getProductSize());
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException {
        return (ArrayInt.D2) acquisitionTimeFromTimeLocator(y, interval);
    }

    @Override
    public String getLongitudeVariableName() {
        return LONGITUDE_VAR_NAME;
    }

    @Override
    public String getLatitudeVariableName() {
        return LATITUDE_VAR_NAME;
    }

    // package access for testing only tb 2020-05-19
    static int getLayerIndex(String variableName) {
        final int splitIndex = variableName.lastIndexOf("_ch");
        if (splitIndex < 0) {
            return 0;
        }

        final String channelKey = variableName.substring(splitIndex + 3);
        if (channelKey.contains("H") || channelKey.contains("L")) {
            // special treatment for 1km reflective data
            throw new IllegalStateException("Implement me!");
        }

        int nominalLayerIndex = Integer.parseInt(channelKey) - 1;
        if (variableName.contains("500_Aggr1km")) {
            nominalLayerIndex -= 2;
        }
        return nominalLayerIndex;
    }

    private void extractGeometries(AcquisitionInfo acquisitionInfo) throws IOException {
        final BoundingPolygonCreator boundingPolygonCreator = new BoundingPolygonCreator(new Interval(50, 50), geometryFactory);
        final Array longitude = arrayCache.get(GEOLOCATION_GROUP, LONGITUDE_VAR_NAME);
        final Array latitude = arrayCache.get(GEOLOCATION_GROUP, LATITUDE_VAR_NAME);
        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(longitude, latitude);
        if (!boundingGeometry.isValid()) {
            throw new RuntimeException("Detected invalid bounding geometry");
        }
        acquisitionInfo.setBoundingGeometry(boundingGeometry);

        final Geometries geometries = new Geometries();
        geometries.setBoundingGeometry(boundingGeometry);
        final LineString timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitude, latitude);
        geometries.setTimeAxesGeometry(timeAxisGeometry);
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);
    }
}
