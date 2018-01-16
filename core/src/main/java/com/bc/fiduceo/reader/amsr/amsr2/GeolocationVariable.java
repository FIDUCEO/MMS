/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.amsr.amsr2;

import com.bc.fiduceo.util.VariablePrototype;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.List;

//@todo 2 tb/tb write tests 2018-01-16
@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
class GeolocationVariable extends VariablePrototype {

    private final Variable variable;

    GeolocationVariable(String name, NetcdfFile netcdfFile) {
        final String escapedName = NetcdfFile.makeValidCDLName(name);
        synchronized (netcdfFile) {
            variable = netcdfFile.findVariable(escapedName);
        }
    }

    @Override
    public String getShortName() {
        return variable.getShortName();
    }

    @Override
    public DataType getDataType() {
        return variable.getDataType();
    }

    @Override
    public Array read() throws IOException {
        final int[] shape = variable.getShape();
        final int[] origin = new int[]{0, 0};
        final int[] stride = new int[] {1, 2};
        try {
            final Section section = new Section(origin, shape, stride);

            return variable.read(section).copy();
        } catch (InvalidRangeException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public List<Attribute> getAttributes() {
        return variable.getAttributes();
    }
}
