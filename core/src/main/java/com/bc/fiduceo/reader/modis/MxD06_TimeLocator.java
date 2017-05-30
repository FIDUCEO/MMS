package com.bc.fiduceo.reader.modis;


import com.bc.fiduceo.reader.TimeLocator;
import ucar.ma2.Array;

public class MxD06_TimeLocator implements TimeLocator {

    MxD06_TimeLocator(Array section) {

    }

    @Override
    public long getTimeFor(int x, int y) {
        throw new RuntimeException("not implemented");
    }
}
