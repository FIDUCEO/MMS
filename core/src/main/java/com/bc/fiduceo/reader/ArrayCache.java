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

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.HashMap;

class ArrayCache {

    private final NetcdfFile netcdfFile;
    private final HashMap<String, Array> cache;

    ArrayCache(NetcdfFile netcdfFile) {
        this.netcdfFile = netcdfFile;

        cache = new HashMap<>();
    }

    Array get(String variableName) throws IOException {
        Array array = cache.get(variableName);
        if (array == null) {
            synchronized (netcdfFile) {
                final Variable variable = netcdfFile.findVariable(null, variableName);
                if (variable == null) {
                    throw new IOException("requested variable '" + variableName + "' not present in file");
                }
                array = variable.read();
            }
            cache.put(variableName, array);
        }

        return array;
    }
}
