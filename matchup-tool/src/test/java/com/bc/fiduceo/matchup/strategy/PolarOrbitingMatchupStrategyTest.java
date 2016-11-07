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

package com.bc.fiduceo.matchup.strategy;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.QueryParameter;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class PolarOrbitingMatchupStrategyTest {

    @Test
    public void testIsSegmented() throws Exception {
        final GeometryCollection collection = mock(GeometryCollection.class);

        when(collection.getGeometries()).thenReturn(new Geometry[1]);
        assertEquals(false, PolarOrbitingMatchupStrategy.isSegmented(collection));

        when(collection.getGeometries()).thenReturn(new Geometry[2]);
        assertEquals(true, PolarOrbitingMatchupStrategy.isSegmented(collection));

        verify(collection, times(2)).getGeometries();
        verifyNoMoreInteractions(collection);
    }

    @Test
    public void testGetPixelLocator_notSegmented() throws Exception {
        final Reader reader = mock(Reader.class);
        final PixelLocator locator = mock(PixelLocator.class);
        when(reader.getPixelLocator()).thenReturn(locator);
        final Polygon polygon = mock(Polygon.class);
        final boolean segmented = false;

        final PixelLocator pixelLocator = PolarOrbitingMatchupStrategy.getPixelLocator(reader, segmented, polygon);

        verify(reader, times(1)).getPixelLocator();
        verifyNoMoreInteractions(reader);
        verifyNoMoreInteractions(polygon);
        assertNotNull(pixelLocator);
        assertSame(locator, pixelLocator);
    }

    @Test
    public void testGetPixelLocator_segmented() throws Exception {
        final Reader reader = mock(Reader.class);
        final PixelLocator locator = mock(PixelLocator.class);
        final Polygon polygon = mock(Polygon.class);
        when(reader.getSubScenePixelLocator(polygon)).thenReturn(locator);
        final boolean segmented = true;

        final PixelLocator pixelLocator = PolarOrbitingMatchupStrategy.getPixelLocator(reader, segmented, polygon);

        verify(reader, times(1)).getSubScenePixelLocator(same(polygon));
        verifyNoMoreInteractions(reader);
        verifyNoMoreInteractions(polygon);
        assertNotNull(pixelLocator);
        assertSame(locator, pixelLocator);
    }
}
