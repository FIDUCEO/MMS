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
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.util.ArrayUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MmdWriter {

    private final int cacheSize;
    private final Map<String, Array> dataCacheMap;
    private final Map<String, Variable> variableMap;

    private NetcdfFileWriter netcdfFileWriter;
    private int flushCount = 0;

    public MmdWriter(int cacheSize) {
        this.cacheSize = cacheSize;
        dataCacheMap = new HashMap<>();
        variableMap = new HashMap<>();
    }

    public static String createMMDFileName(UseCaseConfig useCaseConfig, Date startDate, Date endDate) {
        final StringBuilder nameBuilder = new StringBuilder();

        nameBuilder.append(useCaseConfig.getName());
        nameBuilder.append("_");

        nameBuilder.append(useCaseConfig.getPrimarySensor().getName());
        nameBuilder.append("_");

        final List<Sensor> additionalSensors = useCaseConfig.getAdditionalSensors();
        if (additionalSensors.size() > 0) {
            for (final Sensor additionalSensor : additionalSensors) {
                nameBuilder.append(additionalSensor.getName());
                nameBuilder.append("_");
            }
        } else {
            nameBuilder.append("_");
        }

        nameBuilder.append(TimeUtils.formatToDOY(startDate));
        nameBuilder.append("_");

        nameBuilder.append(TimeUtils.formatToDOY(endDate));
        nameBuilder.append(".nc");

        return nameBuilder.toString();
    }

    public void create(File mmdFile, UseCaseConfig useCaseConfig, List<VariablePrototype> variablePrototypes, int numMatchups) throws IOException {
        netcdfFileWriter = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, mmdFile.getPath());

        createGlobalAttributes();
        createUseCaseAttributes(netcdfFileWriter, useCaseConfig);
        final List<Dimension> dimensions = useCaseConfig.getDimensions();
        createDimensions(dimensions, numMatchups);
        createExtraMmdVariablesPerSensor(dimensions);

        for (final VariablePrototype variablePrototype : variablePrototypes) {
            final Variable variable = netcdfFileWriter.addVariable(null,
                                                                   variablePrototype.getTargetVariableName(),
                                                                   DataType.getType(variablePrototype.getDataType()),
                                                                   variablePrototype.getDimensionNames());
            final List<Attribute> attributes = variablePrototype.getAttributes();
            for (Attribute attribute : attributes) {
                variable.addAttribute(attribute);
            }
        }
        netcdfFileWriter.create();
    }

    public void close() throws IOException, InvalidRangeException {
        flush();
        variableMap.clear();
        dataCacheMap.clear();
        if (netcdfFileWriter != null) {
            netcdfFileWriter.close();
            netcdfFileWriter = null;
        }
    }

    public void write(int v, String variableName, int zIndex) throws IOException, InvalidRangeException {
        final Array data = Array.factory(new int[][]{{v}});
        write(data, variableName, zIndex);
    }

    public void write(String v, String variableName, int zIndex) throws IOException, InvalidRangeException {
        final int[] shape = getVariable(variableName).getShape();
        final char[] chars = new char[shape[1]];
        v.getChars(0, v.length(), chars, 0 );
        final Array data = Array.factory(new char[][]{chars});
        write(data, variableName, zIndex);
    }

    public void write(Array data, String variableName, int stackIndex) throws IOException, InvalidRangeException {
        final Array target = getTarget(variableName);
        final Index index = target.getIndex();
        index.set(stackIndex % cacheSize);
        Array.arraycopy(data, 0, target, index.currentElement(), (int) data.getSize());
    }

    private Array getTarget(String variableName) {
        if (!dataCacheMap.containsKey(variableName)) {
            Variable variable = getVariable(variableName);
            final int[] shape = variable.getShape();
            shape[0] = cacheSize;
            dataCacheMap.put(variableName, Array.factory(variable.getDataType(), shape));
        }
        return dataCacheMap.get(variableName);
    }

    public void flush() throws IOException, InvalidRangeException {
        for (Map.Entry<String, Array> entry : dataCacheMap.entrySet()) {
            final String variableName = entry.getKey();
            final Variable variable = variableMap.get(variableName);
            Array dataToBeWritten = entry.getValue();

            final int matchupCount = variable.getShape(0);
            final int zStart = flushCount * cacheSize;
            if (zStart + cacheSize > matchupCount) {
                final int restHeight = matchupCount - zStart;
                final int[] shape = dataToBeWritten.getShape();
                shape[0] = restHeight;
                dataToBeWritten = dataToBeWritten.section(new int[shape.length], shape);
            }
            final int[] origin = {zStart, 0, 0};
            netcdfFileWriter.write(variable, origin, dataToBeWritten);
        }
        flushCount++;
    }

    static void createUseCaseAttributes(NetcdfFileWriter netcdfFileWriter, UseCaseConfig useCaseConfig) {
        netcdfFileWriter.addGroupAttribute(null, new Attribute(
                    "comment",
                    "The MMD file is created based on the use case configuration documented in the attribute 'use-case-configuration'."
        ));
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        useCaseConfig.store(outputStream);
        netcdfFileWriter.addGroupAttribute(null, new Attribute(
                    "use-case-configuration",
                    outputStream.toString()
        ));
    }

    private Variable getVariable(String variableName) {
        if (!variableMap.containsKey(variableName)) {
            variableMap.put(variableName, netcdfFileWriter.findVariable(variableName));
        }
        return variableMap.get(variableName);
    }

    private void createExtraMmdVariablesPerSensor(List<Dimension> dimensions) {
        for (Dimension dimension : dimensions) {
            final String sensorName = dimension.getName();
            netcdfFileWriter.addVariable(null, sensorName + "_x", DataType.INT, "matchup_count");
            netcdfFileWriter.addVariable(null, sensorName + "_y", DataType.INT, "matchup_count");
            netcdfFileWriter.addVariable(null, sensorName + "_file_name", DataType.CHAR, "matchup_count file_name");
            final String yDimension = getDimensionNameNy(sensorName);
            final String xDimension = getDimensionNameNx(sensorName);
            netcdfFileWriter.addVariable(null, sensorName + "_acquisition_time", DataType.INT, "matchup_count " + yDimension + " " + xDimension);
        }
    }

    private void createGlobalAttributes() {
        addGlobalAttribute("title", "FIDUCEO multi-sensor match-up dataset (MMD)");
        addGlobalAttribute("institution", "Brockmann Consult GmbH");
        addGlobalAttribute("contact", "Tom Block (tom.block@brockmann-consult.de)");
        addGlobalAttribute("license", "This dataset is released for use under CC-BY licence and was developed in the EC FIDUCEO project \"Fidelity and Uncertainty in Climate Data Records from Earth Observations\". Grant Agreement: 638822.");
        addGlobalAttribute("creation_date", TimeUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
    }

    private void addGlobalAttribute(String name, String val) {
        netcdfFileWriter.addGroupAttribute(null, new Attribute(name, val));
    }

    private void createDimensions(List<Dimension> dimensions, int numMatchups) {
        for (final Dimension dimension : dimensions) {
            netcdfFileWriter.addDimension(null, getDimensionNameNx(dimension.getName()), dimension.getNx());
            netcdfFileWriter.addDimension(null, getDimensionNameNy(dimension.getName()), dimension.getNy());
        }
        netcdfFileWriter.addDimension(null, "file_name", 128);
        netcdfFileWriter.addDimension(null, "matchup_count", numMatchups);
    }

    private String getDimensionNameNy(String sensorName) {
        return sensorName + "_ny";
    }

    private String getDimensionNameNx(String sensorName) {
        return sensorName + "_nx";
    }
}