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

package com.bc.fiduceo.post.plugin.amsr;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import com.bc.fiduceo.util.JDomUtils;
import org.jdom.Attribute;
import org.jdom.Element;

public class AddAmsr2ScanDataQualityPlugin implements PostProcessingPlugin {

    @Override
    public PostProcessing createPostProcessing(Element element) {
        final AddAmsr2ScanDataQuality.Configuration configuration = createConfiguration(element);

        final AddAmsr2ScanDataQuality postProcessing = new AddAmsr2ScanDataQuality();
        postProcessing.configure(configuration);
        
        return postProcessing;
    }

    @Override
    public String getPostProcessingName() {
        return "add-amsr2-scan-data-quality";
    }

    public static AddAmsr2ScanDataQuality.Configuration createConfiguration(Element rootElement) {
        final AddAmsr2ScanDataQuality.Configuration configuration = new AddAmsr2ScanDataQuality.Configuration();

        Element childElement = JDomUtils.getMandatoryChild(rootElement, "target-variable");
        Attribute nameAttribute = JDomUtils.getMandatoryAttribute(childElement, "name");
        configuration.targetVariableName = nameAttribute.getValue().trim();

        childElement = JDomUtils.getMandatoryChild(rootElement, "filename-variable");
        nameAttribute = JDomUtils.getMandatoryAttribute(childElement, "name");
        configuration.filenameVariableName = nameAttribute.getValue().trim();

        childElement = JDomUtils.getMandatoryChild(rootElement, "processing-version-variable");
        nameAttribute = JDomUtils.getMandatoryAttribute(childElement, "name");
        configuration.processingVersionVariableName = nameAttribute.getValue().trim();

        childElement = JDomUtils.getMandatoryChild(rootElement, "y-variable");
        nameAttribute = JDomUtils.getMandatoryAttribute(childElement, "name");
        configuration.yCoordinateVariableName = nameAttribute.getValue().trim();

        return configuration;
    }
}
