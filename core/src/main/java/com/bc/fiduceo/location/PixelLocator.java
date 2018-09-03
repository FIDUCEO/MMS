package com.bc.fiduceo.location;
/*
 * Copyright (C) 2016 Brockmann Consult GmbH (info@brockmann-consult.de)
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
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

import java.awt.geom.Point2D;

/**
 * An algorithm for finding the pixel position that corresponds to a given geo-location
 * and vice versa.
 *
 * @author Sabine Embacher
 */
public interface PixelLocator {

    /**
     * Returns the (lon, lat) geo-location that corresponds to a given (x, y) pixel
     * location. The lon value as X and the lat value as Y. The pixel location uses the
     * upper left pixel corner as reference; to refer to pixel-center, set x+0.5 and y+0.5.
     *
     * @param x The pixel x location.
     * @param y The pixel y location.
     * @param g The geo-location object. It will be used to return the geo-location. If {@code null} a new instance will be created.
     * @return Point2D if a geo-location was found, {@code null} otherwise.
     */
    Point2D getGeoLocation(double x, double y, Point2D g);

    /**
     * Returns an array of (x, y) pixel location that corresponds to a given (lon, lat) geo-location.
     * The pixel location uses the upper left pixel corner as reference..
     *
     * @param lon The pixel longitude [-180.0, 180.0].
     * @param lat The pixel latitude [-90.0, 90.0].
     * @return an array of points if one or two pixel locations was found, an empty array otherwise.
     */
    Point2D[] getPixelLocation(double lon, double lat);
}
