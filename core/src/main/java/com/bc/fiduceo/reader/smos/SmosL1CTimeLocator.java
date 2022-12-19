package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;

import java.util.Date;

class SmosL1CTimeLocator implements TimeLocator {
    private final Array days;
    private final Array seconds;
    private final Array micros;
    private final int maxX;
    private final int maxY;
    private final long outFillValue;
    private final int inFillValue;

    SmosL1CTimeLocator(Array days, Array seconds, Array micros) {
        this.days = days;
        this.seconds = seconds;
        this.micros = micros;

        int[] shape = days.getShape();
        maxX = shape[1];
        maxY = shape[0];

        outFillValue = -1L;
        inFillValue = (int) NetCDFUtils.getDefaultFillValue(DataType.INT, false);
    }

    @Override
    public long getTimeFor(int x, int y) {
        if (x < 0 | x >= maxX | y < 0 | y >= maxY) {
            return outFillValue;
        }

        final Index index = days.getIndex();
        index.set(y, x);
        final int day = days.getInt(index);
        final int second = seconds.getInt(index);
        final int micro = micros.getInt(index);
        if (day == inFillValue | second == inFillValue | micro == inFillValue) {
            return outFillValue;
        }

        final Date date = SmosL1CDailyGriddedReader.cfiDateToUtc(day, second, micro);
        return date.getTime();
    }
}
