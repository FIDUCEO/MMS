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

package com.bc.fiduceo.post.plugin.iasi;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.ReaderCache;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.iasi.EpsMetopConstants;
import com.bc.fiduceo.reader.iasi.IASI_Reader;
import com.bc.fiduceo.util.JDomUtils;
import com.bc.fiduceo.util.NetCDFUtils;
import org.jdom.Element;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class AddIASISpectrum extends PostProcessing {

    private final Configuration configuration;

    AddIASISpectrum(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable referenceVariable = NetCDFUtils.getVariable(reader, configuration.referenceVariableName);
        final List<Dimension> dimensions = referenceVariable.getDimensions();

        final List<Dimension> targetDimensions = new ArrayList<>();
        targetDimensions.addAll(dimensions);
        final Dimension iasi_ss = writer.addDimension(null, "iasi_ss", EpsMetopConstants.SS);
        targetDimensions.add(iasi_ss);

        final Variable variable = writer.addVariable(null, configuration.targetVariableName, DataType.FLOAT, targetDimensions);
        variable.addAttribute(new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        variable.addAttribute(new Attribute("description", "decoded IASI spectrum"));
        variable.addAttribute(new Attribute(NetCDFUtils.CF_UNITS_NAME, "W/m2/sr/m-1"));
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final ReaderCache readerCache = new ReaderCache(getContext()) {
            @Override
            protected int[] extractYearMonthDayFromFilename(String fileName) {
                return AddIASISpectrum.extractYMDfromFileName(fileName);
            }
        };

        final Variable fileNameVariable = NetCDFUtils.getVariable(reader, configuration.filenameVariableName);
        final Variable processingVersionVariable = NetCDFUtils.getVariable(reader, configuration.processingVersionVariableName);

        final Variable xVariable = NetCDFUtils.getVariable(reader, configuration.xCoordinateName);
        final Variable yVariable = NetCDFUtils.getVariable(reader, configuration.yCoordinateName);
        final Array xArray = xVariable.read();
        final Array yArray = yVariable.read();

        final Variable targetVariable = NetCDFUtils.getVariable(writer, configuration.targetVariableName);

        final int matchup_count = NetCDFUtils.getDimensionLength("matchup_count", reader);
        final int fileNameSize = NetCDFUtils.getDimensionLength("file_name", reader);
        final int processingVersionSize = NetCDFUtils.getDimensionLength("processing_version", reader);

        final ArrayFloat.D4 writeArray = new ArrayFloat.D4(1, 1, 1, EpsMetopConstants.SS);

        final int[] origin = new int[4];
        for (int i = 0; i < matchup_count; i++) {
            final String fileName = NetCDFUtils.readString(fileNameVariable, i, fileNameSize);
            final String processingVersion = NetCDFUtils.readString(processingVersionVariable, i, processingVersionSize);
            final String sensorKey = getSensorKey(fileName);

            final int x = xArray.getInt(i);
            final int y = yArray.getInt(i);
            // @todo 1 tb/tb iterate over possible extraction windows here. We can not assume a single pixel extract 2017-06-12

            final IASI_Reader iasiReader = (IASI_Reader) readerCache.getFileOpened(fileName, sensorKey, processingVersion);
            final Array spectrum = iasiReader.readSpectrum(x, y);

            final Array spectrumReshaped = Array.factory(spectrum.getDataType(), writeArray.getShape(), spectrum.getStorage());

            origin[0] = i;
            writer.write(targetVariable, origin, spectrumReshaped);

        }


    }

    static int[] extractYMDfromFileName(String fileName) {
        final String yearString = fileName.substring(16, 20);
        final String monthString = fileName.substring(20, 22);
        final String dayString = fileName.substring(22, 24);

        final int[] ymd = new int[3];
        ymd[0] = Integer.parseInt(yearString);
        ymd[1] = Integer.parseInt(monthString);
        ymd[2] = Integer.parseInt(dayString);
        return  ymd;
    }


    static String getSensorKey(String fileName) {
        if (fileName.contains("_M02_")) {
            return "iasi-ma";
        } else if (fileName.contains("_M01_")) {
            return "iasi-mb";
        }
        throw new RuntimeException("Unsupported file: " + fileName);
    }

    // package access for testing only tb 2017-06-12
    static Configuration createConfiguration(Element rootElement) {
        final Configuration configuration = new Configuration();

        configuration.targetVariableName = getNameAttributeFromChild(rootElement, "target-variable");
        configuration.referenceVariableName = getNameAttributeFromChild(rootElement, "reference-variable");
        configuration.xCoordinateName = getNameAttributeFromChild(rootElement, "x-variable");
        configuration.yCoordinateName = getNameAttributeFromChild(rootElement, "y-variable");
        configuration.filenameVariableName = getNameAttributeFromChild(rootElement, "file-name-variable");
        configuration.processingVersionVariableName = getNameAttributeFromChild(rootElement, "processing-version-variable");

        return configuration;
    }

    private static String getNameAttributeFromChild(Element rootElement, String elementName) {
        final Element element = JDomUtils.getMandatoryChild(rootElement, elementName);
        return JDomUtils.getValueFromNameAttributeMandatory(element);
    }

    static class Configuration {
        String targetVariableName;
        String referenceVariableName;
        String xCoordinateName;
        String yCoordinateName;
        String filenameVariableName;
        String processingVersionVariableName;

    }
}
