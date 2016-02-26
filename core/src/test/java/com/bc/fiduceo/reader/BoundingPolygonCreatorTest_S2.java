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

package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BoundingPolygonCreatorTest_S2 extends BoundingPolygonCreatorTest {

    @Before
    public void setUp() throws IOException {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Interval interval = new Interval(8, 8);

        boundingPolygonCreator = new BoundingPolygonCreator(interval, geometryFactory);
    }

    @Test
    public void testPlotMultiPolygon() {
        List<Polygon> polygonList = new ArrayList<>();
        polygonList.add((Polygon) geometryFactory.parse("POLYGON ((10 10, 20 10, 20 20, 10 20, 10 10))"));
        polygonList.add((Polygon) geometryFactory.parse("POLYGON((-8 -10,-6 -10,-6 -8,-8 -8,-8 -10))"));
        String multiPolygon = BoundingPolygonCreator.plotMultiPolygon(polygonList);
        assertEquals("MULTIPOLYGON(((9.999999999999998 10.0,20.0 10.0,20.0 20.0,10.0 20.0)),((-7.999999999999998 -10.0,-6.000000000000001 -10.0,-6.0 -7.999999999999998,-7.999999999999998 -7.999999999999998)))", multiPolygon);

    }
}
