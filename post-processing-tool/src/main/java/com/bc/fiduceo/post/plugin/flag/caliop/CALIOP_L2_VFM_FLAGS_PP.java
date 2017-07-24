/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.fiduceo.post.plugin.flag.caliop;

import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.post.Constants;
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

    ReaderCache readerCache;
    private Variable fileNameVariable;
    private Variable processingVersionVariable;
    private int filenameFieldSize;
    private int matchupCount;
    private String sensorNamePrefix;
    private int processingVersionSize;
    private Variable targetFlagsVariable;
    private String separatorChar;
    private String sensorType;

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        sensorNamePrefix = "caliop_vfm";
        sensorType = sensorNamePrefix + "-cal";
        separatorChar = ".";
        fileNameVariable = getFileNameVariable(reader, sensorNamePrefix, separatorChar);
        processingVersionVariable = getProcessingVersionVariable(reader, sensorNamePrefix, separatorChar);
        filenameFieldSize = NetCDFUtils.getDimensionLength("file_name", reader);
        processingVersionSize = NetCDFUtils.getDimensionLength("processing_version", reader);

        matchupCount = NetCDFUtils.getDimensionLength(Constants.MATCHUP_COUNT, reader);

        final String sourceFileName = getSourceFileName(fileNameVariable, 0, filenameFieldSize, CALIOP_L2_VFM_Reader.REG_EX);
        final String processingVersion = NetCDFUtils.readString(processingVersionVariable, 0, processingVersionSize);

        final CALIOP_L2_VFM_Reader caliopReader = (CALIOP_L2_VFM_Reader) readerCache.getReaderFor(sensorType, Paths.get(sourceFileName), processingVersion);
        final String flagVarName = "Feature_Classification_Flags";
        final Variable sourceFlagsVar = caliopReader.find(flagVarName);
        final List<Attribute> srcAttributes = sourceFlagsVar.getAttributes();

        final String numFlagsDimName = "center-fcf-flags";
        writer.addDimension(null, numFlagsDimName, 545);
        final String dimString = reader.findVariable(toValidName("caliop_vfm.Latitude")).getDimensionsString() + " " + numFlagsDimName;
        final String targetName = sensorNamePrefix + separatorChar + "Center_" + flagVarName;
        targetFlagsVariable = writer.addVariable(null, targetName, DataType.SHORT, dimString);
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
        final int yWindowSize = NetCDFUtils.getDimensionLength("caliop_vfm-cal_ny", reader);
        final int yWindowOffset = yWindowSize / 2;

        final Array yMatchupCenter = reader.findVariable(toValidName("caliop_vfm.y")).read();

        for (int i = 0; i < matchupCount; i++) {
            final String sourceFileName = getSourceFileName(fileNameVariable, i, filenameFieldSize, CALIOP_L2_VFM_Reader.REG_EX);
            final String processingVersion = NetCDFUtils.readString(processingVersionVariable, i, processingVersionSize);
            final CALIOP_L2_VFM_Reader caliopReader = (CALIOP_L2_VFM_Reader) readerCache.getReaderFor(sensorType, Paths.get(sourceFileName), processingVersion);
            final String flagVarName = "Feature_Classification_Flags";
            final Variable sourceFlagsVar = caliopReader.find(flagVarName);
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
