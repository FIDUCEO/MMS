package com.bc.fiduceo.matchup.writer;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.Reader;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;

import java.io.IOException;

public class StringWritingIOVariable extends ReaderIOVariable {

    private final int stringLength;

    public StringWritingIOVariable(ReaderContainer readerContainer, int stringLength) {
        super(readerContainer);
        setDataType(DataType.CHAR.toString());
        this.stringLength = stringLength;
    }

    @Override
    public void writeData(int centerX, int centerY, Interval interval, int zIndex) throws IOException, InvalidRangeException {
        final Reader reader = readerContainer.getReader();
        final Array array = reader.readRaw(centerX, centerY, interval, sourceVariableName);
        final char[] stringChars = (char[]) array.get1DJavaArray(DataType.CHAR);
        target.write(new String(stringChars), targetVariableName, zIndex);
    }

    @Override
    public boolean hasCustomDimension() {
        return true;
    }

    @Override
    public Dimension getCustomDimension() {
        return new Dimension(targetVariableName + "_dim", stringLength);
    }

    @Override
    public String getDimensionNames() {
        return FiduceoConstants.MATCHUP_COUNT + " " + targetVariableName + "_dim";
    }
}
