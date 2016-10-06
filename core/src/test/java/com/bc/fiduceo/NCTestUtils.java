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

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NCTestUtils {

    public static void assertScalarVariable(String variableName, int index, double expected, NetcdfFile netcdfFile) throws IOException, InvalidRangeException {
        final String escapedName = NetcdfFile.makeValidCDLName(variableName);
        final Variable variable = netcdfFile.findVariable(escapedName);
        assertNotNull(variable);
        final Array data = variable.read(new int[]{index}, new int[]{1});
        assertEquals(expected, data.getDouble(0), 1e-8);
    }

    public static void assertStringVariable(String variableName, int index, String expected, NetcdfFile netcdfFile) throws IOException, InvalidRangeException {
        final String escapedName = NetcdfFile.makeValidCDLName(variableName);
        final Variable variable = netcdfFile.findVariable(escapedName);
        assertNotNull(variable);
        final Array data = variable.read(new int[]{index, 0}, new int[]{1, 128});
        final char[] valueAsArray = (char[]) data.get1DJavaArray(char.class);
        assertEquals(expected, new String(valueAsArray).trim());
    }

    public static void assert3DVariable(String variableName, int x, int y, int z, double expected, NetcdfFile mmd) throws IOException, InvalidRangeException {
        final String escapedName = NetcdfFile.makeValidCDLName(variableName);
        final Variable variable = mmd.findVariable(escapedName);
        assertNotNull(variable);
        final Array data = variable.read(new int[]{z, y, x}, new int[]{1, 1, 1});
        assertEquals(expected, data.getDouble(0), 1e-8);
    }

    public static void assertValueAt(double expected, int x, int y, Array array) {
        final Index index = array.getIndex();
        index.set(y, x);
        assertEquals(expected, array.getDouble(index), 1e-8);
    }

    public static void assertValueAt(int expected, int x, int y, Array array) {
        final Index index = array.getIndex();
        index.set(y, x);
        assertEquals(expected, array.getInt(index));
    }
}
