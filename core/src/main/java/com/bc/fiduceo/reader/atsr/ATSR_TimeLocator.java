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

package com.bc.fiduceo.reader.atsr;


import com.bc.fiduceo.reader.TimeLocator;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.TimeCoding;

import java.util.Date;

class ATSR_TimeLocator implements TimeLocator {

    private static final double EPOCH_MJD2000 = 10957.0;
    private static final double MILLIS_PER_DAY = 86400000.0;

    private final TimeCoding timeCoding;
    private final PixelPos pixelPos;

    ATSR_TimeLocator(Product product) {
        timeCoding = product.getSceneTimeCoding();
        pixelPos = new PixelPos();
    }

    @Override
    public long getTimeFor(int x, int y) {
        pixelPos.setLocation(x + 0.5, y + 0.5);
        final double mjd = timeCoding.getMJD(pixelPos);
        return mjd2000ToDate(mjd).getTime();
    }

    // package access for testing only tb 2016-08-09
    static Date mjd2000ToDate(double mjd2000) {
        return new Date(Math.round((EPOCH_MJD2000 + mjd2000) * MILLIS_PER_DAY));
    }
}
