package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.reader.TimeLocator;
import ucar.ma2.Array;
import ucar.ma2.Index;


class AVHRR_FCDR_TimeLocator implements TimeLocator {

    private final Array timeArray;

    AVHRR_FCDR_TimeLocator(Array timeArray) {
        this.timeArray = timeArray;
    }

    @Override
    public long getTimeFor(int x, int y) {
        final Index index = timeArray.getIndex();
        index.set(y);

        final double doubleTime = timeArray.getDouble(index);
        return Math.round(doubleTime * 1000.0);
    }
}
