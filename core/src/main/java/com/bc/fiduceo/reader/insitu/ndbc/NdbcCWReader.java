package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_MillisSince1970;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import com.bc.fiduceo.util.VariableProxy;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.io.FileUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;

class NdbcCWReader extends NdbcReader {

    private static final String GST = "GST";
    private static final String MEASUREMENT_TYPE = "measurement_type";
    private static final String ANEMOMETER_HEIGHT = "anemometer_height";
    private static final String SST_DEPTH = "sst_depth";
    private static final String WSPD = "WSPD";
    private static final String GTIME = "GTIME";
    private static final String REG_EX_CW = "\\w{5}c\\d{4}.txt";
    private static final String STATION_ID = "station_id";
    private static final String STATION_TYPE = "station_type";
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String BAROMETER_HEIGHT = "barometer_height";
    private static final String WDIR = "WDIR";
    private static final String AIR_TEMP_HEIGHT = "air_temp_height";
    private static final String TIME = "time";
    private static final String GDR = "GDR";

    private static StationDatabase stationDatabase;

    private ArrayList<CwRecord> records;
    private TimeLocator timeLocator;
    private Station station;

    @Override
    public void open(File file) throws IOException {
        ensureStationDatabase();
        loadStation(file);
        parseFile(file);
    }

    private void loadStation(File file) {
        final String fileName = FileUtils.getFilenameWithoutExtension(file);
        final String stationId = fileName.substring(0, 5);
        station = stationDatabase.get(stationId);
        if (station == null) {
            throw new IllegalArgumentException("unsupported station, id = " + stationId);
        }
    }

    private void parseFile(File file) throws IOException {
        records = new ArrayList<>();
        try (final FileReader fileReader = new FileReader(file)) {
            final Calendar calendar = TimeUtils.getUTCCalendar();
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#")) {
                    // skip comment lines tb 2023-02-27
                    continue;
                }

                final CwRecord cwRecord = parseLine(line, calendar);
                records.add(cwRecord);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (records != null) {
            records.clear();
            records = null;
        }

        timeLocator = null;
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        int minTime = Integer.MAX_VALUE;
        int maxTime = Integer.MIN_VALUE;
        for (final CwRecord record : records) {
            if (record.utc < minTime) {
                minTime = record.utc;
            }
            if (record.utc > maxTime) {
                maxTime = record.utc;
            }
        }

        acquisitionInfo.setSensingStart(new Date(minTime * 1000L));
        acquisitionInfo.setSensingStop(new Date(maxTime * 1000L));

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX_CW;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        throw new RuntimeException("not implemented");  // intentional tb 2023-02-27
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new RuntimeException("not implemented");  // intentional tb 2023-02-27
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        if (timeLocator == null) {
            createTimeLocator();
        }

        return timeLocator;
    }

    private void createTimeLocator() {
        long[] timeArray = new long[records.size()];

        int i = 0;
        for (final CwRecord record : records) {
            timeArray[i] = record.utc * 1000L;
            i++;
        }

        timeLocator = new TimeLocator_MillisSince1970(timeArray);
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        int[] ymd = new int[3];
        final int dotIndex = fileName.indexOf('.');
        final String yearString = fileName.substring(dotIndex - 4, dotIndex);
        ymd[0] = Integer.parseInt(yearString);
        ymd[1] = 1;
        ymd[2] = 1;
        return ymd;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final CwRecord record = records.get(centerY);

        switch (variableName) {
            case STATION_ID:
                final Array resultArray = Array.factory(DataType.STRING, new int[]{1, 1});
                resultArray.setObject(0, station.getId());
                return resultArray;
            case STATION_TYPE:
                final StationType type = station.getType();
                return createResultArray(toByte(type), -1, DataType.BYTE, interval);
            case MEASUREMENT_TYPE:
                final MeasurementType measurementType = station.getMeasurementType();
                return createResultArray(toByte(measurementType), -1, DataType.BYTE, interval);
            case LONGITUDE:
                return createResultArray(station.getLon(), Float.NaN, DataType.FLOAT, interval);
            case LATITUDE:
                return createResultArray(station.getLat(), Float.NaN, DataType.FLOAT, interval);
            case ANEMOMETER_HEIGHT:
                return createResultArray(station.getAnemometerHeight(), Float.NaN, DataType.FLOAT, interval);
            case AIR_TEMP_HEIGHT:
                return createResultArray(station.getAirTemperatureHeight(), Float.NaN, DataType.FLOAT, interval);
            case BAROMETER_HEIGHT:
                return createResultArray(station.getBarometerHeight(), Float.NaN, DataType.FLOAT, interval);
            case SST_DEPTH:
                return createResultArray(station.getSSTDepth(), Float.NaN, DataType.FLOAT, interval);
            case TIME:
                return createResultArray(record.utc, NetCDFUtils.getDefaultFillValue(int.class), DataType.INT, interval);
            case WDIR:
                return createResultArray(record.windDir, 999, DataType.SHORT, interval);
            case WSPD:
                return createResultArray(record.windSpeed, 99.f, DataType.FLOAT, interval);
            case GST:
                return createResultArray(record.gustSpeed, 99.f, DataType.FLOAT, interval);
            case GDR:
                return createResultArray(record.gustDir, 999, DataType.SHORT, interval);
            case GTIME:
                return createResultArray(record.gustTime, 9999, DataType.SHORT, interval);
        }

        return null;
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return readRaw(centerX, centerY, interval, variableName);   // nothing to scale here tb 2023-02-28
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        final Array timeArray = readRaw(x, y, interval, TIME);

        return (ArrayInt.D2) timeArray;
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        final ArrayList<Variable> variables = new ArrayList<>();

        List<Attribute> attributes = new ArrayList<>();
        // station variables
        attributes.add(new Attribute(CF_LONG_NAME, "Station identifier"));
        variables.add(new VariableProxy(STATION_ID, DataType.STRING, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_LONG_NAME, "Station type. 0: OCEAN_BUOY, 1: COAST_BUOY, 2: LAKE_BUOY, 3: OCEAN_STATION, 4: COAST_STATION, 5: LAKE_STATION"));
        variables.add(new VariableProxy(STATION_TYPE, DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_LONG_NAME, "Measurement type. 0: CONSTANT_WIND, 1: STANDARD_METEOROLOGICAL"));
        variables.add(new VariableProxy(MEASUREMENT_TYPE, DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_east"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "longitude"));
        variables.add(new VariableProxy(LONGITUDE, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_north"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "latitude"));
        variables.add(new VariableProxy(LATITUDE, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, Float.NaN));
        attributes.add(new Attribute(CF_LONG_NAME, "Height of instrument above site elevation"));
        variables.add(new VariableProxy(ANEMOMETER_HEIGHT, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, Float.NaN));
        attributes.add(new Attribute(CF_LONG_NAME, "Height of instrument above site elevation"));
        variables.add(new VariableProxy(AIR_TEMP_HEIGHT, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, Float.NaN));
        attributes.add(new Attribute(CF_LONG_NAME, "Height of instrument above above mean sea level"));
        variables.add(new VariableProxy(BAROMETER_HEIGHT, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, Float.NaN));
        attributes.add(new Attribute(CF_LONG_NAME, "Depth of instrument below water line"));
        variables.add(new VariableProxy(SST_DEPTH, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "seconds since 1970-01-01"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(int.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "time"));
        variables.add(new VariableProxy(TIME, DataType.INT, attributes));

        // measurement record variables
        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degT"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 999));
        attributes.add(new Attribute(CF_STANDARD_NAME, "wind_from_direction"));
        attributes.add(new Attribute(CF_LONG_NAME, "Ten-minute average wind direction measurements in degrees clockwise from true North."));
        variables.add(new VariableProxy(WDIR, DataType.SHORT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "m/s"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 99.f));
        attributes.add(new Attribute(CF_STANDARD_NAME, "wind_speed"));
        attributes.add(new Attribute(CF_LONG_NAME, "Ten-minute average wind speed values in m/s."));
        variables.add(new VariableProxy(WSPD, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degT"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 999));
        attributes.add(new Attribute(CF_STANDARD_NAME, "wind_gust_from_direction"));
        attributes.add(new Attribute(CF_LONG_NAME, "Direction, in degrees clockwise from true North, of the GST, reported at the last hourly 10-minute segment."));
        variables.add(new VariableProxy(GDR, DataType.SHORT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "m/s"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 99.f));
        attributes.add(new Attribute(CF_STANDARD_NAME, "wind_gust_speed"));
        attributes.add(new Attribute(CF_LONG_NAME, "Maximum 5-second peak gust during the measurement hour, reported at the last hourly 10-minute segment."));
        variables.add(new VariableProxy(GST, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "hhmm"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 9999));
        attributes.add(new Attribute(CF_LONG_NAME, "The minute of the hour that the GSP occurred, reported at the last hourly 10-minute segment."));
        variables.add(new VariableProxy(GTIME, DataType.SHORT, attributes));

        return variables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        return new Dimension("product_size", 1, records.size());
    }

    @Override
    public String getLongitudeVariableName() {
        return LONGITUDE;
    }

    @Override
    public String getLatitudeVariableName() {
        return LATITUDE;
    }

    private void ensureStationDatabase() throws IOException {
        if (stationDatabase == null) {
            stationDatabase = parseStationDatabase("buoy_locations_cw.txt");
        }
    }

    CwRecord parseLine(String line, Calendar calendar) {
        final CwRecord cwRecord = new CwRecord();

        line = line.replaceAll(" +", " "); // some fields are separated by two or more blanks (sigh) tb 2023-02-27
        final String[] tokens = StringUtils.split(line, new char[]{' '}, true);

        calendar.setTimeInMillis(0);
        calendar.set(Calendar.YEAR, Integer.parseInt(tokens[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(tokens[1]) - 1);  // calendar wants month zero-based tb 2023-02-27
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(tokens[2]));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(tokens[3]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(tokens[4]));
        cwRecord.utc = (int) (calendar.getTimeInMillis() * 0.001);

        cwRecord.windDir = Short.parseShort(tokens[5]);
        cwRecord.windSpeed = Float.parseFloat(tokens[6]);
        cwRecord.gustDir = Short.parseShort(tokens[7]);
        cwRecord.gustSpeed = Float.parseFloat(tokens[8]);
        cwRecord.gustTime = Short.parseShort(tokens[9]);

        return cwRecord;
    }
}
