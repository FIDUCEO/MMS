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

package com.bc.fiduceo;


import com.bc.fiduceo.geometry.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GeometryUtilTest {

    @Test
    public void testToKml_simplePolygon() {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Polygon polygon = (Polygon) geometryFactory.parse("POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))");

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +
                "<Document>\n" +
                "  <Style id=\"polygonStyle\">\n" +
                "    <LineStyle>\n" +
                "      <color>7f00ffff</color>\n" +
                "      <width>1</width>\n" +
                "    </LineStyle>\n" +
                "    <PolyStyle>\n" +
                "      <color>7f00ff00</color>\n" +
                "    </PolyStyle>\n" +
                "  </Style>\n" +
                "  <Placemark>\n" +
                "  <name>default_name</name>\n" +
                "  <styleUrl>#polygonStyle</styleUrl>\n" +
                "  <Polygon>\n" +
                "    <altitudeMode>clampToGround</altitudeMode>\n" +
                "    <outerBoundaryIs>\n" +
                "      <LinearRing>\n" +
                "        <coordinates>\n" +
                "          0.0,0.0,0\n" +
                "          0.0,1.0,0\n" +
                "          0.9999999999999998,1.0,0\n" +
                "          1.0,0.0,0\n" +
                "          0.0,0.0,0\n" +
                "        </coordinates>\n" +
                "      </LinearRing>\n" +
                "    </outerBoundaryIs>\n" +
                "  </Polygon>\n" +
                "  </Placemark>\n" +
                "</Document>\n" +
                "</kml>", GeometryUtil.toKml(polygon));
    }

    @Test
    public void testCreatePolygonFromMinMax() {
        final double[] geoMinMax = {-170.0, 170.0, -80.0, 80.0};

        final Polygon polygon = GeometryUtil.createPolygonFromMinMax(geoMinMax, new GeometryFactory(GeometryFactory.Type.S2));
        final Point[] coordinates = polygon.getCoordinates();
        assertEquals(5, coordinates.length);
        assertEquals(-170, coordinates[0].getLon(), 1e-8);
        assertEquals(-80, coordinates[0].getLat(), 1e-8);

        assertEquals(170, coordinates[2].getLon(), 1e-8);
        assertEquals(80, coordinates[2].getLat(), 1e-8);

        assertEquals(-170, coordinates[4].getLon(), 1e-8);
        assertEquals(-80, coordinates[4].getLat(), 1e-8);
    }

    @Test
    public void testCreateMultiLineStringFromMinMax() {
        final double[] geoMinMax = {-172.0, 172.0, -78.0, 87.0};

        final MultiLineString lineString = GeometryUtil.createMultiLineStringFromMinMax(geoMinMax, new GeometryFactory(GeometryFactory.Type.S2));
        assertNotNull(lineString);
        final Point[] coordinates = lineString.getCoordinates();
        assertEquals(4, coordinates.length);
        assertEquals("POINT(-172.0 0.0)", coordinates[0].toString());
        assertEquals("POINT(172.0 0.0)", coordinates[1].toString());
        assertEquals("POINT(0.0 87.0)", coordinates[2].toString());
        assertEquals("POINT(0.0 -78.0)", coordinates[3].toString());
    }
}
