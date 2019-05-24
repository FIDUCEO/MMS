package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

interface Transform {

    Dimension getRasterSize();

    int mapCoordinate(int coordinate);

    Interval mapInterval(Interval interval);

    Array process(Array array, double noDataValue) throws InvalidRangeException;
}
