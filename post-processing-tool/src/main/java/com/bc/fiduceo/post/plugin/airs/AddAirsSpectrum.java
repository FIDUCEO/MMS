/*
 * Copyright (C) 2018 Brockmann Consult GmbH
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
package com.bc.fiduceo.post.plugin.airs;

import static com.bc.fiduceo.reader.airs.AIRS_Constants.AIRS_NUM_CHANELS;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.reader.airs.AIRS_L1B_Reader;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMatrix;
import ucar.ma2.MAVector;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.constants.CDM;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddAirsSpectrum extends PostProcessing {

    // package access for testing only se 2018-08-31
    final String srcVariableName_fileName;
    final String srcVariableName_processingVersion;
    final String srcVariableName_x;
    final String srcVariableName_y;
    final String srcVariableName_cutOutReference;
    final String targetRadiancesVariableName;
    final String targetCalFlagVariableName;
    final String targetSpaceViewDeltaVariableName;
    Dimension[] cutOutDims;
    final Map<String, Variable> variablesMap;

    public AddAirsSpectrum(String srcVariableName_fileName, String srcVariableName_processingVersion,
                           String srcVariableName_x, String srcVariableName_y, String srcVariableName_cutOutReference,
                           String targetRadiancesVariableName, String targetCalFlagVariableName,
                           String targetSpaceViewDeltaVariableName) {

        this.srcVariableName_fileName = srcVariableName_fileName;
        this.srcVariableName_processingVersion = srcVariableName_processingVersion;
        this.srcVariableName_x = srcVariableName_x;
        this.srcVariableName_y = srcVariableName_y;
        this.srcVariableName_cutOutReference = srcVariableName_cutOutReference;
        this.targetRadiancesVariableName = targetRadiancesVariableName;
        this.targetCalFlagVariableName = targetCalFlagVariableName;
        this.targetSpaceViewDeltaVariableName = targetSpaceViewDeltaVariableName;
        variablesMap = new HashMap<>();
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        cutOutDims = get2dCutOutDimensions(reader);
        final List<ucar.nc2.Dimension> targetDimensions = addSpectrumDimension(writer, cutOutDims);
        addSpectrumVariables(writer, targetDimensions);
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable fileNameVariable = NetCDFUtils.getVariable(reader, srcVariableName_fileName);
        final Variable processingVersionVariable = NetCDFUtils.getVariable(reader, srcVariableName_processingVersion);

        final int cutHeight = cutOutDims[1].getLength();
        final int cutWidth = cutOutDims[0].getLength();

        final Variable xVariable = NetCDFUtils.getVariable(reader, srcVariableName_x);
        final Variable yVariable = NetCDFUtils.getVariable(reader, srcVariableName_y);
        final Array xArray = xVariable.read();
        final Array yArray = yVariable.read();

        final int matchup_count = NetCDFUtils.getDimensionLength("matchup_count", reader);
        final int fileNameSize = NetCDFUtils.getDimensionLength("file_name", reader);
        final int processingVersionSize = NetCDFUtils.getDimensionLength("processing_version", reader);
        final int[] origin = new int[4];
        for (int i = 0; i < matchup_count; i++) {
            final String fileName = NetCDFUtils.readString(fileNameVariable, i, fileNameSize);
            final String processingVersion = NetCDFUtils.readString(processingVersionVariable, i, processingVersionSize);
            final String sensorKey = "airs_aq";
            final AIRS_L1B_Reader airsReader = (AIRS_L1B_Reader) readerCache.getReaderFor(sensorKey, Paths.get(fileName), processingVersion);
            final int proNx = airsReader.getProductSize().getNx();
            final int proNy = airsReader.getProductSize().getNy();

            final int centerX = xArray.getInt(i);
            final int centerY = yArray.getInt(i);
            final int minX = centerX - cutWidth / 2;
            final int minY = centerY - cutHeight / 2;
            //                                     ___ num matchup dim
            //                                   /   ___ y dim
            //                                  /  /   ___ x dim
            //                                 /  /  /   ___ channels dim
            //                                /  /  /  /
            final int[] writeOrigin3DFull = {i, 0, 0, 0};
            final int[] writeOrigin3DSlice = writeOrigin3DFull.clone();

            int readHeight = cutHeight;
            int readWidth = cutWidth;
            int readMinX = minX;
            int readMinY = minY;
            boolean fillValueArrayNeeded = false;
            if (minX < 0) {
                readWidth += minX;
                readMinX = 0;
                writeOrigin3DSlice[2] = Math.abs(minX);
                fillValueArrayNeeded = true;
            } else if (minX + cutWidth > proNx) {
                readWidth += (proNx - minX - cutWidth);
                fillValueArrayNeeded = true;
            }
            if (minY < 0) {
                readHeight += minY;
                readMinY = 0;
                writeOrigin3DSlice[1] = Math.abs(minY);
                fillValueArrayNeeded = true;
            } else if (minY + cutHeight > proNy) {
                readHeight += (proNy - minY - cutHeight);
                fillValueArrayNeeded = true;
            }
            int[] shape = new int[]{readHeight, readWidth, AIRS_NUM_CHANELS};
            final Array radiancesCuboid = airsReader.readSpectrum(readMinY, readMinX, shape, "radiances");


            final Variable radVar = variablesMap.get(targetRadiancesVariableName);
            if (fillValueArrayNeeded) {
                final Number fillValue = NetCDFUtils.getFillValue(radVar);
                final Array fillValueRadiancesCuboid = Array.factory(radVar.getDataType(), new int[]{cutHeight, cutWidth, AIRS_NUM_CHANELS});
                final IndexIterator iterator = fillValueRadiancesCuboid.getIndexIterator();
                while (iterator.hasNext()) {
                    iterator.setObjectNext(fillValue);
                }
                writer.write(radVar, writeOrigin3DFull, Array.makeArrayRankPlusOne(fillValueRadiancesCuboid));
            }
            writer.write(radVar, writeOrigin3DSlice, Array.makeArrayRankPlusOne(radiancesCuboid));
        }
    }

    @Override
    protected void initReaderCache() {
        readerCache = createReaderCache(getContext());
    }

    // package access for testing only se 2018-08-31
    static List<ucar.nc2.Dimension> addSpectrumDimension(NetcdfFileWriter writer, Dimension[] cutOutDimensions) {
        final List<Dimension> targetDimensions = new ArrayList<>(Arrays.asList(cutOutDimensions));
        final ucar.nc2.Dimension airsChannel = writer.addDimension(null, "airs_channel", AIRS_NUM_CHANELS);
        targetDimensions.add(airsChannel);
        return targetDimensions;
    }

    // package access for testing only se 2018-08-31
    void addSpectrumVariables(NetcdfFileWriter writer, List<Dimension> targetDimensions) {
        final Variable radiances = writer.addVariable(null, targetRadiancesVariableName, DataType.FLOAT, targetDimensions);
        radiances.addAttribute(new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999.0f));
        radiances.addAttribute(new Attribute("description", "Radiances for each channel in milliWatts/m**2/cm**-1/steradian"));
        radiances.addAttribute(new Attribute(NetCDFUtils.CF_UNITS_NAME, "mW/m2/cm-1/sr"));
        variablesMap.put(targetRadiancesVariableName, radiances);

        final Variable calFlag = writer.addVariable(null, targetCalFlagVariableName, DataType.BYTE, targetDimensions);
        calFlag.setUnsigned(true);
        calFlag.addAttribute(new Attribute(CDM.UNSIGNED, "true"));
        calFlag.addAttribute(new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, Byte.valueOf("-1")));
        calFlag.addAttribute(new Attribute("description", "Bit field, by channel, for the current scanline. Zero means the channel was well " +
                                                          "calibrated, for this scanline.\n" +
                                                          "Bit 7 (MSB): scene over/underflow;\n" +
                                                          "Bit 6: (value 64) anomaly in offset calculation;\n" +
                                                          "Bit 5: (value 32) anomaly in gain calculation;\n" +
                                                          "Bit 4: (value 16) pop detected;\n" +
                                                          "Bit 3: (value 8) DCR Occurred;\n" +
                                                          "Bit 2: (value 4) Moon in View;\n" +
                                                          "Bit 1: (value 2) telemetry out of limit condition;\n" +
                                                          "Bit 0: (LSB, value 1) cold scene noise"));
        calFlag.addAttribute(new Attribute(NetCDFUtils.CF_UNITS_NAME, "mW/m2/cm-1/sr"));
        variablesMap.put(targetCalFlagVariableName, calFlag);

        final Variable spaceViewDelta = writer.addVariable(null, targetSpaceViewDeltaVariableName, DataType.FLOAT, targetDimensions);
        spaceViewDelta.addAttribute(new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999.0f));
        spaceViewDelta.addAttribute(new Attribute("description", "The median of the four spaceviews immediately following the Earth views in " +
                                                      "the scanline, minus the median of the spaceviews immediately preceding the " +
                                                      "Earth views in the scanline (also the magnitude of a \"pop\" in this scanline, " +
                                                      "when the \"pop detected\" bit is set in CalFlag.)"));
        spaceViewDelta.addAttribute(new Attribute(NetCDFUtils.CF_UNITS_NAME, "dl"));
        variablesMap.put(targetSpaceViewDeltaVariableName,spaceViewDelta);
    }

    // package access for testing only se 2018-08-31
    Dimension[] get2dCutOutDimensions(NetcdfFile reader) {
        final Variable variable = NetCDFUtils.getVariable(reader, srcVariableName_cutOutReference);
        return variable.getDimensions().toArray(new Dimension[variable.getRank()]);
    }
}
