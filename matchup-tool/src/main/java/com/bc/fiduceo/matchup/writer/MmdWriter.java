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
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class MmdWriter {

    private NetcdfFileWriter netcdfFileWriter;

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

    public void create(File mmdFile, List<Dimension> dimensions, List<VariablePrototype> variablePrototypes, int numMatchups) throws IOException {
        netcdfFileWriter = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, mmdFile.getPath());

        createGlobalAttributes();
        createDimensions(dimensions, numMatchups);
        createXYVariablesPerSensor(dimensions);

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

    public void close() throws IOException {
        if (netcdfFileWriter != null) {
            netcdfFileWriter.close();
            netcdfFileWriter = null;
        }
    }

    public void write(int v, String variableName, int stackIndex) throws IOException, InvalidRangeException {
        final Variable variable = netcdfFileWriter.findVariable(variableName);
        final int[] origin = {stackIndex};
        final Array a = variable.read(origin, new int[]{1});
        a.setInt(0, v);
        netcdfFileWriter.write(variable, origin, a);
    }

    public void write(Array data, String variableName, int stackIndex) throws IOException, InvalidRangeException {
        final Variable variable = netcdfFileWriter.findVariable(variableName);
        final int[] shape = data.getShape();
        final Array dataD3 = data.reshape(new int[]{1, shape[0], shape[1]});
        netcdfFileWriter.write(variable, new int[]{stackIndex, 0, 0}, dataD3);
    }

    void createXYVariablesPerSensor(List<Dimension> dimensions) {
        for (Dimension dimension : dimensions) {
            final String dimensionName = dimension.getName();
            netcdfFileWriter.addVariable(null, dimensionName + "_x", DataType.INT, "matchup_count");
            netcdfFileWriter.addVariable(null, dimensionName + "_y", DataType.INT, "matchup_count");
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
            String dimensionName = dimension.getName() + "_nx";
            netcdfFileWriter.addDimension(null, dimensionName, dimension.getNx());

            dimensionName = dimension.getName() + "_ny";
            netcdfFileWriter.addDimension(null, dimensionName, dimension.getNy());
        }
        netcdfFileWriter.addDimension(null, "matchup_count", numMatchups);
    }
}
