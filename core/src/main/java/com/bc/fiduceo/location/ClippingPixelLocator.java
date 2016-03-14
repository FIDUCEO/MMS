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

public class ClippingPixelLocator implements PixelLocator {

    public final PixelLocator pixelLocator;
    public final int minY;
    public final int maxY;

    public ClippingPixelLocator(PixelLocator pixelLocator, int minY, int maxY) {
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
        if (pixelLocation.length > 1) {
            final Point2D p0 = pixelLocation[0];
            final int y = (int) Math.round(p0.getY());
            if (y >= minY && y <= maxY) {
                return new Point2D[]{p0};
            } else {
                return new Point2D[]{pixelLocation[1]};
            }
        } else {
            return pixelLocation;
        }
    }
}
