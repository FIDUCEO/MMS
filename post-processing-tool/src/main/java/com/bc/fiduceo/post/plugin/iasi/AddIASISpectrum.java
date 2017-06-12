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
import com.bc.fiduceo.util.JDomUtils;
import com.bc.fiduceo.util.NetCDFUtils;
import org.jdom.Element;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
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
        targetDimensions.add(new Dimension("iasi_ss", EpsMetopConstants.SS));

        writer.addVariable(null, configuration.targetVariableName, DataType.FLOAT, targetDimensions);
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

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
