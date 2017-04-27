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
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IOVariablesList {

    private static final String SAMPLE_SET_IO_VARIABLES = "SampleSetIOVariables";

    private final Map<String, List<IOVariable>> ioVariablesMap;
    private final Map<String, ReaderContainer> readerContainerMap;
    private final ReaderFactory readerFactory;

    public IOVariablesList(ReaderFactory readerFactory) {
        ioVariablesMap = new HashMap<>();
        readerContainerMap = new HashMap<>();
        this.readerFactory = readerFactory;
    }

    public void setReaderContainer(String sensorName, ReaderContainer container) {
        readerContainerMap.put(sensorName, container);
    }

    public ReaderContainer getReaderContainer(String sensorName) {
        final ReaderContainer container = readerContainerMap.get(sensorName);
        if (container == null) {
            throw new RuntimeException("Requested ReaderContainer of unconfigured type: " + sensorName);
        }
        return container;
    }

    void setReaderAndPath(String sensorName, Reader reader, Path path, String processingVersion) {
        final ReaderContainer container = readerContainerMap.get(sensorName);
        if (container == null) {
            throw new RuntimeException("Invalid sensor name requested: " + sensorName);
        }

        container.setReader(reader);
        container.setSourcePath(path);
        container.setProcessingVersion(processingVersion);
    }

    public void close() throws IOException {
        final Set<Map.Entry<String, ReaderContainer>> entrySet = readerContainerMap.entrySet();
        for (final Map.Entry<String, ReaderContainer> entry : entrySet) {
            final ReaderContainer container = entry.getValue();
            container.getReader().close();
        }
    }

    public void addSampleSetVariable(SampleSetIOVariable variable) {
        add(variable, SAMPLE_SET_IO_VARIABLES);
    }

    List<SampleSetIOVariable> getSampleSetIOVariables() {
        final List<SampleSetIOVariable> sampleSetVariables = (List) getVariablesFor(SAMPLE_SET_IO_VARIABLES);
        return Collections.unmodifiableList(sampleSetVariables);
    }

    public void extractVariables(final String sensorName, Path filePath, Dimension dimension, VariablesConfiguration variablesConfiguration) throws IOException {
        final Map<String, String> renamings = variablesConfiguration.getRenames(sensorName);
        final List<String> excludes = variablesConfiguration.getExcludes(sensorName);
        final Map<String, String> sensorRenames = variablesConfiguration.getSensorRenames();
        final String separator = variablesConfiguration.getSeparator(sensorName);

        final List<IOVariable> ioVariables;
        if (!ioVariablesMap.containsKey(sensorName)) {
            ioVariablesMap.put(sensorName, new ArrayList<>());
        }
        ioVariables = ioVariablesMap.get(sensorName);

        final ReaderContainer readerContainer = new ReaderContainer();
        setReaderContainer(sensorName, readerContainer);

        try (final Reader reader = readerFactory.getReader(sensorName)) {
            reader.open(filePath.toFile());
            readerContainer.setReader(reader);

            final String dimensionNames = createDimensionNames(dimension);
            final List<Variable> variables = reader.getVariables();
            final String targetSensorName;
            if (sensorRenames.containsKey(sensorName)) {
                targetSensorName = sensorRenames.get(sensorName);
            } else {
                targetSensorName = sensorName;
            }

            for (final Variable variable : variables) {
                final String shortName = variable.getShortName();
                if (excludes.contains(shortName)) {
                    continue;
                }
                final WindowReadingIOVariable ioVariable = new WindowReadingIOVariable(readerContainer);
                ioVariable.setSourceVariableName(shortName);
                final String targetVariableName;
                if (renamings.containsKey(shortName)) {
                    targetVariableName = targetSensorName + separator + renamings.get(shortName);
                } else {
                    targetVariableName = targetSensorName + separator + shortName;
                }
                ioVariable.setTargetVariableName(targetVariableName);
                ioVariable.setDataType(variable.getDataType().toString());
                ioVariable.setDimensionNames(dimensionNames);
                ioVariable.setAttributes(getAttributeClones(variable, sensorName, variablesConfiguration));

                ioVariables.add(ioVariable);
            }
        } catch (InvalidRangeException e) {
            throw new IOException(e.getMessage());
        }
    }

    public List<IOVariable> get() {
        final ArrayList<IOVariable> allVariables = new ArrayList<>();
        for (List<IOVariable> ioVariables : ioVariablesMap.values()) {
            allVariables.addAll(ioVariables);
        }
        return allVariables;
    }

    /**
     * Returns a list of {@link IOVariable} associated with the given sensor name.
     * If there are no associated {@link IOVariable}s, an empty list will be returned.
     *
     * @param sensorName the name of the sensor
     *
     * @return a list of {@link IOVariable}
     */
    public List<IOVariable> getVariablesFor(String sensorName) {
        if (ioVariablesMap.containsKey(sensorName)) {
            return ioVariablesMap.get(sensorName);
        }
        return new ArrayList<>();
    }

    public void add(IOVariable ioVariable, String sensorName) {
        List<IOVariable> sensorVariables = ioVariablesMap.get(sensorName);
        if (sensorVariables == null) {
            sensorVariables = new ArrayList<>();
            ioVariablesMap.put(sensorName, sensorVariables);
        }
        sensorVariables.add(ioVariable);
    }

    public List<String> getSensorNames() {
        final ArrayList<String> sensorNamesList = new ArrayList<>();
        final Set<String> keySet = ioVariablesMap.keySet();
        sensorNamesList.addAll(keySet);
        return sensorNamesList;
    }

    static List<Attribute> getAttributeClones(Variable variable, String sensorName, VariablesConfiguration variablesConfiguration) {
        final String variableName = variable.getShortName();
        final List<Attribute> attributes = variable.getAttributes();
        final ArrayList<Attribute> newAttributes = new ArrayList<>();
        for (Attribute attribute : attributes) {
            final String attName = attribute.getShortName();
            if (attribute.getFullName().startsWith("_Chunk")) {
                continue;
            }
            final Array attVals = attribute.getValues().copy();
            final String cloneAttName = variablesConfiguration.getRenamedAttributeName(sensorName, variableName, attName);
            final Attribute newAttribute = new Attribute(cloneAttName, attVals);
            newAttributes.add(newAttribute);
        }
        return newAttributes;
    }

    // package access for testing only tb 2016-04-12
    static String createDimensionNames(Dimension dimension) {
        final String dimensionName = dimension.getName();
        final String separator = " ";
        return "matchup_count" + separator + dimensionName + "_ny" + separator + dimensionName + "_nx";
    }
}
