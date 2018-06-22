/*
 * Copyright (C) 2018 Brockmann Consult GmbH
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
package com.bc.fiduceo.reader.airs;

import com.bc.fiduceo.reader.ArrayCache;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.List;

public class AirsArrayCache extends ArrayCache {

    public AirsArrayCache(NetcdfFile netcdfFile) {
        super(netcdfFile);
    }

    protected ArrayContainer readArrayAndAttributes(String variableName, Group group) throws IOException {
        Variable variable;
        variable = injectedVariables.get(variableName);
        if (variable == null) {
            synchronized (netcdfFile) {
                final List<Variable> variables = netcdfFile.getVariables();
                for (Variable var : variables) {
                    if (var.getShortName().equals(variableName)) {
                        variable = var;
                        break;
                    }
                }
            }
            if (variable == null) {
                throw new IOException("requested variable '" + variableName + "' not present in file: " + netcdfFile.getLocation());
            }
        }
        final ArrayContainer container = new ArrayContainer();
        container.array = variable.read();

        final List<Attribute> attributes = variable.getAttributes();
        for (final Attribute attribute : attributes) {
            container.attributes.put(attribute.getFullName(), attribute);
        }
        return container;
    }

}
