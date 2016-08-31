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


import com.bc.fiduceo.location.PixelLocator;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;

import java.awt.geom.Point2D;

class ATSR_PixelLocator implements PixelLocator {

    private final GeoCoding geoCoding;
    private final PixelPos pixelPos;
    private final GeoPos geoPos;

    ATSR_PixelLocator(GeoCoding geoCoding) {
        this.geoCoding = geoCoding;
        this.pixelPos = new PixelPos();
        this.geoPos = new GeoPos();
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D point) {
        pixelPos.setLocation(x, y);
        final GeoPos geoPos = new GeoPos();
        final GeoPos geoCodingGeoPos = geoCoding.getGeoPos(pixelPos, geoPos);

        if (point == null) {
            point = new Point2D.Double();
        }

        point.setLocation(geoCodingGeoPos.getLon(), geoCodingGeoPos.getLat());
        return point;
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {
        geoPos.setLocation(lat, lon);
        final PixelPos newPos = geoCoding.getPixelPos(geoPos, pixelPos);
        if (Double.isNaN(newPos.getX()) || Double.isNaN(newPos.getY())) {
            return new Point2D[0];
        }

        final Point2D.Double pxPos = new Point2D.Double(newPos.getX(), newPos.getY());
        return new Point2D[]{pxPos};
    }
}
