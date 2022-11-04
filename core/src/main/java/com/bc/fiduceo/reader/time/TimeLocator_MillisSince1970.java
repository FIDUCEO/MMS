package com.bc.fiduceo.reader.time;

public class TimeLocator_MillisSince1970 implements TimeLocator{

    private final long[] times;

    public TimeLocator_MillisSince1970(long[] times) {
        this.times = times;
    }

    @Override
    public long getTimeFor(int x, int y) {
        if (y < 0 || y >= times.length) {
            return -1;
        }

        return times[y];
    }
}
