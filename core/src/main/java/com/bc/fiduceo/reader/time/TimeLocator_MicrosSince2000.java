package com.bc.fiduceo.reader.time;

import com.bc.fiduceo.util.TimeUtils;

public class TimeLocator_MicrosSince2000 implements TimeLocator {

    private final long[] timeStamps;

    public TimeLocator_MicrosSince2000(long[] timeStamps) {
        this.timeStamps = timeStamps;
    }

    @Override
    public long getTimeFor(int x, int y) {
        if (y < 0 || y >= timeStamps.length) {
            return -1;
        }
        final long timeStampSecs2000 = timeStamps[y];
        return TimeUtils.millisSince2000ToUnixEpoch(timeStampSecs2000);
    }
}
