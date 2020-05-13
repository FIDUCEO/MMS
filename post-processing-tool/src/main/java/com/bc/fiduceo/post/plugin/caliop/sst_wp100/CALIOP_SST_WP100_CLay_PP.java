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

package com.bc.fiduceo.post.plugin.caliop.sst_wp100;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.caliop.CALIOP_L2_VFM_Reader;
import com.bc.fiduceo.reader.caliop.CALIOP_SST_WP100_CLay_ReaderPlugin;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CALIOP_SST_WP100_CLay_PP extends PostProcessing {

    private static final String DIM_NAME_VFM_NY = "caliop_vfm-cal_ny";
    private static final String CLAY_SENSOR_NAME = CALIOP_SST_WP100_CLay_ReaderPlugin.SENSOR_NAME;
    
    static final String DIM_NAME_CLAY_NX = "caliop_clay-cal_nx";
    static final String DIM_NAME_CLAY_NX_10 = "caliop_clay-cal_nx_10";
    static final String DIM_NAME_CLAY_NY = "caliop_clay-cal_ny";

    final String variableName_caliopVFM_fileName;
    final String variableName_caliopVFM_y;
    final String processingVersion;
    final String variablePrefix;

    private final Map<String, Variable> variableMap;

    private Variable fileNameVariableVFM;
    private int filenameFieldSize;
    private int ny;
    private String targetVarNameAcquisitionTime;
    private String targetVarNameFileName;
    private String targetVarNameProcessingVersion;
    private String targetVarNameX;
    private String targetVarNameY;

    CALIOP_SST_WP100_CLay_PP(String variableName_caliopVFM_fileName,
                                    String variableName_caliopVFM_y,
                                    String processingVersion,
                                    String variablePrefix) {
        this.variableName_caliopVFM_fileName = variableName_caliopVFM_fileName;
        this.variableName_caliopVFM_y = variableName_caliopVFM_y;
        this.processingVersion = processingVersion;
        this.variablePrefix = variablePrefix;
        this.variableMap = new HashMap<>();
    }

    private void writeString(NetcdfFileWriter writer, Variable var, int pos, String str) throws IOException, InvalidRangeException {
        final int[] shape = var.getShape();
        final char[] chars = new char[shape[1]];
        str.getChars(0, str.length(), chars, 0);
        final Array data = NetCDFUtils.create(new char[][]{chars});
        writer.write(var, new int[]{pos, 0}, data);
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        fileNameVariableVFM = NetCDFUtils.getVariable(reader, variableName_caliopVFM_fileName);
        filenameFieldSize = NetCDFUtils.getDimensionLength(FiduceoConstants.FILE_NAME, reader);
        ny = NetCDFUtils.getDimensionLength(DIM_NAME_VFM_NY, reader);

        final String vfmSourceFileName = getSourceFileName(0);
        final String cLaySourceFileName = toCLaySourceFileName(vfmSourceFileName);
        final Reader caliopCLayReader = readerCache.getReaderFor(CLAY_SENSOR_NAME, Paths.get(cLaySourceFileName), processingVersion);

        insertDimensions(writer, ny);
        addVariables(writer, caliopCLayReader);
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable vfm_Y_Var = NetCDFUtils.getVariable(reader, variableName_caliopVFM_y);
        final int[] yArr = (int[]) vfm_Y_Var.read().get1DJavaArray(int.class);

        final Variable targetVarAT = NetCDFUtils.getVariable(writer, targetVarNameAcquisitionTime);
        final Variable targetVarPV = NetCDFUtils.getVariable(writer, targetVarNameProcessingVersion);
        final Variable targetVarFN = NetCDFUtils.getVariable(writer, targetVarNameFileName);
        final Variable targetVarX = NetCDFUtils.getVariable(writer, targetVarNameX);
        final Variable targetVarY = NetCDFUtils.getVariable(writer, targetVarNameY);

        final String sensorName = CALIOP_SST_WP100_CLay_ReaderPlugin.SENSOR_NAME;
        final int mcSize = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, reader);

        final Interval interval_1 = new Interval(1, ny);
        final Interval interval_10 = new Interval(10, ny);

        for (int mu = 0; mu < mcSize; mu++) {
            final int[] writeOrigin = {mu, 0, 0};
            final int y = yArr[mu];
            String sourceFileName = getSourceFileName(mu);
            sourceFileName = toCLaySourceFileName(sourceFileName);
            final Reader caliopReader = readerCache.getReaderFor(sensorName, Paths.get(sourceFileName), processingVersion);
            final List<Variable> variables = caliopReader.getVariables();
            int c = 0;
            for (Variable variable : variables) {
                final String shortName = variable.getShortName();
                final int shape1 = variable.getShape(1);
                final Variable targetVar = variableMap.get(shortName);
                if (mu == 0) {
                    System.out.println(++c);
                    System.out.println("CAL ... shortName = " + shortName);
                    System.out.println("        targetName = " + targetVar.getShortName());
                    System.out.println("        shape1 = " + shape1);
                }
                final Interval interval;
                final int centerX;
                if (shape1 == 1) {
                    interval = interval_1;
                    centerX = 0;
                } else {
                    centerX = 5;
                    interval = interval_10;
                }

                final Array values = caliopReader.readRaw(centerX, y, interval, shortName);
                final Array reshaped = values.reshape(new int[]{1, interval.getY(), interval.getX()});
                writer.write(targetVar, writeOrigin, reshaped);
            }
            writer.write(targetVarAT, writeOrigin, caliopReader.readAcquisitionTime(0, y, interval_1).reshape(new int[]{1, ny, 1}));
            writeString(writer, targetVarFN, mu, sourceFileName);
            writeString(writer, targetVarPV, mu, processingVersion);
            writer.write(targetVarX, new int[]{mu}, NetCDFUtils.create(new int[]{0}));
            writer.write(targetVarY, new int[]{mu}, NetCDFUtils.create(new int[]{y}));
        }
    }

    @Override
    protected void initReaderCache() {
        readerCache = createReaderCache(getContext());
    }

    void addVariables(NetcdfFileWriter writer, Reader reader) throws IOException, InvalidRangeException {
        final String MC = FiduceoConstants.MATCHUP_COUNT;
        final String FN = FiduceoConstants.FILE_NAME;
        final String PV = FiduceoConstants.PROCESSING_VERSION;
        final String NY = DIM_NAME_CLAY_NY;
        final String NX = DIM_NAME_CLAY_NX;

        final List<Variable> variables = reader.getVariables();
        for (Variable variable : variables) {
            final String dimString;
            if (variable.getShape()[1] == 10) {
                dimString = MC + " " + NY + " " + DIM_NAME_CLAY_NX_10;
            } else {
                dimString = MC + " " + NY + " " + NX;
            }
            final String shortName = variable.getShortName();
            final Variable targetVar = writer.addVariable(null, variablePrefix + shortName, variable.getDataType(), dimString);
            variableMap.put(shortName, targetVar);
            final List<Attribute> attributes = variable.getAttributes();
            addAttributes(targetVar, attributes);
        }

        Variable var;
        targetVarNameAcquisitionTime = variablePrefix + "acquisition_time";
        var = writer.addVariable(null, targetVarNameAcquisitionTime, DataType.INT, MC + " " + NY + " " + NX);
        var.addAttribute(new Attribute("description", "acquisition time of original pixel"));
        var.addAttribute(new Attribute("units", "seconds since 1970-01-01"));
        var.addAttribute(new Attribute("_FillValue", NetCDFUtils.getDefaultFillValue(DataType.INT, false)));

        targetVarNameFileName = variablePrefix + FiduceoConstants.FILE_NAME;
        var = writer.addVariable(null, targetVarNameFileName, DataType.CHAR, MC + " " + FN);
        var.addAttribute(new Attribute("description", "file name of the original data file"));

        targetVarNameProcessingVersion = variablePrefix + "processing_version";
        var = writer.addVariable(null, targetVarNameProcessingVersion, DataType.CHAR, MC + " " + PV);
        var.addAttribute(new Attribute("description", "the processing version of the original data file"));

        targetVarNameX = variablePrefix + "x";
        var = writer.addVariable(null, targetVarNameX, DataType.INT, MC);
        var.addAttribute(new Attribute("description", "pixel original x location in satellite raster"));
        var.addAttribute(new Attribute("_FillValue", NetCDFUtils.getDefaultFillValue(DataType.INT, false)));

        targetVarNameY = variablePrefix + "y";
        var = writer.addVariable(null, targetVarNameY, DataType.INT, MC);
        var.addAttribute(new Attribute("description", "pixel original y location in satellite raster"));
        var.addAttribute(new Attribute("_FillValue", NetCDFUtils.getDefaultFillValue(DataType.INT, false)));
    }

    void addAttributes(Variable variable, List<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            variable.addAttribute(attribute);
        }
    }

    static void insertDimensions(NetcdfFileWriter writer, int ny) {
        writer.addDimension(null, DIM_NAME_CLAY_NX, 1);
        writer.addDimension(null, DIM_NAME_CLAY_NX_10, 10);
        writer.addDimension(null, DIM_NAME_CLAY_NY, ny);
    }

    private String toCLaySourceFileName(String vfmSourceFileName) {
        return vfmSourceFileName.replace("_VFM-", "_05kmCLay-");
    }

    private String getSourceFileName(int position) throws IOException, InvalidRangeException {
        return getSourceFileName(fileNameVariableVFM, position, filenameFieldSize, CALIOP_L2_VFM_Reader.REG_EX);
    }

    void forTestsOnly_dispose() {
        this.dispose();
    }
}
