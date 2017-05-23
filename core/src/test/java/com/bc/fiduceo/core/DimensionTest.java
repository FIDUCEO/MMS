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

package com.bc.fiduceo.core;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DimensionTest {

    private Dimension dimension;

    @Before
    public void setUp() throws Exception {
        dimension = new Dimension();
    }

    @Test
    public void testSetGetName() {
        final String name = "blabla";

        dimension.setName(name);
        assertEquals(name, dimension.getName());
    }

    @Test
    public void testSetGetNx() {
        final int nx = 23;

        dimension.setNx(nx);
        assertEquals(nx, dimension.getNx());
    }

    @Test
    public void testSetGetNy() {
        final int ny = 19;

        dimension.setNy(ny);
        assertEquals(ny, dimension.getNy());
    }

    @Test
    public void testDefaultConstruction() {
        assertEquals(Integer.MIN_VALUE, dimension.getNx());
        assertEquals(Integer.MIN_VALUE, dimension.getNy());
    }

    @Test
    public void testParameterConstruction() {
        final Dimension dimension = new Dimension("name", 12, 13);

        assertEquals("name", dimension.getName());
        assertEquals(12, dimension.getNx());
        assertEquals(13, dimension.getNy());
    }

    @SuppressWarnings("EqualsWithItself")
    @Test
    public void testEquals_sameObject() {
        assertTrue(dimension.equals(dimension));
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Test
    public void testEquals_differentClass() {
        assertFalse(dimension.equals(new Double(67.8)));
    }

    @Test
    public void testEquals() {
        final Dimension other = new Dimension(dimension.getName(), dimension.getNx(), dimension.getNy());
        assertTrue(this.dimension.equals(other));

        other.setName("wrong");
        assertFalse(this.dimension.equals(other));

        other.setName(dimension.getName());
        other.setNx(dimension.getNx() + 2);
        assertFalse(this.dimension.equals(other));

        other.setNx(dimension.getNx());
        other.setNy(dimension.getNy() + 1);
        assertFalse(this.dimension.equals(other));

        other.setNy(dimension.getNy());
        assertTrue(this.dimension.equals(other));
    }

    @Test
    public void testHashCode() {
        dimension.setName(null);
        assertEquals(29791, dimension.hashCode());

        dimension.setName("Yo");
        assertEquals(2787861, dimension.hashCode());

        dimension.setNx(14);
        assertEquals(-2144695353, dimension.hashCode());

        dimension.setNy(12);
        assertEquals(2788307, dimension.hashCode());
    }
}
