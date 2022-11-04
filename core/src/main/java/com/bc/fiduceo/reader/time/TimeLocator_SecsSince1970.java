package com.bc.fiduceo.reader.time;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.Reader;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;

public class TimeLocator_SecsSince1970 implements TimeLocator {

    private final Reader reader;
    private final Interval interval;

    public TimeLocator_SecsSince1970(Reader reader) {
        this.reader = reader;
        interval = new Interval(1, 1);
    }

    @Override
    public long getTimeFor(int x, int y) {
        try {
            final Array time = reader.readRaw(x, y, interval, "time");
            return time.getInt(0) * 1000L;
        } catch (IOException | InvalidRangeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
