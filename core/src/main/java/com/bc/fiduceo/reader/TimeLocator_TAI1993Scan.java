package com.bc.fiduceo.reader;

import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.Index;

public class TimeLocator_TAI1993Scan implements TimeLocator {

    private final Array taiVector;
    private final Index index;
    private final int linesPerScan;

    public TimeLocator_TAI1993Scan(Array taiVector, int linesPerScan) {
        this.taiVector = taiVector;
        index = taiVector.getIndex();
        this.linesPerScan = linesPerScan;
    }

    @Override
    public long getTimeFor(int x, int y) {
        final int yScan = y / linesPerScan;
        index.set(yScan);
        final double lineTaiSeconds = taiVector.getDouble(index);
        return TimeUtils.tai1993ToUtc(lineTaiSeconds).getTime();
    }
}
