package com.bc.fiduceo.reader.insitu;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.TimeLocator;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;

public class SecsSince1970TimeLocator implements TimeLocator {

    private final Reader reader;
    private final Interval interval;

    public SecsSince1970TimeLocator(Reader reader) {
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
