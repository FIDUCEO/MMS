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

import static ucar.nc2.NetcdfFile.makeValidCDLName;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.WindowArrayFactory;
import com.bc.fiduceo.reader.insitu.SSTInsituReader;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

class SstInsituTimeSeries extends PostProcessing {

    static final String FILE_NAME_PATTERN_D8_D8_NC = ".*_\\d{8}_\\d{8}.nc";
    static final String INSITU_NTIME = "insitu.ntime";
    static final String MATCHUP_COUNT = "matchup_count";
    static final String MATCHUP = "matchup";

    // @todo 3 tb/** maybe move this to a configuration class? 2016-12-23
    final String processingVersion;
    final int timeRangeSeconds;
    final int timeSeriesSize;

    private int matchupCount;
    private String sensorType;
    private Variable fileNameVariable;
    private int filenameFieldSize;
    private InsituReaderCache insituReaderCache;

    SstInsituTimeSeries(String processingVersion, int timeRangeSeconds, int timeSeriesSize) {
        this.processingVersion = processingVersion;
        this.timeRangeSeconds = timeRangeSeconds;
        this.timeSeriesSize = timeSeriesSize;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        sensorType = extractSensorType(reader);
        fileNameVariable = getInsituFileNameVariable(reader, sensorType);
        filenameFieldSize = findDimensionMandatory(reader, "file_name").getLength();
        matchupCount = findDimensionMandatory(reader, MATCHUP_COUNT).getLength();
        final String insituFileName = getInsituFileName(fileNameVariable, 0, filenameFieldSize);
        insituReaderCache = new InsituReaderCache(getContext());
        final Reader insituReader = insituReaderCache.getInsituFileOpened(insituFileName, sensorType, processingVersion);
        addInsituVariables(writer, insituReader);
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable insituY_Var = getInsitu_Y_Variable(reader, sensorType);
        final int[] ys = (int[]) insituY_Var.read().getStorage();
        for (int i = 0; i < matchupCount; i++) {
            final String insituFileName = getInsituFileName(fileNameVariable, 0, filenameFieldSize);
            final SSTInsituReader insituReader = (SSTInsituReader) insituReaderCache.getInsituFileOpened(insituFileName, sensorType, processingVersion);
            Range range = computeInsituRange(ys[i], insituReader);
            final int[] origin = {range.min};
            final int[] shape = {range.max - range.min + 1};

//            final Interval interval = new Interval(range.max - range.min + 1, 1);
            final int[] targetOrigin = {i, 0};
            final int[] shape2D = {1, shape[0]};

            final List<Variable> variables = insituReader.getVariables();
            for (Variable variable : variables) {
//                final Array data = insituReader.readRaw(0, range.min, interval, variable.getShortName());
                final Array srcdata = insituReader.getSourceArray(variable.getShortName());
                final Array data = srcdata.section(origin, shape);
                final String validShortName = makeValidCDLName(variable.getShortName());
                final Variable targetVar = writer.findVariable(validShortName);
                final Array targetData = data.reshape(shape2D);
                writer.write(targetVar, targetOrigin, targetData);
            }
            final Variable variable = writer.findVariable(makeValidCDLName("insitu.y"));
            final int[] y = new int[shape[0]];
            for (int j = 0; j < y.length; j++) {
                y[j] = range.min + j;
            }
            final Array data = Array.factory(y);
            final Array targetData = data.reshape(shape2D);
            writer.write(variable, targetOrigin, targetData);
        }
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
        return getVariable(reader, sensorType, "_file_name");
    }

    static Variable getInsitu_Y_Variable(NetcdfFile reader, final String sensorType) {
        return getVariable(reader, sensorType, "_y");
    }

    static Variable getVariable(NetcdfFile reader, String sensorType, String varName) {
        final String fileNameVarName = sensorType + varName;

        final Variable fileNameVar = reader.findVariable(fileNameVarName);
        if (fileNameVar == null) {
            throw new RuntimeException("Variable '" + fileNameVarName + "' does not exist.");
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

    static Dimension findDimensionMandatory(NetcdfFile reader, String dimName) {
        final Dimension dim = reader.findDimension(dimName);
        if (dim == null) {
            throw new RuntimeException("Dimension '" + dimName + "' does not exist.");
        }
        return dim;
    }

    Range computeInsituRange(int matchupPos, Reader insituReader) throws IOException, InvalidRangeException {
        final List<Variable> variables = insituReader.getVariables();
        final String name = "insitu.time";
        final int[] times = readInts(variables, name);
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

    private void addVariablesAccordingToOldSstFileVersion(List<Variable> variables, String dimString, NetcdfFileWriter writer) {
        for (Variable variable : variables) {
            String shortName = variable.getShortName();
            Variable newVar;
            if (shortName.endsWith(".lat")) {
                shortName = shortName.replace(".lat", ".latitude");
                newVar = writer.addVariable(null, shortName, variable.getDataType(), dimString);
                newVar.addAll(variable.getAttributes());
                newVar.addAttribute(new Attribute("valid_min", -90.0f));
                newVar.addAttribute(new Attribute("valid_max", 90.0f));
            } else if (shortName.endsWith(".lon")) {
                shortName = shortName.replace(".lon", ".longitude");
                newVar = writer.addVariable(null, shortName, variable.getDataType(), dimString);
                newVar.addAll(variable.getAttributes());
                newVar.addAttribute(new Attribute("valid_min", -180.0f));
                newVar.addAttribute(new Attribute("valid_max", 180.0f));
            } else if (shortName.endsWith(".sea_surface_temperature")) {
                newVar = writer.addVariable(null, shortName, DataType.SHORT, dimString);
                newVar.addAttribute(new Attribute("units", "K"));
                newVar.addAttribute(new Attribute("add_offset", 293.15));
                newVar.addAttribute(new Attribute("scale_factor", 0.001));
                newVar.addAttribute(new Attribute("_FillValue", (short) -32768));
                newVar.addAttribute(new Attribute("valid_min", (short) -22000));
                newVar.addAttribute(new Attribute("valid_max", (short) 31850));
                newVar.addAttribute(variable.findAttribute("long_name"));
            } else if (shortName.endsWith(".sst_uncertainty")) {
                newVar = writer.addVariable(null, shortName, DataType.SHORT, dimString);
                newVar.addAttribute(new Attribute("units", "K"));
                newVar.addAttribute(new Attribute("add_offset", 0.0));
                newVar.addAttribute(new Attribute("scale_factor", 0.001));
                newVar.addAttribute(new Attribute("_FillValue", (short) -32768));
                newVar.addAttribute(new Attribute("valid_min", (short) -22000));
                newVar.addAttribute(new Attribute("valid_max", (short) 22000));
                newVar.addAttribute(variable.findAttribute("long_name"));
            } else {
                newVar = writer.addVariable(null, shortName, variable.getDataType(), dimString);
                newVar.addAll(variable.getAttributes());
            }
        }
    }

    private int[] readInts(List<Variable> variables, String name) throws IOException {
        for (Variable variable : variables) {
            if (variable.getShortName().equals(name)) {
                return (int[]) variable.read().getStorage();
            }
        }
        return null;
    }

    public static class Range {

        public final int min;
        public final int max;

        public Range(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }
}
