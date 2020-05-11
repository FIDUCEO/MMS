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

import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NCTestUtils {

    public static void assertVectorVariable(String variableName, int index, double expected, NetcdfFile netcdfFile) throws IOException, InvalidRangeException {
        final Variable variable = getVariable(variableName, netcdfFile);
        assertNotNull(variable);
        final Array data = variable.read(new int[]{index}, new int[]{1});
        assertEquals(expected, data.getDouble(0), 1e-8);
    }

    public static void assertStringVariable(String variableName, int index, String expected, NetcdfFile netcdfFile) throws IOException, InvalidRangeException {
        final int defaultMaxStringSize = 128;
        assertStringVariable(variableName, defaultMaxStringSize, index, expected, netcdfFile);
    }

    public static void assertStringVariable(String variableName, final int maxStringSize, int index, String expected, NetcdfFile netcdfFile) throws IOException, InvalidRangeException {
        final String dontCheckAssociatedDimensions = null;
        assertStringVariable(variableName, dontCheckAssociatedDimensions, maxStringSize, index, expected, netcdfFile);
    }

    public static void assertStringVariable(String variableName, String dimNames, final int maxStringSize, int index, String expected, NetcdfFile netcdfFile) throws IOException, InvalidRangeException {
        final Variable variable = assertVariablePresent(variableName, DataType.CHAR, dimNames, netcdfFile);
        final Array data = variable.read(new int[]{index, 0}, new int[]{1, maxStringSize});
        final char[] valueAsArray = (char[]) data.get1DJavaArray(char.class);
        assertEquals(expected, new String(valueAsArray).trim());
    }

    public static void assert2DVariable(String variableName, int x, int y, double expected, NetcdfFile netcdfFile) throws IOException, InvalidRangeException {
        final Variable variable = getVariable(variableName, netcdfFile);
        assertNotNull("NetCDF Variable '" + variableName + "' expected", variable);
        final Array data = variable.read(new int[]{y, x}, new int[]{1, 1});
        assertEquals(expected, data.getDouble(0), 1e-8);
    }

    public static void assertVariablePresentAnd1DValueLong(String variableName, final DataType dataType, String dimensions, int x, long expected, NetcdfFile netcdfFile) throws IOException, InvalidRangeException {
        final Variable variable = assertVariablePresent(variableName, dataType, dimensions, netcdfFile);
        assert1DValueLong(x, expected, variable);
    }

    public static void assertVariablePresentAnd3DValueLong(String variableName, final DataType dataType, String dimensions, int x, int y, int z, long expected, NetcdfFile netcdfFile) throws IOException, InvalidRangeException {
        final Variable variable = assertVariablePresent(variableName, dataType, dimensions, netcdfFile);
        assert3DValueLong(x, y, z, expected, variable);
    }

    public static void assertVariablePresentAnd3DValueDouble(String variableName, final DataType dataType, String dimensions, int x, int y, int z, double expected, NetcdfFile netcdfFile) throws IOException, InvalidRangeException {
        final Variable variable = assertVariablePresent(variableName, dataType, dimensions, netcdfFile);
        assert3DValueDouble(x, y, z, expected, variable);
    }

    public static void assert3DVariable(String variableName, int x, int y, int z, double expected, NetcdfFile netcdfFile) throws IOException, InvalidRangeException {
        final Variable variable = getVariable(variableName, netcdfFile);
        assert3DValueDouble(x, y, z, expected, variable);
    }

    public static void assert4DVariable(String variableName, int x, int y, int z, int p, double expected, NetcdfFile netcdfFile) throws IOException, InvalidRangeException {
        final Variable variable = getVariable(variableName, netcdfFile);
        assertNotNull("NetCDF Variable '" + variableName + "' expected", variable);
        final Array data = variable.read(new int[]{p, z, y, x}, new int[]{1, 1, 1, 1});
        assertEquals(expected, data.getDouble(0), 1e-8);
    }

    public static Variable assertVariablePresent(String variableName, DataType dataType, String dimNames, NetcdfFile netcdfFile) {
        final Variable variable = getVariable(variableName, netcdfFile);
        assertNotNull(variable);
        assertEquals(dataType, variable.getDataType());
        if (dimNames != null) {
            assertDimensions(dimNames, variable);
        }
        return variable;
    }

    public static void assertDimension(String dimensionName, int size, NetcdfFile netcdfFile) {
        final int dimensionLength = NetCDFUtils.getDimensionLength(dimensionName, netcdfFile);
        assertEquals(size, dimensionLength);
    }

    public static void assertValueAt(double expected, int x, int y, Array array) {
        final Index index = array.getIndex();
        index.set(y, x);
        assertEquals(expected, array.getDouble(index), 1e-8);
    }

    public static void assertValueAt(short expected, int y, Array array) {
        final Index index = array.getIndex();
        index.set(y);
        assertEquals(expected, array.getInt(index));
    }

    public static void assertValueAt(double expected, int y, Array array) {
        final Index index = array.getIndex();
        index.set(y);
        assertEquals(expected, array.getDouble(index), 1e-8);
    }

    public static void assertValueAt(int expected, int x, int y, Array array) {
        final Index index = array.getIndex();
        index.set(y, x);
        assertEquals(expected, array.getInt(index));
    }

    public static void assertValueAt(long expected, int x, int y, Array array) {
        final Index index = array.getIndex();
        index.set(y, x);
        assertEquals(expected, array.getLong(index));
    }

    public static Variable getVariable(String variableName, NetcdfFile netcdfFile) {
        final String escapedName = NetCDFUtils.escapeVariableName(variableName);
        return netcdfFile.findVariable(escapedName);
    }

    public static Variable getVariable(String variableName, NetcdfFile netcdfFile, boolean escapeName) {
        final String escapedName;
        if (escapeName) {
            escapedName = NetCDFUtils.escapeVariableName(variableName);
        } else {
            escapedName = variableName;
        }
        return netcdfFile.findVariable(escapedName);
    }

    public static void assert3DValueDouble(int x, int y, int z, double expected, Variable variable) throws IOException, InvalidRangeException {
        assertNotNull("NetCDF Variable '" + variable.getShortName() + "' expected", variable);
        final Array data = variable.read(new int[]{z, y, x}, new int[]{1, 1, 1});
        assertEquals(expected, data.getDouble(0), 1e-8);
    }

    public static void assert1DValueLong(int x, long expected, Variable variable) throws IOException {
        assertNotNull("NetCDF Variable '" + variable.getShortName() + "' expected", variable);
        final Array array = variable.read();
        final Index index = array.getIndex();
        index.set(x);
        assertEquals(expected, array.getLong(index));
    }

    public static void assert3DValueLong(int x, int y, int z, long expected, Variable variable) throws IOException, InvalidRangeException {
        assertNotNull("NetCDF Variable '" + variable.getShortName() + "' expected", variable);
        final Array data = variable.read(new int[]{z, y, x}, new int[]{1, 1, 1});
        assertEquals(expected, data.getLong(0));
    }

    public static void assertDimensions(String dimensions, Variable variable) {
        assertEquals(dimensions, variable.getDimensionsString());
    }
}
