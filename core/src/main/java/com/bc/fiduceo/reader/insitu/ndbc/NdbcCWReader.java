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

class NdbcCWReader extends NdbcReader {

    private static final String GTIME = "GTIME";
    private static final String REG_EX_CW = "\\w{5}c\\d{4}.txt";

    private static final String GDR = "GDR";

    private static StationDatabase stationDatabase;

    private ArrayList<CwRecord> records;
    private TimeLocator timeLocator;

    @Override
    public void open(File file) throws IOException {
        ensureStationDatabase();
        loadStation(file, stationDatabase);
        parseFile(file);
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
        station = null;
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

        createBasicStationVariables(variables, attributes);

        // measurement record variables ----------------------------------
        createMeasurementTimeVariable(variables);
        createWindDirectionVariable(variables);
        createWindSpeedVariable(variables);

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degT"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 999));
        attributes.add(new Attribute(CF_STANDARD_NAME, "wind_gust_from_direction"));
        attributes.add(new Attribute(CF_LONG_NAME, "Direction, in degrees clockwise from true North, of the GST, reported at the last hourly 10-minute segment."));
        variables.add(new VariableProxy(GDR, DataType.SHORT, attributes));

        createGustSpeedVariable(variables, "Maximum 5-second peak gust during the measurement hour, reported at the last hourly 10-minute segment.");

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
