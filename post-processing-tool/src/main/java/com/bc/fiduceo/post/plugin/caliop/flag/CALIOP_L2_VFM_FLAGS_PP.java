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

package com.bc.fiduceo.post.plugin.caliop.flag;

import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.reader.ReaderCache;
import com.bc.fiduceo.reader.caliop.CALIOP_L2_VFM_Reader;
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
import java.util.List;
import java.util.logging.Level;

public class CALIOP_L2_VFM_FLAGS_PP extends PostProcessing {

    public static final String MATCHUP_COUNT = "matchup_count";
    public static final String CALIOP_VFM_CAL_NY = "caliop_vfm-cal_ny";
    public static final String CALIOP_VFM_CAL_NX = "caliop_vfm-cal_nx";
    public static final String CENTER_FCF_FLAGS = "center-fcf-flags";
    public static final String FLAG_VAR_NAME = "Feature_Classification_Flags";

    final String srcVariableName_fileName;
    final String srcVariableName_processingVersion;
    final String targetVariableName_centerFCF;
    final String srcVariableName_y;
    final String sensorType;

    ReaderCache readerCache;
    Variable fileNameVariable;
    int filenameFieldSize;
    Variable processingVersionVariable;
    int processingVersionSize;
    Variable targetFlagsVariable;

    public CALIOP_L2_VFM_FLAGS_PP(String srcVariableName_fileName,
                                  String srcVariableName_processingVersion,
                                  String srcVariableName_y,
                                  String targetVariableName_centerFCF) {
        this.srcVariableName_fileName = srcVariableName_fileName;

        this.srcVariableName_processingVersion = srcVariableName_processingVersion;
        this.targetVariableName_centerFCF = targetVariableName_centerFCF;
        this.srcVariableName_y = srcVariableName_y;
        sensorType = "caliop_vfm-cal";
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        fileNameVariable = NetCDFUtils.getVariable(reader, srcVariableName_fileName);
        filenameFieldSize = fileNameVariable.getShape(fileNameVariable.getRank() - 1);

        processingVersionVariable = NetCDFUtils.getVariable(reader, srcVariableName_processingVersion);
        processingVersionSize = processingVersionVariable.getShape(processingVersionVariable.getRank() - 1);

        final String sourceFileName = getSourceFileName(fileNameVariable, 0, filenameFieldSize, CALIOP_L2_VFM_Reader.REG_EX);
        final String processingVersion = NetCDFUtils.readString(processingVersionVariable, 0, processingVersionSize);

        final CALIOP_L2_VFM_Reader caliopReader = (CALIOP_L2_VFM_Reader) readerCache.getReaderFor(sensorType, Paths.get(sourceFileName), processingVersion);
        final Variable sourceFlagsVar = caliopReader.find(FLAG_VAR_NAME);
        final List<Attribute> srcAttributes = sourceFlagsVar.getAttributes();

        writer.addDimension(null, CENTER_FCF_FLAGS, 545);
        final String dimString = MATCHUP_COUNT + " " + CALIOP_VFM_CAL_NY + " " + CALIOP_VFM_CAL_NX + " " + CENTER_FCF_FLAGS;
        targetFlagsVariable = writer.addVariable(null, targetVariableName_centerFCF, DataType.SHORT, dimString);
        for (Attribute srcAttribute : srcAttributes) {
            targetFlagsVariable.addAttribute(srcAttribute);
        }
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final int[] srcOrigin = {0, 0};
        final int[] srcShape = {1, 5515};
        final int[] targetOrigin = {0, 0, 0, 0};
        final int[] targetShape = {1, 1, 1, 545};
        final int yWindowSize = NetCDFUtils.getDimensionLength(CALIOP_VFM_CAL_NY, reader);
        final int yWindowOffset = yWindowSize / 2;

        final Array yMatchupCenter = reader.findVariable(toValidName(srcVariableName_y)).read();

        final int matchupCount = (int) yMatchupCenter.getSize();
        for (int i = 0; i < matchupCount; i++) {
            final String sourceFileName = getSourceFileName(fileNameVariable, i, filenameFieldSize, CALIOP_L2_VFM_Reader.REG_EX);
            final String processingVersion = NetCDFUtils.readString(processingVersionVariable, i, processingVersionSize);
            final CALIOP_L2_VFM_Reader caliopReader = (CALIOP_L2_VFM_Reader) readerCache.getReaderFor(sensorType, Paths.get(sourceFileName), processingVersion);
            final Variable sourceFlagsVar = caliopReader.find(FLAG_VAR_NAME);
            final int centerY = yMatchupCenter.getInt(i);
            targetOrigin[0] = i;
            for (int j = 0; j < yWindowSize; j++) {
                final int yFlagReadPos = centerY - yWindowOffset + j;
                srcOrigin[0] = yFlagReadPos;
                targetOrigin[1] = j;
                final Array array = sourceFlagsVar.read(srcOrigin, srcShape);
                final Array centerFlags = CALIOP_L2_VFM_Reader.readNadirClassificationFlags(array);
                final Array reshapedData = centerFlags.reshape(targetShape);
                writer.write(targetFlagsVariable, targetOrigin, reshapedData);
            }
        }
    }

    @Override
    protected void initReaderCache() {
        readerCache = createReaderCache(getContext());
    }

    @Override
    protected void dispose() {
        if (readerCache != null) {
            try {
                readerCache.close();
            } catch (IOException e) {
                FiduceoLogger.getLogger().log(Level.WARNING, "IO Exception while disposing the ReaderCache.", e);
            }
        }
    }

    private String toValidName(String latName) {
        return NetcdfFile.makeValidCDLName(latName);
    }
}
