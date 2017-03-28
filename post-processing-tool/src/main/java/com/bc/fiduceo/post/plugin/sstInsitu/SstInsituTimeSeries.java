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

import com.bc.fiduceo.post.Constants;
import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.insitu.SSTInsituReader;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.bc.fiduceo.util.TimeUtils.secondsSince1978;
import static ucar.nc2.NetcdfFile.makeValidCDLName;

class SstInsituTimeSeries extends PostProcessing {

    static final String FILE_NAME_PATTERN_D8_D8_NC = ".*_\\d{8}_\\d{8}.nc";
    static final String INSITU_NTIME = "insitu.ntime";
    static final String MATCHUP = "matchup";

    // @todo 3 tb/** maybe move this to a configuration class? 2016-12-23
    final String processingVersion;
    final int timeRangeSeconds;
    final int timeSeriesSize;
    final String matchupTimeVarName;

    private int matchupCount;
    private String sensorType;
    private Variable fileNameVariable;
    private int filenameFieldSize;
    private InsituReaderCache insituReaderCache;

    SstInsituTimeSeries(String processingVersion, int timeRangeSeconds, int timeSeriesSize, String matchupTimeVarName) {
        this.processingVersion = processingVersion;
        this.timeRangeSeconds = timeRangeSeconds;
        this.timeSeriesSize = timeSeriesSize;
        this.matchupTimeVarName = matchupTimeVarName;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        sensorType = extractSensorType(reader);
        fileNameVariable = getInsituFileNameVariable(reader, sensorType);
        filenameFieldSize = NetCDFUtils.getDimensionLength("file_name", reader);
        matchupCount = NetCDFUtils.getDimensionLength(Constants.MATCHUP_COUNT, reader);
        final String insituFileName = getInsituFileName(fileNameVariable, 0, filenameFieldSize);
        insituReaderCache = new InsituReaderCache(getContext());
        final Reader insituReader = insituReaderCache.getInsituFileOpened(insituFileName, sensorType, processingVersion);
        addInsituVariables(writer, insituReader);
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable yVariable1D = getInsitu_Y_Variable(reader, sensorType);
        final int[] y1D = (int[]) yVariable1D.read().getStorage();
        final Variable yVariable2D = writer.findVariable(makeValidCDLName("insitu.y"));

        final Variable satMatchupVar = NetCDFUtils.getVariable(reader, matchupTimeVarName);
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
            final String insituFileName = getInsituFileName(fileNameVariable, i, filenameFieldSize);
            final SSTInsituReader insituReader = (SSTInsituReader) insituReaderCache.getInsituFileOpened(insituFileName, sensorType, processingVersion);
            Range range = computeInsituRange(y1D[i], insituReader);
            final int[] origin1D = {range.min};
            final int timeSeriesLength = getTimeSeriesLength(range);
            final int[] shape1D = {timeSeriesLength};
            final int[] origin2D = {i, 0};
            final int[] shape2D = {1, timeSeriesLength};
            final List<Variable> variables = insituReader.getVariables();
            for (Variable variable1D : variables) {
                final Array fullSrcData1D = insituReader.getSourceArray(variable1D.getShortName());
                final Array srcData1D = fullSrcData1D.section(origin1D, shape1D);
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

    static String getInsituFileName(Variable fileNameVar, int position, int filenameSize) throws IOException, InvalidRangeException {
        final Array nameArray = fileNameVar.read(new int[]{position, 0}, new int[]{1, filenameSize});
        final String insituFileName = String.valueOf((char[]) nameArray.getStorage()).trim();
        if (!insituFileName.matches(FILE_NAME_PATTERN_D8_D8_NC)) {
            throw new RuntimeException("The insitu file name '" + insituFileName + "' does not match the regular expression '" + FILE_NAME_PATTERN_D8_D8_NC + "'");
        }
        return insituFileName;
    }

    static Variable getInsituFileNameVariable(NetcdfFile reader, final String sensorType) {
        return getVariable(reader, sensorType + "_file_name");
    }

    private static Variable getInsitu_Y_Variable(NetcdfFile reader, final String sensorType) {
        return getVariable(reader, sensorType + "_y");
    }

    private static Variable getVariable(NetcdfFile reader, final String varName) {
        final Variable fileNameVar = reader.findVariable(varName);
        if (fileNameVar == null) {
            throw new RuntimeException("Variable '" + varName + "' does not exist.");
        }
        return fileNameVar;
    }

    static String extractSensorType(NetcdfFile reader) {
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

    Range computeInsituRange(int matchupPos, SSTInsituReader insituReader) throws IOException, InvalidRangeException {
        final String name = "insitu.time";
        final Array sourceArray = insituReader.getSourceArray(name);
        final int[] times = (int[]) sourceArray.getStorage();
        final int matchupTime = times[matchupPos];
        final int minTime_ = matchupTime - (timeRangeSeconds / 2);
        final int maxTime = minTime_ + timeRangeSeconds;
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
        final String dimString = MATCHUP + " " + INSITU_NTIME;
        writer.addDimension(null, MATCHUP, matchupCount);
        writer.addDimension(null, INSITU_NTIME, timeSeriesSize);
        final List<Variable> variables = insituReader.getVariables();
//        addVariablesAccordingToOldSstFileVersion(variables, dimString, writer);
        addVariables(variables, dimString, writer);
        addAditionalVariables(writer, dimString);
    }

    private void addVariables(List<Variable> variables, String dimString, NetcdfFileWriter writer) {
        for (Variable variable : variables) {
            Variable newVar = writer.addVariable(null, variable.getShortName(), variable.getDataType(), dimString);
            newVar.addAll(variable.getAttributes());
        }
    }

    private void addAditionalVariables(NetcdfFileWriter writer, String dimString) {
        final Variable yVariable = writer.addVariable(null, "insitu.y", DataType.INT, dimString);
        yVariable.addAttribute(new Attribute("_FillValue", NetCDFUtils.getDefaultFillValue(int.class)));

        final Variable dtimeVariable = writer.addVariable(null, "insitu.dtime", DataType.INT, dimString);
        dtimeVariable.addAttribute(new Attribute("units", "seconds from matchup.time"));
        dtimeVariable.addAttribute(new Attribute("_FillValue", NetCDFUtils.getDefaultFillValue(int.class)));
    }

    static class Range {

        final int min;
        final int max;

        Range(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }
}
