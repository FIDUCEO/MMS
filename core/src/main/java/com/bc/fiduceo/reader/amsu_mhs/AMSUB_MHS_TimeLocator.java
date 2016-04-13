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

package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.reader.TimeLocator;
import ucar.ma2.Array;

class AMSUB_MHS_TimeLocator implements TimeLocator {

    private final Array scnlinyr;
    private final Array scnlindy;
    private final Array scnlintime;

    public AMSUB_MHS_TimeLocator(Array scnlinyr, Array scnlindy, Array scnlintime) {
        this.scnlinyr = scnlinyr;
        this.scnlindy = scnlindy;
        this.scnlintime = scnlintime;
    }

    @Override
    public long getTimeFor(int x, int y) {
        final int year = scnlinyr.getInt(y);
        final int dayOfYear = scnlindy.getInt(y);
        final int millisInDay = scnlintime.getInt(y);
        return AMSUB_MHS_L1C_Reader.getDate(year, dayOfYear, millisInDay).getTime();
    }
}
