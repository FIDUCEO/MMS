package com.bc.fiduceo.reader.insitu;

import com.bc.fiduceo.core.Interval;
import ucar.ma2.Array;

public class InsituUtils {

    public static Array getResultArray(int centerY, Interval interval, Array sourceArray, Number fillValue) {
        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();
        final int windowCenterX = windowWidth / 2;
        final int windowCenterY = windowHeight / 2;

        final int[] shape = {windowWidth, windowHeight};
        final Array windowArray = Array.factory(sourceArray.getDataType(), shape);
        for (int y = 0; y < windowHeight; y++) {
            for (int x = 0; x < windowWidth; x++) {
                windowArray.setObject(windowWidth * y + x, fillValue);
            }
        }
        windowArray.setObject(windowWidth * windowCenterY + windowCenterX, sourceArray.getObject(centerY));
        return windowArray;
    }
}
