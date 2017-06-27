/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.fiduceo.reader;

import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.math.SphericalDistance;
import org.esa.snap.core.util.math.RsMathUtils;
import ucar.ma2.Array;

import java.awt.geom.Point2D;

public class PixelLocatorX1Yn implements PixelLocator {

    private static final double MEAN_EARTH_RADIUS_IN_KM = RsMathUtils.MEAN_EARTH_RADIUS * 0.001;
    private final int maxY;
    private final double maxDistanceKm;
    private final Array lons;
    private final Array lats;

    public PixelLocatorX1Yn(double maxDistanceKm, Array lons, Array lats) {
        this.maxDistanceKm = maxDistanceKm;
        final long size = lons.getSize();
        if (size > Integer.MAX_VALUE) {
            throw new RuntimeException("The number of elements in an array must be less or equal the integer maximum value 2147483647 = 0x7fffffff = (2^31)-1.");
        }
        if (size != lats.getSize()) {
            throw new RuntimeException("The arrays lons and lats must have the same number of elements.");
        }
        this.maxY = (int) size;
        this.lons = lons;
        this.lats = lats;
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D g) {
        if (x < 0 || x > 1) {
            throw new RuntimeException("Invalid x value. Must be in the range >=0 and <=1.");
        }
        if (y < 0 || y > maxY) {
            throw new RuntimeException("Invalid y value. Must be in the range >=0 and <=" + maxY + ".");
        }
        final int idx = Math.min((int) Math.floor(y), maxY - 1);
        return new Point2D.Double(lons.getDouble(idx), lats.getDouble(idx));
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {
        final SphericalDistance sphericalDistance = new SphericalDistance(lon, lat);

        int smallestIDX = -1;
        double smallestDistKm = Double.MAX_VALUE;
        for (int i = 0; i < maxY; i++) {
            final double iLon = lons.getDouble(i);
            final double iLat = lats.getDouble(i);
            final double distKm = sphericalDistance.distance(iLon, iLat) * MEAN_EARTH_RADIUS_IN_KM;
            if (distKm < smallestDistKm) {
                smallestDistKm = distKm;
                smallestIDX = i;
            }
        }
        if (smallestDistKm <= maxDistanceKm) {
            return new Point2D.Double[]{new Point2D.Double(0.5, smallestIDX + 0.5)};
        } else {
            return null;
        }
    }
}
