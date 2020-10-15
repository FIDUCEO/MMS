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

package com.bc.fiduceo.reader.snap;


import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.TimeCoding;

public class SNAP_TimeLocator implements TimeLocator {

    private final TimeCoding timeCoding;
    private final PixelPos pixelPos;

    public SNAP_TimeLocator(Product product) {
        timeCoding = product.getSceneTimeCoding();
        pixelPos = new PixelPos();
    }

    @Override
    public long getTimeFor(int x, int y) {
        pixelPos.setLocation(x + 0.5, y + 0.5);
        final double mjd = timeCoding.getMJD(pixelPos);
        return TimeUtils.mjd2000ToDate(mjd).getTime();
    }
}
