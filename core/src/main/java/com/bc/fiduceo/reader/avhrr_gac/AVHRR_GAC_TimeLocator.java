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

package com.bc.fiduceo.reader.avhrr_gac;

import com.bc.fiduceo.reader.TimeLocator;
import ucar.ma2.Array;
import ucar.ma2.Index;

import java.util.Date;

class AVHRR_GAC_TimeLocator implements TimeLocator {

    private final long referenceTime;
    private final Array dTime;

    AVHRR_GAC_TimeLocator(Array dTime, Date sensingStart) {
        referenceTime = sensingStart.getTime();
        this.dTime = dTime;
    }

    @Override
    public long getTimeFor(int x, int y) {
        final Index index = dTime.getIndex();
        index.set(0, y, x);

        final float timeDeltaInSecs = dTime.getFloat(index);
        final long timeDeltaInMillis = Math.round(timeDeltaInSecs * 1000);
        return referenceTime + timeDeltaInMillis;
    }
}
