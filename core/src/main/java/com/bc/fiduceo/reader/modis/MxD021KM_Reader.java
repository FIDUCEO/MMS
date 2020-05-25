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
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;
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
    private static final int LINES_PER_SCAN = 10;
    private static final int NUM_1KM_REF_CHAN = 15;

    private final GeometryFactory geometryFactory;

    private Dimension productSize;
    private TimeLocator timeLocator;

    MxD021KM_Reader(ReaderContext readerContext) {
        geometryFactory = readerContext.getGeometryFactory();
        productSize = null;
        timeLocator = null;
    }

    // package access for testing only tb 2020-05-19
    static int getLayerIndex(String variableName) {
        final int splitIndex = variableName.lastIndexOf("_ch");
        if (splitIndex < 0) {
            return 0;
        }

        final String channelKey = variableName.substring(splitIndex + 3);
        if (variableName.contains("1KM_RefSB")) {
            return getLayerIndex1kmRefl(channelKey);
        }

        int nominalLayerIndex = Integer.parseInt(channelKey) - 1;
        if (variableName.contains("500_Aggr1km")) {
            nominalLayerIndex -= 2;
        } else if (variableName.contains("1KM_Emissive")) {
            if (nominalLayerIndex <= 24) {
                nominalLayerIndex -= 19;
            } else {
                nominalLayerIndex -= 20;
            }

        }
        return nominalLayerIndex;
    }

    private static int getLayerIndex1kmRefl(String channelKey) {
        int offset = 8;
        if (channelKey.contains("H") || channelKey.contains("L")) {
            if (channelKey.contains("13H") || channelKey.contains("14L")) {
                offset = 7;
            } else if (channelKey.contains("14H")) {
                offset = 6;
            }
            channelKey = channelKey.substring(0, 2);
        }
        final int channelIndex = Integer.parseInt(channelKey);
        if (channelIndex == 26) {
            offset = 12;
        } else if (channelIndex >= 15) {
            offset = 6;
        }
        return channelIndex - offset;
    }

    // package access for testing only tb 2020-05-20
    static String getScaleFactorAttributeName(String variableName) {
        if (variableName.contains("RefSB_ch") || variableName.contains("Emissive_ch")) {
            return "radiance_scales";
        } else if (variableName.contains("Indexes_ch")) {
            return "scaling_factor";
        }

        return null;
    }

    // package access for testing only tb 2020-05-20
    static String getOffsetAttributeName(String variableName) {
        if (variableName.contains("RefSB_ch") || variableName.contains("Emissive_ch")) {
            return "radiance_offsets";
        }

        return null;
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

            // @todo 1 tb/tb scale_factors and offsets (and probably other attributes) need to be extracted per layer!
            if (variableName.contains("EV_1KM_RefSB")) {
                final ArrayList<Variable> bandVariables = new ArrayList<>(NUM_1KM_REF_CHAN);

                addLayered3DVariables(bandVariables, variable, NUM_1KM_REF_CHAN, variableName, new ModisL1ReflectiveExtension());

                exportVariables.addAll(bandVariables);
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

            // @todo 1 tb/tb Noise_In_Thermal_Detectors
            // @todo 1 tb/tb MxD03 Variables
            // Latitude, Longitude, Height, SensorZenith, SensorAzimuth, Range, SolarZenith, SolarAzimuth
            // gflags, Land/SeaMask, WaterPresent

            exportVariables.add(variable);
        }

        return exportVariables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        if (productSize == null) {
            final Array band26 = arrayCache.get(DATA_GROUP, "EV_Band26");
            final int[] shape = band26.getShape();
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
            timeLocator = new TimeLocator_TAI1993Scan(startTimeArray, LINES_PER_SCAN);
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
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException {
        // @todo 1 tb/tb add distinction for MOD03 data and the sensor state variables
        final String fullVariableName = ReaderUtils.stripChannelSuffix(variableName);
        // @todo 1 tb/tb extend to data from other groups!
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
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException {
        final Array rawArray = readRaw(centerX, centerY, interval, variableName);

        final String fullVariableName = ReaderUtils.stripChannelSuffix(variableName);
        final int layerIndex = getLayerIndex(variableName);

        // @todo 1 tb/tb extend to data from other groups!
        final String groupName = DATA_GROUP;
        double scale = 1.0;
        double offset = 0.0;

        final String scaleFactorAttributeName = getScaleFactorAttributeName(variableName);
        if (scaleFactorAttributeName != null) {
            final Number scaleFactor = getNumberAttributeLayered(fullVariableName, groupName, scaleFactorAttributeName, layerIndex);
            scale = scaleFactor.doubleValue();
        }

        final String offsetAttributeName = getOffsetAttributeName(variableName);
        if (offsetAttributeName != null) {
            final Number offsetNum = getNumberAttributeLayered(fullVariableName, groupName, "radiance_offsets", layerIndex);
            offset = offsetNum.doubleValue();
        }

        if (ReaderUtils.mustScale(scale, offset)) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scale, offset);
            return MAMath.convert2Unpacked(rawArray, scaleOffset);
        }

        return rawArray;
    }

    private Number getNumberAttributeLayered(String fullVariableName, String groupName, String attributeName, int layerIndex) throws IOException {
        final Attribute vectorAttribute = arrayCache.getAttribute(attributeName, groupName, fullVariableName);
        return vectorAttribute.getNumericValue(layerIndex);
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

    private void ensureMod03File() {
        // @todo tb/tb 2020-05-20
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
