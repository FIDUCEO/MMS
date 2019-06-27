package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import ucar.ma2.Array;

import java.io.IOException;

interface Transform {

    Dimension getRasterSize();

    int mapCoordinate_X(int coordinate);

    int mapCoordinate_Y(int coordinate);

    int getOffset();

    Interval mapInterval(Interval interval);

    Array process(Array array, double noDataValue) throws IOException;

    Array processFlags(Array array, int noDataValue) throws IOException;
}
