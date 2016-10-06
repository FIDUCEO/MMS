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


import java.util.*;

class VariablesConfiguration {

    private final HashMap<String, List<VariableRename>> renamesMap;
    private final HashMap<String, List<VariableExclude>> excludesMap;

    VariablesConfiguration() {
        renamesMap = new HashMap<>();
        excludesMap = new HashMap<>();
    }

    List<VariableRename> getRenames(String sensorKey) {
        final List<VariableRename> variableRenames = renamesMap.get(sensorKey);
        if (variableRenames == null) {
            return new ArrayList<>();
        }
        return Collections.unmodifiableList(variableRenames);
    }

    List<VariableExclude> getExcludes(String sensorKey) {
        final List<VariableExclude> variableExcludes = excludesMap.get(sensorKey);
        if (variableExcludes == null) {
            return new ArrayList<>();
        }
        return Collections.unmodifiableList(variableExcludes);
    }

    void addRenames(String sensorKeys, List<VariableRename> renames) {
        final StringTokenizer stringTokenizer = new StringTokenizer(sensorKeys, ",");
        while (stringTokenizer.hasMoreTokens()) {
            final String sensorKey = stringTokenizer.nextToken().trim();
            renamesMap.put(sensorKey, renames);
        }
    }

    void addExcludes(String sensorKeys, List<VariableExclude> excludes) {
        final StringTokenizer stringTokenizer = new StringTokenizer(sensorKeys, ",");
        while (stringTokenizer.hasMoreTokens()) {
            final String sensorKey = stringTokenizer.nextToken().trim();
            excludesMap.put(sensorKey, excludes);
        }
    }
}
