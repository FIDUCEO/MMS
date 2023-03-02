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

class NdbcSMReader extends NdbcReader {

    private static final String REG_EX_SM = "\\w{5}h\\d{4}.txt";

    private static final String WVHT = "WVHT";
    private static final String DPD = "DPD";
    private static final String APD = "APD";
    private static final String MWD = "MWD";
    private static final String PRES  = "PRES";
    private static final String ATMP  = "ATMP";
    private static final String DEWP  = "DEWP";
    private static final String VIS  = "VIS";
    private static final String TIDE  = "TIDE";

    private static StationDatabase stationDatabase;

    private ArrayList<SmRecord> records;
    private TimeLocator timeLocator;

    @Override
    public void open(File file) throws IOException {
        ensureStationDatabase();
        loadStation(file, stationDatabase);
        parseFile(file);
    }

    @Override
    public void close() throws IOException {
        if (records != null) {
            records.clear();
            records = null;
        }

        station = null;
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        int minTime = Integer.MAX_VALUE;
        int maxTime = Integer.MIN_VALUE;
        for (final SmRecord record : records) {
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
        return REG_EX_SM;
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
            createTimeLocator();
        }

        return timeLocator;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final SmRecord record = records.get(centerY);

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
            case WVHT:
                return createResultArray(record.waveHeight, 99.f, DataType.FLOAT, interval);
            case DPD:
                return createResultArray(record.domWavePeriod, 99.f, DataType.FLOAT, interval);
            case APD:
                return createResultArray(record.avgWavePeriod, 99.f, DataType.FLOAT, interval);
            case MWD:
                return createResultArray(record.waveDir, 999, DataType.SHORT, interval);
            case PRES:
                return createResultArray(record.seaLevelPressure, 9999.f, DataType.FLOAT, interval);
            case ATMP:
                return createResultArray(record.airTemp, 999.f, DataType.FLOAT, interval);
            case DEWP:
                return createResultArray(record.dewPointTemp, 999.f, DataType.FLOAT, interval);
            case VIS:
                return createResultArray(record.visibility, 99.f, DataType.FLOAT, interval);
            case TIDE:
                return createResultArray(record.tideLevel, 99.f, DataType.FLOAT, interval);
        }

        return null;
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return readRaw(centerX, centerY, interval, variableName);
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
        // station variables -------------------------------------------------------------------
        createBasicStationVariables(variables, attributes);

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

        // measurement record variables ---------------------------------------------------------
        createMeasurementTimeVariable(variables);
        createWindDirectionVariable(variables);
        createWindSpeedVariable(variables);
        createGustSpeedVariable(variables, "Peak 5 or 8 second gust speed (m/s) measured during the eight-minute or two-minute period. The 5 or 8 second period can be determined by payload.");

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 99.f));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_surface_wave_significant_height"));
        attributes.add(new Attribute(CF_LONG_NAME, "Significant wave height (meters) is calculated as the average of the highest one-third of all of the wave heights during the 20-minute sampling period."));
        variables.add(new VariableProxy(WVHT, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "s"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 99.f));
        attributes.add(new Attribute(CF_LONG_NAME, "Dominant wave period (seconds) is the period with the maximum wave energy."));
        variables.add(new VariableProxy(DPD, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "s"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 99.f));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_surface_wave_mean_period"));
        attributes.add(new Attribute(CF_LONG_NAME, "Average wave period (seconds) of all waves during the 20-minute period."));
        variables.add(new VariableProxy(APD, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degT"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 999));
        attributes.add(new Attribute(CF_LONG_NAME, "The direction from which the waves at the dominant period (DPD) are coming. The units are degrees from true North, increasing clockwise, with North as 0 (zero) degrees and East as 90 degrees."));
        variables.add(new VariableProxy(MWD, DataType.SHORT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "hPa"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 9999.f));
        attributes.add(new Attribute(CF_STANDARD_NAME, "air_pressure_at_mean_sea_level"));
        attributes.add(new Attribute(CF_LONG_NAME, "Sea level pressure (hPa)."));
        variables.add(new VariableProxy(PRES, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degC"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 999.f));
        attributes.add(new Attribute(CF_STANDARD_NAME, "air_temperature"));
        attributes.add(new Attribute(CF_LONG_NAME, "Air temperature (Celsius). For sensor heights see variable 'air_temp_height'."));
        variables.add(new VariableProxy(ATMP, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degC"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 999.f));
        attributes.add(new Attribute(CF_STANDARD_NAME, "dew_point_temperature"));
        attributes.add(new Attribute(CF_LONG_NAME, "Dewpoint temperature taken at the same height as the air temperature measurement."));
        variables.add(new VariableProxy(DEWP, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "nmi"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 99.f));
        attributes.add(new Attribute(CF_STANDARD_NAME, "visibility_in_air"));
        attributes.add(new Attribute(CF_LONG_NAME, "Station visibility (nautical miles). Note that buoy stations are limited to reports from 0 to 1.6 nmi."));
        variables.add(new VariableProxy(VIS, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "ft"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 99.f));
        attributes.add(new Attribute(CF_LONG_NAME, "The water level in feet above or below Mean Lower Low Water (MLLW)."));
        variables.add(new VariableProxy(TIDE, DataType.FLOAT, attributes));

        return variables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        return new Dimension("product_size", 1, records.size());
    }

    private void ensureStationDatabase() throws IOException {
        if (stationDatabase == null) {
            stationDatabase = parseStationDatabase("buoy_locations_sm.txt");
        }
    }

    SmRecord parseLine(String line, Calendar calendar) {
        final SmRecord record = new SmRecord();

        line = line.replaceAll(" +", " "); // some fields are separated by two or more blanks (sigh) tb 2023-02-27
        final String[] tokens = StringUtils.split(line, new char[]{' '}, true);

        calendar.setTimeInMillis(0);
        calendar.set(Calendar.YEAR, Integer.parseInt(tokens[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(tokens[1]) - 1);  // calendar wants month zero-based tb 2023-02-27
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(tokens[2]));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(tokens[3]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(tokens[4]));
        record.utc = (int) (calendar.getTimeInMillis() * 0.001);

        record.windDir = Short.parseShort(tokens[5]);
        record.windSpeed = Float.parseFloat(tokens[6]);
        record.gustSpeed = Float.parseFloat(tokens[7]);
        record.waveHeight = Float.parseFloat(tokens[8]);
        record.domWavePeriod = Float.parseFloat(tokens[9]);
        record.avgWavePeriod = Float.parseFloat(tokens[10]);
        record.waveDir = Short.parseShort(tokens[11]);
        record.seaLevelPressure = Float.parseFloat(tokens[12]);
        record.airTemp = Float.parseFloat(tokens[13]);
        record.seaSurfTemp = Float.parseFloat(tokens[14]);
        record.dewPointTemp = Float.parseFloat(tokens[15]);
        record.visibility = Float.parseFloat(tokens[16]);
        record.tideLevel = Float.parseFloat(tokens[17]);

        return record;
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

                final SmRecord smRecord = parseLine(line, calendar);
                records.add(smRecord);
            }
        }
    }

    private void createTimeLocator() {
        long[] timeArray = new long[records.size()];

        int i = 0;
        for (final SmRecord record : records) {
            timeArray[i] = record.utc * 1000L;
            i++;
        }

        timeLocator = new TimeLocator_MillisSince1970(timeArray);
    }
}
