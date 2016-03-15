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

import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VariablesConfiguration {

    private final List<VariablePrototype> prototypes;

    public VariablesConfiguration() {
        prototypes = new ArrayList<>();
    }

    public void extractPrototypes(Sensor sensor, String filePath) throws IOException {
        final ReaderFactory readerFactory = ReaderFactory.get();
        final Reader reader = readerFactory.getReader(sensor.getName());

        try {
            reader.open(new File(filePath));
            final List<Variable> variables = reader.getVariables();
            for (final Variable variable : variables) {
                final VariablePrototype prototype = new VariablePrototype();
                final String shortName = variable.getShortName();
                prototype.setName(sensor.getName() + "_" + shortName);
                prototypes.add(prototype);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public List<VariablePrototype> get() {
        return prototypes;
    }
}
