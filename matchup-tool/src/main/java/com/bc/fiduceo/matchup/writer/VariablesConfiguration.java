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


import static java.io.File.separator;

import java.util.*;

public class VariablesConfiguration {

    public final static String DEFAULT_SEPARATOR = "_";

    private final HashMap<String, Map<String, String>> renamesMap;
    private final HashMap<String, List<String>> excludesMap;
    private final Map<String, String> sensorRenames;
    private final Map<String, String> sensorSeparator;


    public VariablesConfiguration() {
        renamesMap = new HashMap<>();
        excludesMap = new HashMap<>();
        sensorRenames = new HashMap<>();
        sensorSeparator = new HashMap<>();
    }

    public Map<String, String> getRenames(String sensorKey) {
        final Map<String, String> variableRenames = renamesMap.get(sensorKey);
        if (variableRenames == null) {
            return new HashMap<>();
        }
        return Collections.unmodifiableMap(variableRenames);
    }

    public List<String> getExcludes(String sensorKey) {
        final List<String> variableExcludes = excludesMap.get(sensorKey);
        if (variableExcludes == null) {
            return new ArrayList<>();
        }
        return Collections.unmodifiableList(variableExcludes);
    }

    public void addRenames(String sensorKeys, Map<String, String> renames) {
        final StringTokenizer stringTokenizer = new StringTokenizer(sensorKeys, ",");
        while (stringTokenizer.hasMoreTokens()) {
            final String sensorKey = stringTokenizer.nextToken().trim();
            if (renamesMap.containsKey(sensorKey)) {
                renamesMap.get(sensorKey).putAll(renames);
            } else {
                renamesMap.put(sensorKey, renames);
            }
        }
    }

    public void addExcludes(String sensorKeys, List<String> excludes) {
        final StringTokenizer stringTokenizer = new StringTokenizer(sensorKeys, ",");
        while (stringTokenizer.hasMoreTokens()) {
            final String sensorKey = stringTokenizer.nextToken().trim();
            excludesMap.put(sensorKey, excludes);
        }
    }

    public Map<String, String> getSensorRenames() {
        return Collections.unmodifiableMap(sensorRenames);
    }

    public void addSensorRename(String sourceName, String targetName) {
        sensorRenames.put(sourceName, targetName);
    }

    public String getSeparator(String sensorName) {
        if (sensorSeparator.containsKey(sensorName)) {
            return sensorSeparator.get(sensorName);
        } else {
            return DEFAULT_SEPARATOR;
        }
    }

    public void setSeparator(String sensorName, String separator) {
        sensorSeparator.put(sensorName, separator);
    }
}
