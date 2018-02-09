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
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class AddIASISpectrum extends PostProcessing {

    private final Configuration configuration;

    AddIASISpectrum(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void initReaderCache() {
        readerCache = createReaderCache(getContext());
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) {
        final Variable referenceVariable = NetCDFUtils.getVariable(reader, configuration.referenceVariableName);
        final List<ucar.nc2.Dimension> dimensions = referenceVariable.getDimensions();

        final List<ucar.nc2.Dimension> targetDimensions = addSpectrumDimension(writer, dimensions);

        addSpectrumVariable(writer, targetDimensions);
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable fileNameVariable = NetCDFUtils.getVariable(reader, configuration.filenameVariableName);
        final Variable processingVersionVariable = NetCDFUtils.getVariable(reader, configuration.processingVersionVariableName);

        final Variable referenceVariable = NetCDFUtils.getVariable(reader, configuration.referenceVariableName);
        final int[] shape = referenceVariable.getShape();
        final int height = shape[1];
        final int width = shape[2];
        final int halfWidth = width / 2;
        final int halfHeight = height / 2;

        final Variable xVariable = NetCDFUtils.getVariable(reader, configuration.xCoordinateName);
        final Variable yVariable = NetCDFUtils.getVariable(reader, configuration.yCoordinateName);
        final Array xArray = xVariable.read();
        final Array yArray = yVariable.read();

        final Variable targetVariable = NetCDFUtils.getVariable(writer, configuration.targetVariableName);

        final int matchup_count = NetCDFUtils.getDimensionLength("matchup_count", reader);
        final int fileNameSize = NetCDFUtils.getDimensionLength("file_name", reader);
        final int processingVersionSize = NetCDFUtils.getDimensionLength("processing_version", reader);

        final Array fillValueSpectrum = getFillValueSpectrum();
        final ArrayFloat.D4 writeArray = new ArrayFloat.D4(1, height, width, EpsMetopConstants.SS);

        final int[] origin = new int[4];
        for (int i = 0; i < matchup_count; i++) {
            final String fileName = NetCDFUtils.readString(fileNameVariable, i, fileNameSize);
            final String processingVersion = NetCDFUtils.readString(processingVersionVariable, i, processingVersionSize);
            final String sensorKey = getSensorKey(fileName);

            final IASI_Reader iasiReader = (IASI_Reader) readerCache.getReaderFor(sensorKey, Paths.get(fileName), processingVersion);
            final Rectangle boundingRectangle = getBoundingRectangle(iasiReader);

            final int centerX = xArray.getInt(i);
            final int centerY = yArray.getInt(i);
            int yWriteIndex = 0;
            int xWriteIndex;

            for (int yOffset = -halfHeight; yOffset <= halfHeight; yOffset++) {
                final int y = centerY + yOffset;
                xWriteIndex = 0;

                for (int xOffset = -halfWidth; xOffset <= halfWidth; xOffset++) {
                    final int x = centerX + xOffset;

                    final Array spectrum = readSpectrum(fillValueSpectrum, iasiReader, boundingRectangle, y, x);
                    copySpectrumToTargetArray(writeArray, yWriteIndex, xWriteIndex, spectrum);

                    ++xWriteIndex;
                }

                ++yWriteIndex;
            }
            origin[0] = i;
            writer.write(targetVariable, origin, writeArray);
        }
    }

    // package access for testing only tb 2017-06-12
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

    // package access for testing only tb 2017-06-14
    static Array getFillValueSpectrum() {
        final float[] filValueVector = IASI_Reader.getDefaultFloatSpect();
        return Array.factory(filValueVector);
    }

    // package access for testing only tb 2017-06-14
    static Rectangle getBoundingRectangle(IASI_Reader iasiReader) {
        final com.bc.fiduceo.core.Dimension productSize = iasiReader.getProductSize();
        return new Rectangle(0, 0, productSize.getNx(), productSize.getNy());
    }

    // package access for testing only tb 2017-08-02
    static List<ucar.nc2.Dimension> addSpectrumDimension(NetcdfFileWriter writer, List<ucar.nc2.Dimension> dimensions) {
        final List<ucar.nc2.Dimension> targetDimensions = new ArrayList<>();
        targetDimensions.addAll(dimensions);

        final ucar.nc2.Dimension iasi_ss = writer.addDimension(null, "iasi_ss", EpsMetopConstants.SS);
        targetDimensions.add(iasi_ss);
        return targetDimensions;
    }

    // @todo 2 tb/** make static and add test 2017-06-14
    private void copySpectrumToTargetArray(ArrayFloat.D4 writeArray, int yWriteIndex, int xWriteIndex, Array spectrum) {
        // @todo 2 tb/** check if there exists a method to copy the vector in one run 2017-06-14
        for (int k = 0; k < EpsMetopConstants.SS; k++) {
            final float spectrumValue = spectrum.getFloat(k);
            writeArray.set(0, yWriteIndex, xWriteIndex, k, spectrumValue);
        }
    }

    private Array readSpectrum(Array fillValueSpectrum, IASI_Reader iasiReader, Rectangle boundingRectangle, int y, int x) throws IOException {
        final Array spectrum;
        if (boundingRectangle.contains(x, y)) {
            spectrum = iasiReader.readSpectrum(x, y);
        } else {
            spectrum = fillValueSpectrum;
        }
        return spectrum;
    }

    private void addSpectrumVariable(NetcdfFileWriter writer, List<ucar.nc2.Dimension> targetDimensions) {
        final Variable variable = writer.addVariable(null, configuration.targetVariableName, DataType.FLOAT, targetDimensions);
        variable.addAttribute(new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        variable.addAttribute(new Attribute("description", "decoded IASI spectrum"));
        variable.addAttribute(new Attribute(NetCDFUtils.CF_UNITS_NAME, "W/m2/sr/m-1"));
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
