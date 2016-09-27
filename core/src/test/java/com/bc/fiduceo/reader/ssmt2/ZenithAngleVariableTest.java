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

package com.bc.fiduceo.reader.ssmt2;

import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Attribute;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ZenithAngleVariableTest {

    @Test
    public void testSensorType() {
        assertEquals(4, ZenithAngleVariable.SensorType.values().length);
        assertEquals("F11", ZenithAngleVariable.SensorType.F11.toString());
        assertEquals("F14", ZenithAngleVariable.SensorType.F14.toString());
    }

    @Test
    public void testGetDataType() {
        final ZenithAngleVariable variable = new ZenithAngleVariable(ZenithAngleVariable.SensorType.F12, 34);

        assertEquals(DataType.FLOAT, variable.getDataType());
    }

    @Test
    public void testGetShape() {
        final ZenithAngleVariable variable = new ZenithAngleVariable(ZenithAngleVariable.SensorType.F14, 35);

        final int[] shape = variable.getShape();
        assertEquals(2, shape.length);
        assertEquals(35, shape[0]);
        assertEquals(28, shape[1]);
    }

    @Test
    public void testGetShape_indexed() {
        final ZenithAngleVariable variable = new ZenithAngleVariable(ZenithAngleVariable.SensorType.F15, 36);

        assertEquals(36, variable.getShape(0));
        assertEquals(28, variable.getShape(1));
    }

    @Test
    public void testGetShape_indexed_outOfRange() {
        final ZenithAngleVariable variable = new ZenithAngleVariable(ZenithAngleVariable.SensorType.F11, 37);

        try {
            variable.getShape(-1);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }

        try {
            variable.getShape(2);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetAttributes() {
        final ZenithAngleVariable variable = new ZenithAngleVariable(ZenithAngleVariable.SensorType.F12, 38);

        final List<Attribute> attributes = variable.getAttributes();
        assertEquals(1, attributes.size());
        final Attribute attribute = attributes.get(0);
        assertEquals("units", attribute.getFullName());
        assertEquals("degrees", attribute.getStringValue());
    }

    @Test
    public void testRead_F11() throws IOException {
        final ZenithAngleVariable variable = new ZenithAngleVariable(ZenithAngleVariable.SensorType.F11, 12);

        final Array array = variable.read();
        final Index index = array.getIndex();

        index.set(0, 0);
        assertEquals(47.43000030517578, array.getFloat(index), 1e-8);

        index.set(5, 2);
        assertEquals(39.959999084472656, array.getFloat(index), 1e-8);

        index.set(7, 24);
        assertEquals(36.33000183105469, array.getFloat(index), 1e-8);
    }

    @Test
    public void testRead_F12() throws IOException {
        final ZenithAngleVariable variable = new ZenithAngleVariable(ZenithAngleVariable.SensorType.F12, 14);

        final Array array = variable.read();
        final Index index = array.getIndex();

        index.set(1, 1);
        assertEquals(43.630001068115234, array.getFloat(index), 1e-8);

        index.set(6, 3);
        assertEquals(36.310001373291016, array.getFloat(index), 1e-8);

        index.set(8, 23);
        assertEquals(32.7400016784668, array.getFloat(index), 1e-8);
    }

    @Test
    public void testRead_F14() throws IOException {
        final ZenithAngleVariable variable = new ZenithAngleVariable(ZenithAngleVariable.SensorType.F14, 15);

        final Array array = variable.read();
        final Index index = array.getIndex();

        index.set(2, 2);
        assertEquals(39.95000076293945, array.getFloat(index), 1e-8);

        index.set(7, 4);
        assertEquals(32.75, array.getFloat(index), 1e-8);

        index.set(9, 22);
        assertEquals(29.209999084472656, array.getFloat(index), 1e-8);
    }

    @Test
    public void testRead_F15() throws IOException {
        final ZenithAngleVariable variable = new ZenithAngleVariable(ZenithAngleVariable.SensorType.F15, 16);

        final Array array = variable.read();
        final Index index = array.getIndex();

        index.set(3, 3);
        assertEquals(36.310001373291016, array.getFloat(index), 1e-8);

        index.set(8, 5);
        assertEquals(29.209999084472656, array.getFloat(index), 1e-8);

        index.set(10, 21);
        assertEquals(25.709999084472656, array.getFloat(index), 1e-8);
    }

    @Test
    public void testGetShortName() {
        final ZenithAngleVariable variable = new ZenithAngleVariable(ZenithAngleVariable.SensorType.F11, 17);

        assertEquals("Satellite_zenith_angle", variable.getShortName());
    }
}
