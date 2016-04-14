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
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.util.IO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VariablesConfiguration {

    private final HashMap<String, List<VariablePrototype>> prototypesMap;

    public VariablesConfiguration() {
        prototypesMap = new HashMap<>();
    }

    public void extractPrototypes(Sensor sensor, Path filePath, Dimension dimension) throws IOException {
        final ReaderFactory readerFactory = ReaderFactory.get();
        final String sensorName = sensor.getName();

        final List<VariablePrototype> prototypes;
        if (!prototypesMap.containsKey(sensorName)) {
            prototypesMap.put(sensorName, new ArrayList<>());
        }
        prototypes = prototypesMap.get(sensorName);

        try (final Reader reader = readerFactory.getReader(sensorName)) {
            reader.open(filePath.toFile());

            final String dimensionNames = createDimensionNames(dimension);
            final List<Variable> variables = reader.getVariables();
            for (final Variable variable : variables) {
                final VariablePrototype prototype = new VariablePrototype();
                prototype.setSourceVariableName(variable.getFullName());
                prototype.setTargetVariableName(sensorName + "_" + variable.getShortName());
                prototype.setDataType(variable.getDataType().toString());
                prototype.setDimensionNames(dimensionNames);
                prototype.setAttributes(getAttributeClones(variable));

                prototypes.add(prototype);
            }
        } catch (InvalidRangeException e) {
            throw new IOException(e.getMessage());
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
        final ArrayList<VariablePrototype> allPrototypes = new ArrayList<>();
        for (List<VariablePrototype> prototypes : prototypesMap.values()) {
            allPrototypes.addAll(prototypes);
        }
        return allPrototypes;
    }

    /**
     * Returns a list of {@link VariablePrototype} associated with the given sensor name.
     * If there are no associated {@link VariablePrototype}s, an empty list will be returned.
     *
     * @param sensorName the name of the sensor
     * @return a list of {@link VariablePrototype}
     */
    public List<VariablePrototype> getPrototypesFor(String sensorName) {
        if (prototypesMap.containsKey(sensorName)) {
            return prototypesMap.get(sensorName);
        }
        return new ArrayList<>();
    }


    // package access for testing only tb 2016-04-12
    static String createDimensionNames(Dimension dimension) {
        final String dimensionName = dimension.getName();
        return "matchup_count " + dimensionName + "_ny " + dimensionName + "_nx";
    }
}
