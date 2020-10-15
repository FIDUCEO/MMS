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

package com.bc.fiduceo.reader.time;

import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;

public class TimeLocator_YearDoyMs implements TimeLocator {

    private final Array yearPerScanline;
    private final Array doyPerScanline;
    private final Array millisecondsPerScanline;

    public TimeLocator_YearDoyMs(Array yearPerScanline, Array doyPerScanline, Array millisecondsPerScanline) {
        this.yearPerScanline = yearPerScanline;
        this.doyPerScanline = doyPerScanline;
        this.millisecondsPerScanline = millisecondsPerScanline;
    }

    @Override
    public long getTimeFor(int x, int y) {
        final int year = yearPerScanline.getInt(y);
        final int dayOfYear = doyPerScanline.getInt(y);
        final int millisInDay = millisecondsPerScanline.getInt(y);
        return TimeUtils.getDate(year, dayOfYear, millisInDay).getTime();
    }
}
