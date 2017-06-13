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

public class VariablesConfiguration {

    final static String DEFAULT_SEPARATOR = "_";

    private final HashMap<String, Map<String, String>> renamesMap;
    private final HashMap<String, List<String>> excludesMap;
    private final Map<String, String> sensorRenames;
    private final Map<String, String> sensorSeparator;
    private final List<AttributeRename> attributeRenameList;

    public VariablesConfiguration() {
        renamesMap = new HashMap<>();
        excludesMap = new HashMap<>();
        sensorRenames = new HashMap<>();
        sensorSeparator = new HashMap<>();
        attributeRenameList = new ArrayList<>();
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

    public void setAttributeRename(String sensorName, String variableName, String attributeName, String rename) {
        final AttributeRename newRename = new AttributeRename(sensorName, variableName, attributeName, rename);
        for (AttributeRename attributeRename : attributeRenameList) {
            if (newRename.isSameRulePath(attributeRename)) {
                throw new RuntimeException("The same rule path is not allowed to be defined twice. " +
                        "Rulepath: sensorName, variableName, attributeName = " + sensorName + ", " + variableName + ", " + attributeName + "'");
            }
        }
        attributeRenameList.add(newRename);
    }

    public String getRenamedAttributeName(String sensorName, String variableName, String attributeName) {
        final LinkedList<AttributeRename> renamer = new LinkedList<>();
        for (AttributeRename attributeRename : attributeRenameList) {
            if (attributeRename.attributeName.equals(attributeName)) {
                renamer.add(attributeRename);
            }
        }
        for (int i = renamer.size() - 1; i >= 0; i--) {
            AttributeRename attributeRename = renamer.get(i);
            if (!attributeRename.isSensorName(sensorName)) {
                renamer.remove(attributeRename);
            }
        }
        for (int i = renamer.size() - 1; i >= 0; i--) {
            AttributeRename attributeRename = renamer.get(i);
            if (!attributeRename.acceptVariableName(variableName)) {
                renamer.remove(attributeRename);
            }
        }
        for (AttributeRename attributeRename : renamer) {
            if (attributeRename.isVariableName(variableName)) {
                return attributeRename.rename;
            }
        }
        if (renamer.size() >= 1) {
            return renamer.get(0).rename;
        }
        return attributeName;
    }

    private static class AttributeRename {

        public final String sensorName;
        final String variableName;
        final String attributeName;
        final String rename;

        AttributeRename(String sensorName, String variableName, String attributeName, String rename) {
            this.sensorName = sensorName;
            this.variableName = variableName;
            this.attributeName = attributeName;
            this.rename = rename;
        }

        boolean isSensorName(String sensorName) {
            return Objects.equals(this.sensorName, sensorName);
        }

        boolean isVariableName(String variableName) {
            return Objects.equals(this.variableName, variableName);
        }

        boolean acceptVariableName(String variableName) {
            return this.variableName == null || isVariableName(variableName);
        }

        boolean isSameRulePath(AttributeRename rename) {
            return Objects.equals(this.sensorName, rename.sensorName)
                    && Objects.equals(this.variableName, rename.variableName)
                    && Objects.equals(this.attributeName, rename.attributeName);
        }
    }
}
