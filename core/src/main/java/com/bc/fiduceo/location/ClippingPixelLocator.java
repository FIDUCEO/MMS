/*
 * $Id$
 *
 * Copyright (C) 2016 by Brockmann Consult (info@brockmann-consult.de)
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
package com.bc.fiduceo.location;

import java.awt.geom.Point2D;
import java.util.ArrayList;

class ClippingPixelLocator implements PixelLocator {

    final PixelLocator pixelLocator;
    final int minY;
    final int maxY;

    ClippingPixelLocator(PixelLocator pixelLocator, int minY, int maxY) {
        this.pixelLocator = pixelLocator;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D g) {
        return pixelLocator.getGeoLocation(x, y, g);
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {
        final Point2D[] pixelLocation = pixelLocator.getPixelLocation(lon, lat);
        final ArrayList<Point2D> points = new ArrayList<>();
        for (Point2D point2D : pixelLocation) {
            final int y = (int) Math.floor(point2D.getY());
            if (y >= minY && y <= maxY) {
                points.add(point2D);
            }
        }
        return points.toArray(new Point2D[points.size()]);
    }
}
