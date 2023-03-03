package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.netcdf.StringVariable;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import org.esa.snap.core.util.io.FileUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;

abstract class NdbcReader implements Reader {

    static final String STATION_ID = "station_id";
    static final String STATION_TYPE = "station_type";
    static final String MEASUREMENT_TYPE = "measurement_type";
    static final String LONGITUDE = "longitude";
    static final String LATITUDE = "latitude";
    static final String ANEMOMETER_HEIGHT = "anemometer_height";
    static final String AIR_TEMP_HEIGHT = "air_temp_height";
    static final String BAROMETER_HEIGHT = "barometer_height";
    static final String SST_DEPTH = "sst_depth";

    static final String TIME = "time";
    static final String WDIR = "WDIR";
    static final String WSPD = "WSPD";
    static final String GST = "GST";

    protected Station station;

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
    public String getLongitudeVariableName() {
        return LONGITUDE;
    }

    @Override
    public String getLatitudeVariableName() {
        return LATITUDE;
    }

    StationDatabase parseStationDatabase(String resourceName) throws IOException {
        final InputStream is = getClass().getResourceAsStream(resourceName);
        if (is == null) {
            throw new IllegalStateException("The internal resource file could not be read.");
        }

        final StationDatabase sdb = new StationDatabase();
        sdb.load(is);

        is.close();

        return sdb;
    }

    void loadStation(File file, StationDatabase stationDatabase) {
        final String fileName = FileUtils.getFilenameWithoutExtension(file);
        final String stationId = fileName.substring(0, 5);
        station = stationDatabase.get(stationId);
        if (station == null) {
            throw new IllegalArgumentException("unsupported station, id = " + stationId);
        }
    }

    static byte toByte(StationType stationType) {
        switch (stationType) {
            case OCEAN_BUOY:
                return 0;
            case COAST_BUOY:
                return 1;
            case LAKE_BUOY:
                return 2;
            case OCEAN_STATION:
                return 3;
            case COAST_STATION:
                return 4;
            case LAKE_STATION:
                return 5;
            default:
                return -1;
        }
    }

    static byte toByte(MeasurementType measurementType) {
        switch (measurementType) {
            case CONSTANT_WIND:
                return 0;
            case STANDARD_METEOROLOGICAL:
                return 1;
            default:
                return -1;
        }
    }

    static void createBasicStationVariables(ArrayList<Variable> variables, List<Attribute> attributes) {
        attributes.add(new Attribute(CF_LONG_NAME, "Station identifier"));
        variables.add(new StringVariable(new VariableProxy(STATION_ID, DataType.STRING, attributes), 6));

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
    }

    static void createMeasurementTimeVariable(ArrayList<Variable> variables) {
        List<Attribute> attributes;
        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "seconds since 1970-01-01"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(int.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "time"));
        variables.add(new VariableProxy(TIME, DataType.INT, attributes));
    }

    static void createWindDirectionVariable(ArrayList<Variable> variables) {
        List<Attribute> attributes;
        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degT"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 999));
        attributes.add(new Attribute(CF_STANDARD_NAME, "wind_from_direction"));
        attributes.add(new Attribute(CF_LONG_NAME, "Ten-minute average wind direction measurements in degrees clockwise from true North."));
        variables.add(new VariableProxy(WDIR, DataType.SHORT, attributes));
    }

    static void createWindSpeedVariable(ArrayList<Variable> variables) {
        List<Attribute> attributes;
        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "m/s"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 99.f));
        attributes.add(new Attribute(CF_STANDARD_NAME, "wind_speed"));
        attributes.add(new Attribute(CF_LONG_NAME, "Ten-minute average wind speed values in m/s."));
        variables.add(new VariableProxy(WSPD, DataType.FLOAT, attributes));
    }

    static void createGustSpeedVariable(ArrayList<Variable> variables, String longName) {
        List<Attribute> attributes;
        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "m/s"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, 99.f));
        attributes.add(new Attribute(CF_STANDARD_NAME, "wind_gust_speed"));
        attributes.add(new Attribute(CF_LONG_NAME, longName));
        variables.add(new VariableProxy(GST, DataType.FLOAT, attributes));
    }


    protected Array createResultArray(Number value, Number fillValue, DataType dataType, Interval interval) {
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
