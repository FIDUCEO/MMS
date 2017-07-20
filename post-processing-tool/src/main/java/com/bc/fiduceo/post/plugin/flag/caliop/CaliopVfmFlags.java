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
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderCache;
import com.bc.fiduceo.reader.caliop.CALIOP_L2_VFM_Reader;
import com.bc.fiduceo.reader.caliop.CALIOP_L2_VFM_ReaderPlugin;
import com.bc.fiduceo.util.NetCDFUtils;
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

public class CaliopVfmFlags extends PostProcessing {

    private ReaderCache readerCache;
    private Variable fileNameVariable;
    private Variable processingVersionVariable;
    private int filenameFieldSize;
    private int matchupCount;
    private String sensorType;
    private int processingVersionSize;

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        sensorType = "caliop_vfm-cal";
        fileNameVariable = getFileNameVariable(reader, sensorType);
        processingVersionVariable = getProcessingVersionVariable(reader, sensorType);
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
        final String dimString = Constants.MATCHUP_COUNT + " " + sensorType + "_ny" + " " + numFlagsDimName;
        final Variable targetVariable = writer.addVariable(null, sensorType + "_Center_" + flagVarName, DataType.SHORT, dimString);
        for (Attribute srcAttribute : srcAttributes) {
            targetVariable.addAttribute(srcAttribute);
        }
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
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
}
