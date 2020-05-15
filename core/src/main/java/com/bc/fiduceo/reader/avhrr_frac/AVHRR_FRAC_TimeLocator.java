package com.bc.fiduceo.reader.avhrr_frac;

import com.bc.fiduceo.reader.time.TimeLocator;

import java.util.Date;

class AVHRR_FRAC_TimeLocator implements TimeLocator {

    private final long startTime;
    private final double increment;

    AVHRR_FRAC_TimeLocator(Date startTime, Date stopTime, int numLines) {
        this.startTime = startTime.getTime();
        double timeDelta = (stopTime.getTime() - this.startTime);
        increment = timeDelta / (numLines - 1);
    }

    @Override
    public long getTimeFor(int x, int y) {
        return Math.round((startTime + y * increment));
    }
}
