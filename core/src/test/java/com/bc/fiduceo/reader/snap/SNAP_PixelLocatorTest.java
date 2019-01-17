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

import com.bc.fiduceo.reader.snap.SNAP_PixelLocator;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.dataop.maptransf.Datum;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.awt.geom.Point2D;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class SNAP_PixelLocatorTest {

    private SNAP_PixelLocator pixelLocator;
    private MockGeoCoding geoCoding;

    @Before
    public void setUp() {
        geoCoding = new MockGeoCoding();
        pixelLocator = new SNAP_PixelLocator(geoCoding);
    }

    @Test
    public void testGetGeoLocation() {
        final Point2D geoLocation = pixelLocator.getGeoLocation(45, 23, null);
        assertEquals(46.0, geoLocation.getX(), 1e-8);
        assertEquals(24.0, geoLocation.getY(), 1e-8);
    }

    @Test
    public void testGetGeoLocation_withRecyclePoint() {
        final Point2D.Double point = new Point2D.Double();

        final Point2D geoLocation = pixelLocator.getGeoLocation(46, 25, point);
        assertEquals(47.0, geoLocation.getX(), 1e-8);
        assertEquals(26.0, geoLocation.getY(), 1e-8);

        assertSame(point, geoLocation);
    }

    @Test
    public void testGetPixelLocation() {
        final Point2D[] pixelLocations = pixelLocator.getPixelLocation(18.4, 34.5);
        assertNotNull(pixelLocations);
        assertEquals(1, pixelLocations.length);

        assertEquals(17.4, pixelLocations[0].getX(), 1e-8);
        assertEquals(33.5, pixelLocations[0].getY(), 1e-8);
    }

    @Test
    public void testGetPixelLocation_outsideRaster() {
        geoCoding.setReturnInvalidPixelPos(true);

        final Point2D[] pixelLocations = pixelLocator.getPixelLocation(18.4, 34.5);
        assertNotNull(pixelLocations);
        assertEquals(0, pixelLocations.length);
    }

    private static class MockGeoCoding implements GeoCoding {

        private boolean returnInvalidPixelPos;

        void setReturnInvalidPixelPos(boolean returnInvalidPixelPos) {
            this.returnInvalidPixelPos = returnInvalidPixelPos;
        }

        @Override
        public boolean isCrossingMeridianAt180() {
            return false;
        }

        @Override
        public boolean canGetPixelPos() {
            return false;
        }

        @Override
        public boolean canGetGeoPos() {
            return false;
        }

        @Override
        public PixelPos getPixelPos(GeoPos geoPos, PixelPos pixelPos) {
            if (returnInvalidPixelPos) {
                return new PixelPos(Double.NaN, geoPos.getLat() - 1.0);
            } else {
                return new PixelPos(geoPos.getLon() - 1.0, geoPos.getLat() - 1.0);
            }
        }

        @Override
        public GeoPos getGeoPos(PixelPos pixelPos, GeoPos geoPos) {
            return new GeoPos(pixelPos.getY() + 1.0, pixelPos.getX() + 1.0);
        }

        @Override
        public Datum getDatum() {
            return null;
        }

        @Override
        public void dispose() {

        }

        @Override
        public CoordinateReferenceSystem getImageCRS() {
            return null;
        }

        @Override
        public CoordinateReferenceSystem getMapCRS() {
            return null;
        }

        @Override
        public CoordinateReferenceSystem getGeoCRS() {
            return null;
        }

        @Override
        public MathTransform getImageToMapTransform() {
            return null;
        }
    }
}
