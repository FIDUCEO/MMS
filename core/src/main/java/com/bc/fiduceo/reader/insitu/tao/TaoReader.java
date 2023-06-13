package com.bc.fiduceo.reader.insitu.tao;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.netcdf.StringVariable;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_MillisSince1970;
import com.bc.fiduceo.util.NetCDFUtils;
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
import java.util.Date;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;

class TaoReader implements Reader {

    private final static String REG_EX = "(?:TAO|TRITON|RAMA|PIRATA)_\\w+_\\w+(-\\w+)??\\d{4}-\\d{2}.txt";
    private static final String TIME = "time";
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String SSS = "SSS";
    private static final float SSS_FILL = -9.999f;
    private static final String SST = "SST";
    private static final float SST_FILL = -9.999f;
    private static final String AIRT = "AIRT";
    private static final double AIRT_FILL = -9.99;
    private static final String RH = "RH";
    private static final double RH_FILL = -9.99;
    private static final String WSPD = "WSPD";
    private static final double WSPD_FILL = -99.9;
    private static final String WDIR = "WDIR";
    private static final double WDIR_FILL = -99.9;
    private static final String BARO = "BARO";
    private static final double BARO_FILL = -9.9;
    private static final String RAIN = "RAIN";
    private static final double RAIN_FILL = -9.99;
    private static final String Q = "Q";
    private static final String M = "M";

    private ArrayList<TaoRecord> records;
    private TimeLocator timeLocator;

    static TaoRecord parseLine(String line) {
        line = line.replaceAll(" +", " "); // ensure that we only have single blanks as separator tb 2023-04-28
        final String[] tokens = StringUtils.split(line, new char[]{' '}, true);

        final TaoRecord record = new TaoRecord();
        record.time = Integer.parseInt(tokens[0]);
        record.longitude = Float.parseFloat(tokens[1]);
        record.latitude = Float.parseFloat(tokens[2]);
        record.SSS = Float.parseFloat(tokens[3]);
        record.SST = Float.parseFloat(tokens[4]);
        record.AIRT = Float.parseFloat(tokens[5]);
        record.RH = Float.parseFloat(tokens[6]);
        record.WSPD = Float.parseFloat(tokens[7]);
        record.WDIR = Float.parseFloat(tokens[8]);
        record.BARO = Float.parseFloat(tokens[9]);
        record.RAIN = Float.parseFloat(tokens[10]);
        record.Q = Integer.parseInt(tokens[11]);
        record.M = tokens[12];

        return record;
    }

    @Override
    public void open(File file) throws IOException {
        try (final FileReader fileReader = new FileReader(file)) {
            records = new ArrayList<>();

            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#")) {
                    // skip comment lines tb 2023-04-28
                    continue;
                }

                final TaoRecord record = parseLine(line);
                records.add(record);
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
        for (final TaoRecord record : records) {
            if (record.time < minTime) {
                minTime = record.time;
            }
            if (record.time > maxTime) {
                maxTime = record.time;
            }
        }

        acquisitionInfo.setSensingStart(new Date(minTime * 1000L));
        acquisitionInfo.setSensingStop(new Date(maxTime * 1000L));

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX;
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
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final int endIdx = fileName.indexOf(".txt");
        final String yearString = fileName.substring(endIdx - 7, endIdx - 3);
        final String monthString = fileName.substring(endIdx - 2, endIdx);

        final int[] ymd = new int[3];
        ymd[0] = Integer.parseInt(yearString);
        ymd[1] = Integer.parseInt(monthString);
        ymd[2] = 1;
        return ymd;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final TaoRecord record = records.get(centerY);

        switch (variableName) {
            case TIME:
                return createResultArray(record.time, NetCDFUtils.getDefaultFillValue(DataType.INT, false), DataType.INT, interval);

            case LONGITUDE:
                return createResultArray(record.longitude, NetCDFUtils.getDefaultFillValue(DataType.FLOAT, false), DataType.FLOAT, interval);

            case LATITUDE:
                return createResultArray(record.latitude, NetCDFUtils.getDefaultFillValue(DataType.FLOAT, false), DataType.FLOAT, interval);

            case SSS:
                return createResultArray(record.SSS, SSS_FILL, DataType.FLOAT, interval);

            case SST:
                return createResultArray(record.SST, SST_FILL, DataType.FLOAT, interval);

            case AIRT:
                return createResultArray(record.AIRT, AIRT_FILL, DataType.FLOAT, interval);

            case RH:
                return createResultArray(record.RH, RH_FILL, DataType.FLOAT, interval);

            case WSPD:
                return createResultArray(record.WSPD, WSPD_FILL, DataType.FLOAT, interval);

            case WDIR:
                return createResultArray(record.WDIR, WDIR_FILL, DataType.FLOAT, interval);

            case BARO:
                return createResultArray(record.BARO, BARO_FILL, DataType.FLOAT, interval);

            case RAIN:
                return createResultArray(record.RAIN, RAIN_FILL, DataType.FLOAT, interval);

            case Q:
                return createResultArray(record.Q, NetCDFUtils.getDefaultFillValue(DataType.INT, false), DataType.INT, interval);
                
            case M:
                final Array resultArray = Array.factory(DataType.STRING, new int[]{1, 1});
                resultArray.setObject(0, record.M);
                return resultArray;
        }

        return null;
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return readRaw(centerX, centerY, interval, variableName);   // no scaled data in this product type tb 2023-05-02
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
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_east"));
        attributes.add(new Attribute(CF_STANDARD_NAME, "longitude"));
        variables.add(new VariableProxy(LONGITUDE, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_north"));
        attributes.add(new Attribute(CF_STANDARD_NAME, "latitude"));
        variables.add(new VariableProxy(LATITUDE, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "seconds since 1970-01-01"));
        attributes.add(new Attribute(CF_STANDARD_NAME, "time"));
        variables.add(new VariableProxy(TIME, DataType.INT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "psu"));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_surface_salinity"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, SSS_FILL));
        variables.add(new VariableProxy(SSS, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_Celsius"));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_surface_temperature"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, SST_FILL));
        variables.add(new VariableProxy(SST, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_Celsius"));
        attributes.add(new Attribute(CF_STANDARD_NAME, "air_temperature"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, AIRT_FILL));
        variables.add(new VariableProxy(AIRT, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "percent"));
        attributes.add(new Attribute(CF_STANDARD_NAME, "relative_humidity"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, RH_FILL));
        variables.add(new VariableProxy(RH, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "m/s"));
        attributes.add(new Attribute(CF_STANDARD_NAME, "wind_speed"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, WSPD_FILL));
        variables.add(new VariableProxy(WSPD, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degree"));
        attributes.add(new Attribute(CF_STANDARD_NAME, "wind_to_direction"));
        attributes.add(new Attribute(CF_LONG_NAME, "Wind To Direction degree true in Oceanographic Convention"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, WDIR_FILL));
        variables.add(new VariableProxy(WDIR, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "hPa"));
        attributes.add(new Attribute(CF_STANDARD_NAME, "air_pressure_at_mean_sea_level"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, BARO_FILL));
        variables.add(new VariableProxy(BARO, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "mm/hour"));
        attributes.add(new Attribute(CF_STANDARD_NAME, "rainfall_rate"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, RAIN_FILL));
        variables.add(new VariableProxy(RAIN, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_LONG_NAME, "Data Quality Codes"));
        variables.add(new VariableProxy(Q, DataType.INT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_LONG_NAME, "Data Mode Codes"));
        variables.add(new StringVariable(new VariableProxy(M, DataType.STRING, attributes), 8));

        return variables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        return new Dimension("product_size", 1, records.size());
    }

    @Override
    public String getLongitudeVariableName() {
        return "longitude";
    }

    @Override
    public String getLatitudeVariableName() {
        return "latitude";
    }

    private void createTimeLocator() {
        long[] timeArray = new long[records.size()];

        int i = 0;
        for (final TaoRecord record : records) {
            timeArray[i] = record.time * 1000L;
            i++;
        }

        timeLocator = new TimeLocator_MillisSince1970(timeArray);
    }

    // @todo 2 tb/tb create generic in-itu record and move this method to it tb 2023-05-02
    private Array createResultArray(Number value, Number fillValue, DataType dataType, Interval interval) {
        final int windowHeight = interval.getY();
        final int windowWidth = interval.getX();
        final Array windowArray = NetCDFUtils.create(dataType,
                new int[]{windowHeight, windowWidth},
                fillValue);

        final int windowCenterX = windowWidth / 2;
        final int windowCenterY = windowHeight / 2;
        windowArray.setObject(windowWidth * windowCenterY + windowCenterX, value);
        return windowArray;
    }
}
