package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import ucar.ma2.Array;

interface Transform {

    Dimension getRasterSize();

    int mapCoordinate(int coordinate);

    Interval mapInterval(Interval interval);

    Array process(Array array, double noDataValue);
}
