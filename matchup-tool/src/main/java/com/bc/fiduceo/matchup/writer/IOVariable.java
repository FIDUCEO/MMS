/*
 * Copyright (C) 2016 Brockmann Consult GmbH
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

package com.bc.fiduceo.matchup.writer;

import com.bc.fiduceo.core.Interval;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

import java.io.IOException;
import java.util.List;

public interface IOVariable {

    void setTarget(Target target);

    void writeData(int centerX, int centerY, Interval interval, int zIndex) throws IOException, InvalidRangeException;

    String getSourceVariableName();

    List<Attribute> getAttributes();

    String getDataType();

    void setDataType(String dataType);

    String getDimensionNames();

    /**
     * Sets the dimension names for this variable. The String must be composed of a blank separated list of dimensions names.
     * The order of the dimensions is "z y x".
     *
     * @param dimensionNames the zero separated dimension names
     */
    void setDimensionNames(String dimensionNames);

    String getTargetVariableName();

    void setTargetVariableName(String name);

    boolean hasCustomDimension();

    Dimension getCustomDimension();
}
