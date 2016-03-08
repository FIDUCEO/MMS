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


import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.GeometryUtil;
import com.bc.fiduceo.geometry.Polygon;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GeometryUtilTest {

    @Test
    public void testToKml_simplePolygon() {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Polygon polygon = (Polygon) geometryFactory.parse("POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))");

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +
                "  <Placemark>\n" +
                "  <name>default_name</name>\n" +
                "  <Polygon>\n" +
                "    <altitudeMode>clampToGround</altitudeMode>\n" +
                "    <outerBoundaryIs>\n" +
                "      <LinearRing>\n" +
                "        <coordinates>\n" +
                "          1.0,0.0,0\n" +
                "          0.9999999999999998,1.0,0\n" +
                "          0.0,1.0,0\n" +
                "          0.0,0.0,0\n" +
                "          1.0,0.0,0\n" +
                "        </coordinates>\n" +
                "      </LinearRing>\n" +
                "    </outerBoundaryIs>\n" +
                "  </Polygon>\n" +
                "  </Placemark>\n" +
                "</kml>", GeometryUtil.toKml(polygon));


    }
}
