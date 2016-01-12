
/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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

import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteGeometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import com.vividsolutions.jts.io.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GeometryUtilsTest_JTS {

    private GeometryFactory factory;

    @Before
    public void setUp() {
        factory = new GeometryFactory(GeometryFactory.Type.JTS);
    }

    @Test
    public void testCreateTimeAxis() throws ParseException {
        final Polygon polygon = (Polygon) factory.parse("POLYGON((10 30, 10 20, 10 10, 20 10, 30 10, 30 20, 30 30, 20 30, 10 30))");

        final TimeAxis timeAxis = GeometryUtils.createTimeAxis(polygon, 0, 2, new Date(1000), new Date(2000));
        assertNotNull(timeAxis);
        assertEquals(1000, timeAxis.getTime(factory.createPoint(10, 30)).getTime());
        assertEquals(1500, timeAxis.getTime(factory.createPoint(10, 20)).getTime());
        assertEquals(2000, timeAxis.getTime(factory.createPoint(10, 10)).getTime());
    }

    @Test
    public void testPrepareForStorage_onePolygon_oneAxis_descending() {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        final List<Point> coordinateList = createCoordinateList(new double[]{10, 10, 10, 30, 30, 30, 10}, new double[]{30, 20, 10, 10, 20, 30, 30});
        acquisitionInfo.setCoordinates(coordinateList);
        acquisitionInfo.setTimeAxisStartIndices(new int[]{0});
        acquisitionInfo.setTimeAxisEndIndices(new int[]{2});
        acquisitionInfo.setSensingStart(new Date(100000));
        acquisitionInfo.setSensingStop(new Date(200000));
        acquisitionInfo.setNodeType(NodeType.DESCENDING);

        final SatelliteGeometry satelliteGeometry = GeometryUtils.prepareForStorage(acquisitionInfo);
        assertNotNull(satelliteGeometry);

        final com.bc.fiduceo.geometry.Geometry geometry = satelliteGeometry.getGeometry();
        assertNotNull(geometry);
        assertEquals("POLYGON ((10 30, 30 30, 30 20, 30 10, 10 10, 10 20, 10 30))", geometry.toString());

        final TimeAxis[] timeAxes = satelliteGeometry.getTimeAxes();
        assertNotNull(timeAxes);
        assertEquals(1, timeAxes.length);
        assertEquals(100000, timeAxes[0].getTime(factory.createPoint(10, 20)).getTime());
    }

    private List<Point> createCoordinateList(double[] lons, double[] lats) {
        final ArrayList<Point> coordinates = new ArrayList<>(lons.length);

        for (int i = 0; i < lons.length; i++) {
            coordinates.add(factory.createPoint(lons[i], lats[i]));
        }

        return coordinates;
    }
}
