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
 */

package com.bc.fiduceo.post.plugin.sstInsitu;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.insitu.sst_cci.SSTInsituReader;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_UNITS_NAME;
import static com.bc.fiduceo.util.TimeUtils.secondsSince1978;
import static ucar.nc2.NetcdfFile.makeValidCDLName;

class SstInsituTimeSeries extends PostProcessing {

    private static final String FILE_NAME_PATTERN_D8_D8_NC = ".*_\\d{8}_\\d{8}.nc";
    static final String INSITU_NTIME = "insitu.ntime";

    private final Configuration configuration;

    private int matchupCount;
    private String sensorType;
    private Variable fileNameVariable;
    private int filenameFieldSize;

    SstInsituTimeSeries(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        sensorType = extractSensorType(reader, configuration);
        fileNameVariable = getFileNameVariable(reader, sensorType, configuration);

        filenameFieldSize = NetCDFUtils.getDimensionLength(FiduceoConstants.FILE_NAME, reader);
        matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, reader);

        final String insituFileName = getSourceFileName(fileNameVariable, 0, filenameFieldSize, FILE_NAME_PATTERN_D8_D8_NC);

        final Reader insituReader = readerCache.getReaderFor(sensorType, Paths.get(insituFileName), configuration.processingVersion);
        addInsituVariables(writer, insituReader);
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable yVariable1D = getInsitu_Y_Variable(reader, sensorType, configuration);
        final int[] y1D = (int[]) yVariable1D.read().getStorage();
        final Variable yVariable2D = writer.findVariable(makeValidCDLName("insitu.y"));

        final Variable satMatchupVar = NetCDFUtils.getVariable(reader, configuration.matchupTimeVarName);
        final int[] satMatchupShape = satMatchupVar.getShape();
        final int xIdx = satMatchupShape[2] / 2;
        final int yIdx = satMatchupShape[1] / 2;
        // read only the center pixels
        final Array satMatchupTimeData = satMatchupVar.read(new int[]{0, yIdx, xIdx}, new int[]{satMatchupShape[0], 1, 1}).reduce();
        final int[] storage = (int[]) satMatchupTimeData.getStorage();
        final int[] satMatchup1978_1D = Arrays.stream(storage).map(v -> v - secondsSince1978).toArray();

        final Variable timeVar2D = writer.findVariable(makeValidCDLName("insitu.time"));
        final Variable dtimeVar2D = writer.findVariable(makeValidCDLName("insitu.dtime"));

        for (int i = 0; i < matchupCount; i++) {
            final String insituFileName = getSourceFileName(fileNameVariable, i, filenameFieldSize, FILE_NAME_PATTERN_D8_D8_NC);
            final SSTInsituReader insituReader = (SSTInsituReader) readerCache.getReaderFor(sensorType, Paths.get(insituFileName), configuration.processingVersion);
            Range range = computeInsituRange(y1D[i], insituReader);
            final int[] origin1D = {range.min};
            final int timeSeriesLength = getTimeSeriesLength(range);
            final int[] shape1D = {timeSeriesLength};
            final int[] origin2D = {i, 0};
            final int[] shape2D = {1, timeSeriesLength};
            final List<Variable> variables = insituReader.getVariables();
            for (Variable variable1D : variables) {
                final Array fullSrcData1D = insituReader.getSourceArray(variable1D.getShortName());
                final Array srcData1D = NetCDFUtils.section(fullSrcData1D, origin1D, shape1D);
                final String validShortName = makeValidCDLName(variable1D.getShortName());
                final Variable targetVar2D = writer.findVariable(validShortName);
                final Array targetData2D = srcData1D.reshape(shape2D);
                writer.write(targetVar2D, origin2D, targetData2D);
            }

            final Array y2D = createY2D(range, shape2D);
            writer.write(yVariable2D, origin2D, y2D);

            final int satMatchupTime = satMatchup1978_1D[i];
            final Array deltaTimes = createDeltaTime2D(satMatchupTime, timeVar2D, dtimeVar2D, origin2D, shape2D);
            writer.write(dtimeVar2D, origin2D, deltaTimes);
        }
    }

    @Override
    protected void initReaderCache() {
        readerCache = createReaderCache(getContext());
    }

    static String extractSensorType(NetcdfFile reader, Configuration configuration) {
        if (StringUtils.isNotNullAndNotEmpty(configuration.insituSensorName)) {
            return configuration.insituSensorName;
        }

        final List<Variable> variables = reader.getVariables();
        for (Variable variable : variables) {
            final String shortName = variable.getShortName();
            final int insituIndex = shortName.indexOf("_insitu.");
            if (insituIndex > 0) {
                return shortName.substring(0, insituIndex);
            }
        }
        throw new RuntimeException("Unable to extract sensor type.");
    }

    // package access for testing only tb 2018-02-14
    static Variable getFileNameVariable(NetcdfFile reader, final String sensorType, Configuration configuration) {
        if (StringUtils.isNullOrEmpty(configuration.fileNameVariableName)) {
            return NetCDFUtils.getVariable(reader, sensorType + "_file_name");
        }

        return NetCDFUtils.getVariable(reader, configuration.fileNameVariableName);
    }

    // package access for testing only tb 2018-02-14
    static Variable getInsitu_Y_Variable(NetcdfFile reader, final String sensorType, Configuration configuration) {
        if (StringUtils.isNullOrEmpty(configuration.yVariableName)) {
            return NetCDFUtils.getVariable(reader, sensorType + "_y");
        }

        return NetCDFUtils.getVariable(reader, configuration.yVariableName);
    }

    Range computeInsituRange(final int matchupPos, SSTInsituReader insituReader) {
        final String name = "insitu.time";
        final Array sourceArray = insituReader.getSourceArray(name);
        final int[] times = (int[]) sourceArray.getStorage();
        final int matchupTime = times[matchupPos];
        final int minTime_ = matchupTime - (configuration.timeRangeSeconds / 2);
        final int maxTime = minTime_ + configuration.timeRangeSeconds;
        final int minTime = minTime_ < 0 ? 0 : minTime_;
        int minIdx = matchupPos;
        int maxIdx = matchupPos;
        for (int i = matchupPos; i > 0; i--) {
            if (minTime <= times[i]) {
                minIdx = i;
            } else {
                break;
            }
        }
        for (int i = matchupPos; i < times.length; i++) {
            if (maxTime >= times[i]) {
                maxIdx = i;
            } else {
                break;
            }
        }
        return new Range(minIdx, maxIdx);
    }

    void addInsituVariables(NetcdfFileWriter writer, final Reader insituReader) throws IOException, InvalidRangeException {
        final String dimString = FiduceoConstants.MATCHUP_COUNT + " " + INSITU_NTIME;
        writer.addDimension(null, INSITU_NTIME, configuration.timeSeriesSize);
        final List<Variable> variables = insituReader.getVariables();
        addVariables(variables, dimString, writer);
        addAdditionalVariables(writer, dimString);
    }

    private Array createDeltaTime2D(int satelliteMatchupTime, Variable timeVar2D, Variable dtimeVar2D, int[] origin2D, int[] shape2D) throws IOException, InvalidRangeException {
        final Array insituTimes = timeVar2D.read(origin2D, shape2D);
        final Array deltaTimes = dtimeVar2D.read(origin2D, shape2D);
        for (int j = 0; j < insituTimes.getSize(); j++) {
            final int insituTime = insituTimes.getInt(j);
            final int deltaTime = insituTime - satelliteMatchupTime;
            deltaTimes.setInt(j, deltaTime);
        }
        return deltaTimes;
    }

    private Array createY2D(Range range, int[] shape2D) {
        final int timeSeriesLength = getTimeSeriesLength(range);
        final int[] y = new int[timeSeriesLength];
        for (int j = 0; j < y.length; j++) {
            y[j] = range.min + j;
        }
        final Array yData1D = Array.factory(y);
        return yData1D.reshape(shape2D);
    }

    private int getTimeSeriesLength(Range range) {
        return range.max - range.min + 1;
    }

    private void addVariables(List<Variable> variables, String dimString, NetcdfFileWriter writer) {
        for (Variable variable : variables) {
            Variable newVar = writer.addVariable(null, variable.getShortName(), variable.getDataType(), dimString);
            newVar.addAll(variable.getAttributes());
        }
    }

    private void addAdditionalVariables(NetcdfFileWriter writer, String dimString) {
        final Variable yVariable = writer.addVariable(null, "insitu.y", DataType.INT, dimString);
        yVariable.addAttribute(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(int.class)));

        final Variable dtimeVariable = writer.addVariable(null, "insitu.dtime", DataType.INT, dimString);
        dtimeVariable.addAttribute(new Attribute(CF_UNITS_NAME, "seconds from matchup.time"));
        dtimeVariable.addAttribute(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(int.class)));
    }

    static class Range {

        final int min;
        final int max;

        Range(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }

    static class Configuration {
        String processingVersion;
        int timeRangeSeconds;
        int timeSeriesSize;
        String matchupTimeVarName;
        String insituSensorName;
        String fileNameVariableName;
        String yVariableName;
    }
}
