package com.bc.fiduceo.reader.amsr.amsr2;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.location.PixelLocatorFactory;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.amsr.AmsrUtils;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.*;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;

@SuppressWarnings("SynchronizeOnNonFinalField")
public class AMSR2_Reader extends NetCDFReader {

    private static final String REG_EX = "GW1AM2_\\d{12}_\\d{3}[AD]_L1SGRTBR_\\d{7}.h5(.gz)?";
    private static final String[] LAND_OCEAN_FLAG_EXTENSIONS = new String[]{"6", "10", "23", "36"};

    private static final String LON_VARIABLE_NAME = "Longitude_of_Observation_Point_for_89A";
    private static final String LAT_VARIABLE_NAME = "Latitude_of_Observation_Point_for_89A";

    private final ReaderContext readerContext;
    private PixelLocator pixelLocator;
    private BoundingPolygonCreator boundingPolygonCreator;
    private File tempFile;

    AMSR2_Reader(ReaderContext readerContext) {
        this.readerContext = readerContext;
    }

    @Override
    public void open(File file) throws IOException {
        if (ReaderUtils.isCompressed(file)) {
            tempFile = readerContext.createTempFile("amsr2", "h5");
            ReaderUtils.decompress(file, tempFile);
            netcdfFile = NetcdfFile.open(tempFile.getPath());
        } else {
            netcdfFile = NetcdfFile.open(file.getPath());
        }
        arrayCache = new ArrayCache(netcdfFile);

        initializeVariables();
    }

    @Override
    public void close() throws IOException {
        pixelLocator = null;
        boundingPolygonCreator = null;
        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }
        if (tempFile != null) {
            readerContext.deleteTempFile(tempFile);
            tempFile = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        setSensingTimes(acquisitionInfo);
        AmsrUtils.setNodeType(acquisitionInfo, netcdfFile);
        setGeometries(acquisitionInfo);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final Array latitudes = arrayCache.get(LAT_VARIABLE_NAME);
            final Array longitudes = arrayCache.get(LON_VARIABLE_NAME);

            final int[] shape = longitudes.getShape();
            final int width = shape[1];
            final int height = shape[0];
            pixelLocator = PixelLocatorFactory.getSwathPixelLocator(longitudes, latitudes, width, height);
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        return getPixelLocator();
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        final Array scan_time = arrayCache.get("Scan_Time");
        return new TimeLocator_TAI1993Vector(scan_time);
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final String[] strings = fileName.split("_");
        final String dateTimePart = strings[1];

        return AmsrUtils.parseYMD(dateTimePart);
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final String escapedName = NetcdfFile.makeValidCDLName(variableName);
        final Array rawArray = arrayCache.get(escapedName);
        final Dimension productSize = getProductSize();
        final Number fillValue = getFillValue(escapedName);
        return RawDataReader.read(centerX, centerY, interval, fillValue, rawArray, productSize.getNx());
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Array rawArray = readRaw(centerX, centerY, interval, variableName);

        final double scaleFactor = getScaleFactor(variableName, "SCALE_FACTOR");
        if (scaleFactor != 1.0) {
            final double offset = 0.0;
            if (ReaderUtils.mustScale(scaleFactor, offset)) {
                final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, offset);
                return MAMath.convert2Unpacked(rawArray, scaleOffset);
            }
        }

        return rawArray;
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        final Array rawTimeTAI = readRaw(x, y, interval, "Scan_Time");
        final double fillValue = getFillValue("Scan_Time").doubleValue();

        final int acquisitionTimeFillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
        final Array acquisitionTimeUtc = Array.factory(DataType.INT, rawTimeTAI.getShape());
        for (int i = 0; i < rawTimeTAI.getSize(); i++) {
            final double rawTimeTAIDouble = rawTimeTAI.getDouble(i);
            if (rawTimeTAIDouble != fillValue) {
                final Date utcDate = TimeUtils.tai1993ToUtc(rawTimeTAIDouble);
                acquisitionTimeUtc.setInt(i, (int) (utcDate.getTime() * 0.001));
            } else {
                acquisitionTimeUtc.setInt(i, acquisitionTimeFillValue);
            }
        }

        return (ArrayInt.D2) acquisitionTimeUtc;
    }

    @Override
    public List<Variable> getVariables() {
        final List<Variable> variables = new ArrayList<>();

        final List<Variable> fileVariables = netcdfFile.getVariables();
        for (final Variable fileVariable : fileVariables) {
            final String shortName = fileVariable.getShortName();
            if (shortName.contains("(original,89GHz") ||
                    shortName.contains("Attitude_Data") ||
                    shortName.contains("Position_in_Orbit") ||
                    shortName.contains("Navigation_Data") ||
                    shortName.contains("Latitude_of_Observation_Point") ||  // we add the relevant sub-sampled arrays during file-open tb 2018-01-16
                    shortName.contains("Longitude_of_Observation_Point") ||
                    shortName.contains("Land_Ocean_Flag_6_to_36") || // we add this during open tb 2018-01-17
                    shortName.contains("Pixel_Data_Quality_6_to_36") || // we add this during open tb 2018-01-17
                    shortName.contains("Pixel_Data_Quality_89") ||
                    shortName.contains("Scan_Data_Quality") ||   // we may need this tb 2018-01-16
                    shortName.contains("Land_Ocean_Flag_89")) {
                continue;
            }

            variables.add(fileVariable);
        }

        final List<Variable> injectedVariables = arrayCache.getInjectedVariables();
        variables.addAll(injectedVariables);

        return variables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        final Array longitudes = arrayCache.get(LON_VARIABLE_NAME);
        final int[] shape = longitudes.getShape();
        return new Dimension("size", shape[1], shape[0]);
    }

    @Override
    public String getLongitudeVariableName() {
        return LON_VARIABLE_NAME;
    }

    @Override
    public String getLatitudeVariableName() {
        return LAT_VARIABLE_NAME;
    }

    public Array readScanDataQuality(int scanNumber) throws IOException, InvalidRangeException {
        final Array scanDataQuality = arrayCache.get("Scan_Data_Quality");

        final int[] origin = new int[]{scanNumber, 0};
        final int[] shape = new int[]{1, 512};

        return scanDataQuality.section(origin, shape).copy();
    }

    // package access for testing only tb 2018-01-15
    static ProductData.UTC getUtcDate(Attribute attribute) throws IOException {
        final String stringValue = attribute.getStringValue();

        final String utcString = stringValue.substring(0, stringValue.length() - 1);
        final String utcWithMicros = utcString.concat("000");
        try {
            return ProductData.UTC.parse(utcWithMicros, "yyyy-MM-dd'T'HH:mm:ss");
        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        }
    }

    private void setSensingTimes(AcquisitionInfo acquisitionInfo) throws IOException {
        final Attribute startDateTime = NetCDFUtils.getGlobalAttributeSafe("ObservationStartDateTime", netcdfFile);
        final Attribute endDateTime = NetCDFUtils.getGlobalAttributeSafe("ObservationEndDateTime", netcdfFile);
        acquisitionInfo.setSensingStart(getUtcDate(startDateTime).getAsDate());
        acquisitionInfo.setSensingStop(getUtcDate(endDateTime).getAsDate());
    }

    private BoundingPolygonCreator getBoundingPolygonCreator() {
        if (boundingPolygonCreator == null) {
            // @todo 2 tb/tb move intervals to config 2018-01-15
            boundingPolygonCreator = new BoundingPolygonCreator(new Interval(20, 100), readerContext.getGeometryFactory());
        }

        return boundingPolygonCreator;
    }

    private void setGeometries(AcquisitionInfo acquisitionInfo) throws IOException {
        final Array lonArray = arrayCache.get(LON_VARIABLE_NAME);
        final Array latArray = arrayCache.get(LAT_VARIABLE_NAME);

        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator();
        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(lonArray, latArray);
        if (!boundingGeometry.isValid()) {
            // @todo 2 tb/tb implement splitted polygon approach if we encounter failures here 2018-01-15
            throw new RuntimeException("Detected invalid bounding geometry");
        }
        acquisitionInfo.setBoundingGeometry(boundingGeometry);

        final LineString timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(lonArray, latArray);
        ReaderUtils.setTimeAxes(acquisitionInfo, timeAxisGeometry, readerContext.getGeometryFactory());
    }

    protected Number getFillValue(String variableName) throws IOException {
        final Number fillValue = arrayCache.getNumberAttributeValue(CF_FILL_VALUE_NAME, variableName);
        if (fillValue != null) {
            return fillValue;
        }

        if (variableName.equalsIgnoreCase("Area_Mean_Height")) {
            return -99999;
        } else if (variableName.contains("Brightness_Temperature")) {
            return 65535;
        } else if (variableName.contains("Earth_")) {
            return -32767;
        } else if (variableName.contains("Land_Ocean_Flag")) {
            return 255;
        }

        final Array array = arrayCache.get(variableName);
        return NetCDFUtils.getDefaultFillValue(array);
    }

    private void initializeVariables() throws IOException {
        arrayCache.inject(new GeolocationVariable(LON_VARIABLE_NAME, netcdfFile));
        arrayCache.inject(new GeolocationVariable(LAT_VARIABLE_NAME, netcdfFile));

        try {
            injectLandOceanFlagVariables();
        } catch (InvalidRangeException e) {
            throw new IOException(e.getMessage());
        }

        injectPixelDataQualityVariable();
    }

    private void injectPixelDataQualityVariable() {
        final Variable fileVariable = netcdfFile.findVariable("Pixel_Data_Quality_6_to_36");
        final PixelDataQualityVariable qualityVariable = new PixelDataQualityVariable(fileVariable);
        arrayCache.inject(qualityVariable);
    }

    private void injectLandOceanFlagVariables() throws InvalidRangeException {
        Variable fileVariable = netcdfFile.findVariable("Land_Ocean_Flag_6_to_36");
        final int[] shape = fileVariable.getShape();
        shape[0] = 1;   // pick a single layer
        final int[] origin = {0, 0, 0};
        final String variableNamePrefix = "Land_Ocean_Flag_";
        for (int i = 0; i < LAND_OCEAN_FLAG_EXTENSIONS.length; i++) {
            origin[0] = i;
            final Section section = new Section(origin, shape);
            final Variable channelVariable = fileVariable.section(section);
            channelVariable.setName(variableNamePrefix + LAND_OCEAN_FLAG_EXTENSIONS[i]);
            arrayCache.inject(channelVariable);
        }
    }
}
