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

package com.bc.fiduceo.location;

import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import org.esa.snap.core.util.math.CosineDistance;
import ucar.ma2.Array;

import java.awt.geom.Point2D;

public class PixelLocatorFactory {

    public static PixelLocator getClippingPixelLocator(PixelLocator pixelLocator, int minY, int maxY) {
        return new ClippingPixelLocator(pixelLocator, minY, maxY);
    }

    public static PixelLocator getSwathPixelLocator(Array longitudes, Array latitudes, int width, int height) {
        return new SwathPixelLocator(longitudes, latitudes, width, height);
    }

    public static PixelLocator getSubScenePixelLocator(Polygon subSceneGeometry, int width, int height, int subsetHeight, PixelLocator pixelLocator) {
        final Point centroid = subSceneGeometry.getCentroid();
        final double cLon = centroid.getLon();
        final double cLat = centroid.getLat();

        final int sh2 = subsetHeight / 2;

        final double centerX = width / 2 + 0.5;

        final Point2D g1 = pixelLocator.getGeoLocation(centerX, sh2 + 0.5, null);
        final Point2D g2 = pixelLocator.getGeoLocation(centerX, sh2 + subsetHeight + 0.5, null);
        final CosineDistance cd1 = new CosineDistance(g1.getX(), g1.getY());
        final CosineDistance cd2 = new CosineDistance(g2.getX(), g2.getY());
        final double d1 = cd1.distance(cLon, cLat);
        final double d2 = cd2.distance(cLon, cLat);

        final int minY;
        final int maxY;
        if (d1 < d2) {
            minY = 0;
            maxY = subsetHeight - 1;
        } else {
            minY = subsetHeight - 1;
            maxY = height - 1;
        }
        return getClippingPixelLocator(pixelLocator, minY, maxY);
    }
}
