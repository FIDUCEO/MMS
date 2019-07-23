package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.util.TimeUtils;

import java.util.Calendar;

public class TimeLocator_MicrosSince2000 implements TimeLocator {

    private final long[] timeStamps;
    private static final long offset;

    static {
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.set(Calendar.YEAR, 2000);
        utcCalendar.set(Calendar.MONTH, 1);
        utcCalendar.set(Calendar.DAY_OF_MONTH, 1);
        utcCalendar.set(Calendar.HOUR_OF_DAY, 0);
        utcCalendar.set(Calendar.MINUTE, 0);
        utcCalendar.set(Calendar.SECOND, 0);
        utcCalendar.set(Calendar.MILLISECOND, 0);

        offset = utcCalendar.getTime().getTime();
    }

    TimeLocator_MicrosSince2000(long[] timeStamps) {
        this.timeStamps = timeStamps;
    }


    @Override
    public long getTimeFor(int x, int y) {
        final long timeStampSecs2000 = timeStamps[y];
        return convertToUnixEpochMillis(timeStampSecs2000);
    }

    static long convertToUnixEpochMillis(double timeStampSecs2000) {
        long timeStamp = Math.round(timeStampSecs2000 / 1000.0);
        return offset + timeStamp;
    }
}
