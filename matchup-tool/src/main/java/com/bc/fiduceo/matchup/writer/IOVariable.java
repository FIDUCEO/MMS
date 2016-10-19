package com.bc.fiduceo.matchup.writer;

import com.bc.fiduceo.core.Interval;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;

import java.io.IOException;
import java.util.List;

public interface IOVariable {

    void setTarget(Target target);

    void writeData(int centerX, int centerY, Interval interval, int zIndex) throws IOException, InvalidRangeException;

    String getSourceVariableName();

    List<Attribute> getAttributes();

    String getDataType();

    String getDimensionNames();

    String getTargetVariableName();

    void setTargetVariableName(String name);
}
