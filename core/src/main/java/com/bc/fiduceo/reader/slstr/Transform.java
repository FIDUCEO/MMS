package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import ucar.ma2.Array;

import java.io.IOException;

interface Transform {

    Dimension getRasterSize();

    double mapCoordinate_X(double coordinate);

    double mapCoordinate_Y(double coordinate);

    double inverseCoordinate_X(double coordinate);

    double inverseCoordinate_Y(double coordinate);

    int getOffset();

    Interval mapInterval(Interval interval);

    Array process(Array array, double noDataValue) throws IOException;

    Array processFlags(Array array, int noDataValue) throws IOException;
}
