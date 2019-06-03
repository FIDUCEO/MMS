package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

interface Transform {

    Dimension getRasterSize();

    int mapCoordinate_X(int coordinate);

    int mapCoordinate_Y(int coordinate);

    int getOffset_X();

    int getOffset_Y();

    Interval mapInterval(Interval interval);

    Array process(Array array, double noDataValue) throws InvalidRangeException;

    Array processFlags(Array array, int noDataValue) throws InvalidRangeException;
}