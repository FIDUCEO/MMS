package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.io.IOException;
import java.io.InputStream;

abstract class NdbcReader implements Reader {

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
