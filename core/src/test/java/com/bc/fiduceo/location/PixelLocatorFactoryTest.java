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

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;

import java.awt.geom.Point2D;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("ConstantConditions")
public class PixelLocatorFactoryTest {

    private GeometryFactory geometryFactory;

    @Before
    public void setUp() {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
    }

    @Test
    public void testGetClipping(){
        final PixelLocator locator = mock(PixelLocator.class);

        final PixelLocator pixelLocator = PixelLocatorFactory.getClippingPixelLocator(locator, 5, 105);
        assertNotNull(pixelLocator);
        assertTrue(pixelLocator instanceof ClippingPixelLocator);
    }

    @Test
    public void testGetSwath(){
        final Array longitudes = Array.factory(new float[] {1.f, 2.f, 3.f, 4.f});
        final Array latitudes= Array.factory(new float[] {5.f, 6.f, 7.f, 8.f});

        final PixelLocator pixelLocator = PixelLocatorFactory.getSwathPixelLocator(longitudes, latitudes, 2, 2);
        assertNotNull(pixelLocator);
        assertTrue(pixelLocator instanceof SwathPixelLocator);
    }

    @Test
    public void testGetSubScenePixelLocator_firstScene() throws Exception {
        final Polygon polygon = mock(Polygon.class);
        when(polygon.getCentroid()).thenReturn(geometryFactory.createPoint(26, 40));
        final PixelLocator locator = mock(PixelLocator.class);
        when(locator.getGeoLocation(100.5, 2500.5, null)).thenReturn(new Point2D.Double(30, 45));
        when(locator.getGeoLocation(100.5, 7500.5, null)).thenReturn(new Point2D.Double(15, -45));

        final PixelLocator pixelLocator = PixelLocatorFactory.getSubScenePixelLocator(polygon, 200, 9000, 5000, locator);

        verify(locator, times(1)).getGeoLocation(100.5, 2500.5, null);
        verify(locator, times(1)).getGeoLocation(100.5, 7500.5, null);
        verifyNoMoreInteractions(locator);
        verify(polygon, times(1)).getCentroid();
        verifyNoMoreInteractions(polygon);

        assertNotNull(pixelLocator);
        assertEquals(true, pixelLocator instanceof ClippingPixelLocator);
        final ClippingPixelLocator clipping = (ClippingPixelLocator) pixelLocator;
        assertEquals(0, clipping.minY);
        assertEquals(4999, clipping.maxY);
        assertSame(locator, clipping.pixelLocator);
    }

    @Test
    public void testGetSubScenePixelLocator_secondScene() throws Exception {
        final Polygon polygon = mock(Polygon.class);
        when(polygon.getCentroid()).thenReturn(geometryFactory.createPoint(17, -40));
        final PixelLocator locator = mock(PixelLocator.class);
        when(locator.getGeoLocation(100.5, 2500.5, null)).thenReturn(new Point2D.Double(30, 45));
        when(locator.getGeoLocation(100.5, 7500.5, null)).thenReturn(new Point2D.Double(15, -45));

        final PixelLocator pixelLocator = PixelLocatorFactory.getSubScenePixelLocator(polygon, 200, 9000, 5000, locator);

        verify(locator, times(1)).getGeoLocation(100.5, 2500.5, null);
        verify(locator, times(1)).getGeoLocation(100.5, 7500.5, null);
        verifyNoMoreInteractions(locator);
        verify(polygon, times(1)).getCentroid();
        verifyNoMoreInteractions(polygon);

        assertNotNull(pixelLocator);
        assertEquals(true, pixelLocator instanceof ClippingPixelLocator);
        final ClippingPixelLocator clipping = (ClippingPixelLocator) pixelLocator;
        assertEquals(4999, clipping.minY);
        assertEquals(8999, clipping.maxY);
        assertSame(locator, clipping.pixelLocator);
    }

    @Test
    public void testGetSubScenePixelLocator_invalidGeolocation_firstLine() throws Exception {
        final Polygon polygon = mock(Polygon.class);
        when(polygon.getCentroid()).thenReturn(geometryFactory.createPoint(17, -40));
        final PixelLocator locator = mock(PixelLocator.class);
        when(locator.getGeoLocation(100.5, 2500.5, null)).thenReturn(null);
        when(locator.getGeoLocation(100.5, 7500.5, null)).thenReturn(new Point2D.Double(15, -45));

        final PixelLocator pixelLocator = PixelLocatorFactory.getSubScenePixelLocator(polygon, 200, 9000, 5000, locator);
        assertNull(pixelLocator);
    }

    @Test
    public void testGetSubScenePixelLocator_invalidGeolocation_lastLine() throws Exception {
        final Polygon polygon = mock(Polygon.class);
        when(polygon.getCentroid()).thenReturn(geometryFactory.createPoint(17, -40));
        final PixelLocator locator = mock(PixelLocator.class);
        when(locator.getGeoLocation(100.5, 2500.5, null)).thenReturn(new Point2D.Double(30, 45));
        when(locator.getGeoLocation(100.5, 7500.5, null)).thenReturn(null);

        final PixelLocator pixelLocator = PixelLocatorFactory.getSubScenePixelLocator(polygon, 200, 9000, 5000, locator);
        assertNull(pixelLocator);
    }
}
