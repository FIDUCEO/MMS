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
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;

public class WindowReadingIOVariable extends AbstractIOVariable {

    public WindowReadingIOVariable() {
        this(null);
    }

    WindowReadingIOVariable(RawDataSourceContainer rawDataSourceContainer) {
        super(rawDataSourceContainer);
    }

    @Override
    public void writeData(int centerX, int centerY, Interval interval, int zIndex) throws IOException, InvalidRangeException {
        final Array array = rawDataSourceContainer.getSource().readRaw(centerX, centerY, interval, sourceVariableName);
        target.write(array, targetVariableName, zIndex);
    }
}
