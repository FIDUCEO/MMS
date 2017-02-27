/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.post.plugin.nwp;

import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FileMerger {

    private final Configuration configuration;
    private final TemplateVariables templateVariables;

    FileMerger(Configuration configuration, TemplateVariables templateVariables) {
        this.configuration = configuration;
        this.templateVariables = templateVariables;
    }

    /**
     * Merges the ERA-interim analysis file into the target MMD. Returns the NWP data center times per matchup as seconds since 1970.
     *
     * @param netcdfFileWriter the MMD file writer
     * @param analysisFile     the projected and re-gridded ERA-interim analysis file
     * @return the extraction center times per matchup
     * @throws IOException           when something goes wrong
     * @throws InvalidRangeException internal error
     */
    int[] mergeAnalysisFile(NetcdfFileWriter netcdfFileWriter, NetcdfFile analysisFile) throws IOException, InvalidRangeException {
        final Map<Variable, Variable> analysisVariablesMap = getAnalysisVariablesMap(netcdfFileWriter, analysisFile);
        final int[] anSourceShape = {configuration.getAnalysisSteps(), 1, 1, 1};

        final Variable analysisTime = NetCDFUtils.getVariable(analysisFile, "t");
        final Array analysisTimeArray = analysisTime.read();

        final String timeVariableName = configuration.getTimeVariableName();
        final Variable mmdTime = NetCDFUtils.getVariable(netcdfFileWriter, timeVariableName);
        final Array mmdTimeArray = extractCenterVector(mmdTime);

        final int analysisSteps = configuration.getAnalysisSteps();
        final int anPastTimeStepCount = NwpUtils.computePastTimeStepCount(analysisSteps);
        final int anFutureTimeStepCount = NwpUtils.computeFutureTimeStepCount(analysisSteps);

        final NetcdfFile netcdfFile = netcdfFileWriter.getNetcdfFile();
        final int matchupCount = NetCDFUtils.getDimensionLength("matchup_count", netcdfFile);

        final int[] centerTimes = new int[matchupCount];
        for (int i = 0; i < matchupCount; i++) {
            final int targetTime = mmdTimeArray.getInt(i);
            final int timeStep = NwpUtils.nearestTimeStep(analysisTimeArray, targetTime);
            if (timeStep - anPastTimeStepCount < 0 || timeStep + anFutureTimeStepCount > analysisTimeArray.getSize() - 1) {
                throw new RuntimeException("Not enough time steps in NWP time series.");
            }

            final int[] sourceStart = {timeStep - anPastTimeStepCount, 0, i, 0};
            NwpUtils.copyValues(analysisVariablesMap, netcdfFileWriter, i, sourceStart, anSourceShape);
            centerTimes[i] = analysisTimeArray.getInt(timeStep);
        }

        return centerTimes;
    }

    static Array extractCenterVector(Variable variable) throws IOException, InvalidRangeException {
        final int rank = variable.getRank();
        if (rank == 1) {
            return variable.read();
        } else if (rank == 3) {
            final int[] shape = variable.getShape();
            final int[] offset = {0, shape[1] / 2, shape[2] / 2};
            shape[1] = 1;
            shape[2] = 1;
            return variable.read(offset, shape);
        }

        throw new RuntimeException("unsupported rank of time variable: " + variable.getFullName());

    }

    /**
     * Merges the ERA-interim forecast file into the target MMD. Returns the NWP data center times per matchup as seconds since 1970.
     *
     * @param netcdfFileWriter the MMD file writer
     * @param forecastFile     the projected and re-gridded ERA-interim forecast file
     * @return the extraction center times per matchup
     * @throws IOException           when something goes wrong
     * @throws InvalidRangeException internal error
     */
    int[] mergeForecastFile(NetcdfFileWriter netcdfFileWriter, NetcdfFile forecastFile) throws IOException, InvalidRangeException {
        final Map<Variable, Variable> forecastVariablesMap = getForecastVariablesMap(netcdfFileWriter, forecastFile);
        final int[] fcSourceShape = {configuration.getForecastSteps(), 1, 1, 1};

        final Variable forecastTime = NetCDFUtils.getVariable(forecastFile, "t");
        final Array forecastTimeArray = forecastTime.read();

        final String timeVariableName = configuration.getTimeVariableName();
        final Variable mmdTime = NetCDFUtils.getVariable(netcdfFileWriter, timeVariableName);
        final Array mmdTimeArray = extractCenterVector(mmdTime);

        final int forecastSteps = configuration.getForecastSteps();
        final int fcPastTimeStepCount = NwpUtils.computePastTimeStepCount(forecastSteps);
        final int fcFutureTimeStepCount = NwpUtils.computeFutureTimeStepCount(forecastSteps);

        final NetcdfFile netcdfFile = netcdfFileWriter.getNetcdfFile();
        final int matchupCount = NetCDFUtils.getDimensionLength("matchup_count", netcdfFile);

        final int[] centerTimes = new int[matchupCount];
        for (int i = 0; i < matchupCount; i++) {
            final int targetTime = mmdTimeArray.getInt(i);
            final int timeStep = NwpUtils.nearestTimeStep(forecastTimeArray, targetTime);
            if (timeStep - fcPastTimeStepCount < 0 || timeStep + fcFutureTimeStepCount > forecastTimeArray.getSize() - 1) {
                throw new RuntimeException("Not enough time steps in NWP time series.");
            }

            final int[] sourceStart = {timeStep - fcPastTimeStepCount, 0, i, 0};
            NwpUtils.copyValues(forecastVariablesMap, netcdfFileWriter, i, sourceStart, fcSourceShape);
            centerTimes[i] = forecastTimeArray.getInt(timeStep);
        }

        return centerTimes;
    }

    private Map<Variable, Variable> getAnalysisVariablesMap(NetcdfFileWriter netcdfFileWriter, NetcdfFile analysisFile) {
        final List<TemplateVariable> analysisVariables = templateVariables.getAnalysisVariables();
        return getVariableVariableMap(netcdfFileWriter, analysisFile, analysisVariables);
    }

    private Map<Variable, Variable> getForecastVariablesMap(NetcdfFileWriter netcdfFileWriter, NetcdfFile forecastFile) {
        final List<TemplateVariable> forecastVariables = templateVariables.getForecastVariables();
        return getVariableVariableMap(netcdfFileWriter, forecastFile, forecastVariables);
    }

    private Map<Variable, Variable> getVariableVariableMap(NetcdfFileWriter netcdfFileWriter, NetcdfFile analysisFile, List<TemplateVariable> templateVariables) {
        final Map<Variable, Variable> variablesMap = new HashMap<>();
        for (final TemplateVariable analysisVariable : templateVariables) {
            final String targetName = analysisVariable.getName();
            final Variable targetVariable = NetCDFUtils.getVariable(netcdfFileWriter, targetName);

            final String originalName = analysisVariable.getOriginalName();
            final Variable sourceVariable = NetCDFUtils.getVariable(analysisFile, originalName);

            variablesMap.put(targetVariable, sourceVariable);
        }
        return variablesMap;
    }
}
