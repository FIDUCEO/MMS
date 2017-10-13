/*
 * Copyright (C) 2015 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface Reader extends AutoCloseable {

    /**
     * Opens the reader using the file passed in.
     *
     * @param file the file to open
     * @throws IOException on disk errors or other IO oddities
     */
    void open(File file) throws IOException;

    /**
     * Closes the reader again, releases all resources acquired.
     *
     * @throws IOException on file system problems
     */
    void close() throws IOException;

    /**
     * Extracts the acquisition information of the current product.
     *
     * @return the acquisitoin information
     * @throws IOException on reading errors
     */
    AcquisitionInfo read() throws IOException;

    /**
     * Returns the regular expression to be used for filtering files of the supported format from the file system.
     *
     * @return the regular expression
     */
    String getRegEx();

    /**
     * Returns a pixel locator object for the file currently opened. This pixel locator is
     * valid for the full satellite acquisition, i.e. it can return multiple (x,y) positions
     * for the same (lon, lat) in case of acquisition ambiguities or self-overlap.
     *
     * @return the pixel locator
     * @throws IOException on file system errors
     */
    PixelLocator getPixelLocator() throws IOException;

    /**
     * Returns a pixel locator optimized for the observation geometry passed in.
     *
     * @param sceneGeometry the area currently being investigated
     * @return the pixel locator
     * @throws IOException on file system errors
     */
    PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException;

    /**
     * Returns a time locator for the currently opened product.
     *
     * @return the time locator
     * @throws IOException ob file system errors
     */
    TimeLocator getTimeLocator() throws IOException;

    int[] extractYearMonthDayFromFilename(String fileName);

    /**
     * Reads raw data of a window defined by a center pixel position and a defined window size.
     * According to this constraints the window dimensions must always be odd. If not
     * an IllegalArgumentException will be thrown. In the case where parts of the window are out
     * of the border, the outside array positions are filled with the fill value defined by the
     * product.
     *
     * @param centerX      the center x position.
     * @param centerY      the center y position.
     * @param interval     the window sizes.
     * @param variableName the name of the data variable.
     * @return a data Array containing the data of the defined window.
     * @throws IOException
     * @throws InvalidRangeException
     */
    Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException;

    /**
     * Reads data of a window defined by a center pixel position and a defined window size.
     * Scaling factor and offset are applied, if present. If not, this method returns the same data as readRaw().
     * According to this constraints the window dimensions must always be odd. If not
     * an IllegalArgumentException will be thrown. In the case where parts of the window are out
     * of the border, the outside array positions are filled with the fill value defined by the
     * product.
     *
     * @param centerX      the center x position.
     * @param centerY      the center y position.
     * @param interval     the window sizes.
     * @param variableName the name of the data variable.
     * @return a data Array containing the data of the defined window.
     * @throws IOException
     * @throws InvalidRangeException
     */
    Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException;

    /**
     * Reads an {@link Array} of acquisition time values per pixel.
     * The unit value is seconds since 1970-01-01 00:00:00.
     *
     * @param x        the center x position of the window reading process.
     * @param y        the center y position of the window reading process.
     * @param interval the window sizes.
     * @return an {@link Array} of acquisition time values per pixel. Pixels not containing a valid acquisition time
     * are set to -2147483647 (the NetCDF default fill value for integer)
     */
    ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException;

    /**
     * Retrieves a {@link List list} of {@link Variable variables}.
     * It is mandatory, that all the number data type variables has a CF conform <code>_FillValue</code> set.
     * If a fill value attribute already exist, but not with a CF conform name, the attribute must be
     * duplicated and the CF conform name must be set.
     *
     * @return a {@link List list} of {@link Variable variables}.
     * @throws InvalidRangeException
     * @throws IOException
     */
    List<Variable> getVariables() throws InvalidRangeException, IOException;

    /**
     * Retieves the width and height of the product measurement data.
     *
     * @return the data Dimension
     */
    Dimension getProductSize() throws IOException;

    String getLongitudeVariableName();

    String getLatitudeVariableName();
}
