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

import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.post.Constants;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.util.math.FracIndex;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
    int[] mergeTimeSeriesAnalysisFile(NetcdfFileWriter netcdfFileWriter, NetcdfFile analysisFile) throws IOException, InvalidRangeException {
        final Map<Variable, Variable> analysisVariablesMap = getAnalysisVariablesMap(netcdfFileWriter, analysisFile);
        final TimeSeriesConfiguration timeSeriesConfiguration = configuration.getTimeSeriesConfiguration();

        final int[] anSourceShape = {timeSeriesConfiguration.getAnalysisSteps(), 1, 1, 1};

        final Variable analysisTime = NetCDFUtils.getVariable(analysisFile, "t");
        final Array analysisTimeArray = analysisTime.read();

        final String timeVariableName = timeSeriesConfiguration.getTimeVariableName();
        final Variable mmdTime = NetCDFUtils.getVariable(netcdfFileWriter, timeVariableName);
        final Array mmdTimeArray = extractCenterVector(mmdTime);

        final int analysisSteps = timeSeriesConfiguration.getAnalysisSteps();
        final int anPastTimeStepCount = NwpUtils.computePastTimeStepCount(analysisSteps);
        final int anFutureTimeStepCount = NwpUtils.computeFutureTimeStepCount(analysisSteps);

        final NetcdfFile netcdfFile = netcdfFileWriter.getNetcdfFile();
        final int matchupCount = NetCDFUtils.getDimensionLength(Constants.DIMENSION_NAME_MATCHUP_COUNT, netcdfFile);

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
     * @param fillValue        the value to initialize the returned center times array
     * @return the extraction center times per matchup
     * @throws IOException           when something goes wrong
     * @throws InvalidRangeException internal error
     */
    int[] mergeForecastFile(NetcdfFileWriter netcdfFileWriter, NetcdfFile forecastFile, int fillValue) throws IOException, InvalidRangeException {
        final Map<Variable, Variable> forecastVariablesMap = getForecastVariablesMap(netcdfFileWriter, forecastFile);
        final TimeSeriesConfiguration timeSeriesConfiguration = configuration.getTimeSeriesConfiguration();

        final int[] fcSourceShape = {timeSeriesConfiguration.getForecastSteps(), 1, 1, 1};

        final Variable forecastTime = NetCDFUtils.getVariable(forecastFile, "t");
        final Array forecastTimeArray = forecastTime.read();

        final String timeVariableName = timeSeriesConfiguration.getTimeVariableName();
        final Variable mmdTime = NetCDFUtils.getVariable(netcdfFileWriter, timeVariableName);
        final Array mmdTimeArray = extractCenterVector(mmdTime);

        final int forecastSteps = timeSeriesConfiguration.getForecastSteps();
        final int fcPastTimeStepCount = NwpUtils.computePastTimeStepCount(forecastSteps);
        final int fcFutureTimeStepCount = NwpUtils.computeFutureTimeStepCount(forecastSteps);

        final NetcdfFile netcdfFile = netcdfFileWriter.getNetcdfFile();
        final int matchupCount = NetCDFUtils.getDimensionLength(Constants.DIMENSION_NAME_MATCHUP_COUNT, netcdfFile);

        final int[] centerTimes = new int[matchupCount];
        Arrays.fill(centerTimes, fillValue);
        final Logger logger = FiduceoLogger.getLogger();
        for (int i = 0; i < matchupCount; i++) {
            final int targetTime = mmdTimeArray.getInt(i);
            final int timeStep = NwpUtils.nearestTimeStep(forecastTimeArray, targetTime);
            final int startIdx = timeStep - fcPastTimeStepCount;
            if (startIdx < 0 || startIdx + forecastSteps > forecastTimeArray.getSize() - 1) {
                logger.warning("Not enough time steps in NWP time series for matchup index " + i);
                continue;
            }

            final int[] sourceStart = {startIdx, 0, i, 0};
            NwpUtils.copyValues(forecastVariablesMap, netcdfFileWriter, i, sourceStart, fcSourceShape);
            centerTimes[i] = forecastTimeArray.getInt(timeStep);
        }

        return centerTimes;
    }

    /**
     * Merges the ERA-interim sensor extract analysis file into the target MMD.
     *
     * @param netcdfFileWriter the MMD file writer
     * @param analysisFile     the projected and re-gridded ERA-interim analysis file
     * @throws IOException           when something goes wrong
     * @throws InvalidRangeException internal error
     */
    void mergeSensorExtractAnalysisFile(NetcdfFileWriter netcdfFileWriter, NetcdfFile analysisFile) throws IOException, InvalidRangeException {
        final NetcdfFile netcdfFile = netcdfFileWriter.getNetcdfFile();
        final int matchupCount = NetCDFUtils.getDimensionLength(Constants.DIMENSION_NAME_MATCHUP_COUNT, netcdfFile);

        final int x_dim = NetCDFUtils.getDimensionLength("x", analysisFile);
        final int y_dim = NetCDFUtils.getDimensionLength("y", analysisFile) / matchupCount;
        //final int z_dim = NetCDFUtils.getDimensionLength("lev", analysisFile);

        final SensorExtractConfiguration sensorExtractConfiguration = configuration.getSensorExtractConfiguration();
        final String targetTimeVariableName = sensorExtractConfiguration.getTimeVariableName();
        final Variable targetTimeVariable = NetCDFUtils.getVariable(netcdfFileWriter, targetTimeVariableName);
        final Array targetTimes = targetTimeVariable.read();
        final int targetTimeFillValue = NetCDFUtils.getFillValue(targetTimeVariable).intValue();

        final Variable nwpTimeVariable = NetCDFUtils.getVariable(analysisFile, "t");
        final Array nwpTime = nwpTimeVariable.read();

        final int[] sourceShape = {1, 0, y_dim, x_dim};
        for (int i = 0; i < matchupCount; i++) {
            final int targetTime = targetTimes.getInt(i);
            if (targetTime == targetTimeFillValue) {
                continue;
            }

            final int[] sourceStart = {0, 0, i * y_dim, 0};
            final FracIndex fi = NwpUtils.getInterpolationIndex(nwpTime, targetTime);

            final List<TemplateVariable> sensorExtractVariables = templateVariables.getSensorExtractVariables();
            for (final TemplateVariable variable : sensorExtractVariables) {
                final Variable nwpVariable = NetCDFUtils.getVariable(analysisFile, variable.getOriginalName());

                final float fillValue = NetCDFUtils.getFillValue(nwpVariable).floatValue();
                final float valid_min = NetCDFUtils.getAttributeFloat(nwpVariable, "valid_min", Float.NEGATIVE_INFINITY);
                final float valid_max = NetCDFUtils.getAttributeFloat(nwpVariable, "valid_max", Float.POSITIVE_INFINITY);

                sourceStart[0] = fi.i;
                sourceShape[1] = nwpVariable.getShape(1);

                final Array slice1 = nwpVariable.read(sourceStart, sourceShape);
                sourceStart[0] = fi.i + 1;
                final Array slice2 = nwpVariable.read(sourceStart, sourceShape);
                for (int k = 0; k < slice1.getSize(); k++) {
                    final float v1 = slice1.getFloat(k);
                    final float v2 = slice2.getFloat(k);
                    final boolean invalid1 = v1 == fillValue || v1 < valid_min || v1 > valid_max;
                    final boolean invalid2 = v2 == fillValue || v2 < valid_min || v2 > valid_max;
                    if (invalid1 && invalid2) {
                        slice2.setFloat(k, fillValue);
                    } else //noinspection StatementWithEmptyBody
                        if (invalid1) {
                        // do nothing, value is already set
                    } else if (invalid2) {
                        slice2.setFloat(k, v1);
                    } else {
                        slice2.setDouble(k, (1.0 - fi.f) * v1 + fi.f * v2);
                    }
                }

                final Variable targetVariable = NetCDFUtils.getVariable(netcdfFileWriter, variable.getName());
                final int[] targetShape = targetVariable.getShape();
                targetShape[0] = 1;
                final int[] targetStart = new int[targetShape.length];
                targetStart[0] = i;
                netcdfFileWriter.write(targetVariable, targetStart, slice2.reshape(targetShape));
            }
        }
    }

    private Map<Variable, Variable> getAnalysisVariablesMap(NetcdfFileWriter netcdfFileWriter, NetcdfFile analysisFile) {
        final List<TemplateVariable> analysisVariables = templateVariables.getTimeSeriesAnalysisVariables();
        return getVariableVariableMap(netcdfFileWriter, analysisFile, analysisVariables);
    }

    private Map<Variable, Variable> getForecastVariablesMap(NetcdfFileWriter netcdfFileWriter, NetcdfFile forecastFile) {
        final List<TemplateVariable> forecastVariables = templateVariables.getTimeSeriesForecastVariables();
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
