package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.reader.TimeLocator;
import ucar.ma2.Array;
import ucar.ma2.Index;

class HIRS_FCDR_TimeLocator implements TimeLocator {

    private final Array time;
    private final double scaleFactor;
    private final double offset;

    HIRS_FCDR_TimeLocator(Array time, double scaleFactor, double offset) {
        this.time = time;
        this.scaleFactor = scaleFactor;
        this.offset = offset;
    }

    @Override
    public long getTimeFor(int x, int y) {
        final Index index = time.getIndex();

        index.set(y);
        final int rawTime = time.getInt(index);
        return Math.round((scaleFactor * rawTime + offset) * 1000.0);
    }
}
