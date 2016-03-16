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

package com.bc.fiduceo.matchup.writer;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VariablesConfiguration {

    private final List<VariablePrototype> prototypes;

    public VariablesConfiguration() {
        prototypes = new ArrayList<>();
    }

    public void extractPrototypes(Sensor sensor, Path filePath, Dimension dimension) throws IOException {
        final ReaderFactory readerFactory = ReaderFactory.get();
        final Reader reader = readerFactory.getReader(sensor.getName());

        try {
            reader.open(filePath.toFile());

            final String dimensionNames = createDimensionNames(dimension);
            final List<Variable> variables = reader.getVariables();
            for (final Variable variable : variables) {
                final VariablePrototype prototype = new VariablePrototype();
                final String shortName = variable.getShortName();
                prototype.setTargetVariableName(sensor.getName() + "_" + shortName);
                prototype.setDataType(variable.getDataType().toString());
                prototype.setDimensionNames(dimensionNames);
                final List<Attribute> newAttributes = getAttributeClones(variable);
                prototype.setAttributes(newAttributes);
                prototypes.add(prototype);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    static List<Attribute> getAttributeClones(Variable variable) {
        final List<Attribute> attributes = variable.getAttributes();
        final ArrayList<Attribute> newAttributes = new ArrayList<>();
        for (Attribute attribute : attributes) {
            final String attName = attribute.getShortName();
            final Array attVals = attribute.getValues().copy();
            final Attribute newAttribute = new Attribute(attName, attVals);
            newAttributes.add(newAttribute);
        }
        return newAttributes;
    }

    public List<VariablePrototype> get() {
        return prototypes;
    }

    // @todo 2 tb/tb make static and add tests
    String createDimensionNames(Dimension dimension) {
        final StringBuilder dimensionNames = new StringBuilder();
        dimensionNames.append("matchup_count ");
        dimensionNames.append(dimension.getName());
        dimensionNames.append("_ny ");
        dimensionNames.append(dimension.getName());
        dimensionNames.append("_nx");
        return dimensionNames.toString();
    }
}
