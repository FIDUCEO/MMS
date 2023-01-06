package com.bc.fiduceo.reader.time;

import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.Index;

import java.util.concurrent.TimeUnit;

public class TimeLocator_SecondsSince2000 implements TimeLocator {

    private final Array timeArray;
    private final int width;
    private final int height;
    private final double fillValue;

    public TimeLocator_SecondsSince2000(Array timeArray, double fillValue) {
        this.timeArray = timeArray;
        final int[] shape = timeArray.getShape();

        this.width = shape[1];
        this.height = shape[0];

        this.fillValue = fillValue;
    }

    @Override
    public long getTimeFor(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return -1;
        }

        final Index index = timeArray.getIndex();
        index.set(y, x);

        final double secsSince2K = timeArray.getDouble(index);
        if (secsSince2K == fillValue) {
            return -1;
        }

        return TimeUtils.secondsSince2000ToUnixEpoch(secsSince2K);
    }
}
