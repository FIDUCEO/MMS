package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.Interval;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.File;
import java.io.IOException;

public interface RawDataSource extends AutoCloseable {

    void open(File file) throws IOException;

    void close() throws IOException;

    /**
     * Reads raw data of a window defined by a center pixel position an a defined window size.
     * According to this constraints the window dimensions must always be odd. If not
     * an IllegalArgumentException will be thrown. In the case where parts of the window are out
     * of the border, the outside array positions are filled with the fill value defined by the
     * product.
     *
     * @param centerX      the center x position.
     * @param centerY      the center y position.
     * @param interval     the window sizes.
     * @param variableName the name of the data variable.
     *
     * @return a data Array containing the data of the defined window.
     *
     * @throws IOException
     * @throws InvalidRangeException
     */
    Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException;

}
