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

package com.bc.fiduceo.geometry.jts;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JtsUtilsTest {

    @Test
    public void testNormalizePolygon_tooSmallArray() {
        Coordinate[] coordinates = new Coordinate[0];
        JtsUtils.normalizePolygon(coordinates);
        assertEquals(0, coordinates.length);

        coordinates = new Coordinate[1];
        JtsUtils.normalizePolygon(coordinates);
        assertEquals(1, coordinates.length);
    }

    @Test
    public void testNormalizePolygon_noNormaization() {
        final Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(10, 10);
        coordinates[1] = new Coordinate(10, 20);
        coordinates[2] = new Coordinate(20, 20);
        coordinates[3] = new Coordinate(20, 10);
        coordinates[4] = new Coordinate(10, 10);

        JtsUtils.normalizePolygon(coordinates);
        assertEquals(10, coordinates[0].x, 1e-8);
        assertEquals(10, coordinates[1].x, 1e-8);
        assertEquals(20, coordinates[2].x, 1e-8);
        assertEquals(20, coordinates[3].x, 1e-8);
        assertEquals(10, coordinates[4].x, 1e-8);
    }

    @Test
    public void testNormalizePolygon_normalizeEast() {
        final Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(170, 10);
        coordinates[1] = new Coordinate(170, 20);
        coordinates[2] = new Coordinate(-175, 20);
        coordinates[3] = new Coordinate(-175, 10);
        coordinates[4] = new Coordinate(170, 10);

        JtsUtils.normalizePolygon(coordinates);
        assertEquals(170, coordinates[0].x, 1e-8);
        assertEquals(170, coordinates[1].x, 1e-8);
        assertEquals(185, coordinates[2].x, 1e-8);
        assertEquals(185, coordinates[3].x, 1e-8);
        assertEquals(170, coordinates[4].x, 1e-8);
    }

    @Test
    public void testNormalizePolygon_normalizeWest() {
        final Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(-170, 10);
        coordinates[1] = new Coordinate(-170, 20);
        coordinates[2] = new Coordinate(175, 20);
        coordinates[3] = new Coordinate(175, 10);
        coordinates[4] = new Coordinate(-170, 10);

        JtsUtils.normalizePolygon(coordinates);
        assertEquals(190, coordinates[0].x, 1e-8);
        assertEquals(190, coordinates[1].x, 1e-8);
        assertEquals(175, coordinates[2].x, 1e-8);
        assertEquals(175, coordinates[3].x, 1e-8);
        assertEquals(190, coordinates[4].x, 1e-8);
    }
}
