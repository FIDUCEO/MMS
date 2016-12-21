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

package com.bc.fiduceo.post.plugin;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import com.bc.fiduceo.util.JDomUtils;
import org.jdom.Attribute;
import org.jdom.Element;

/* The XML template for this post processing class looks like:

    <add-amsre-solar-angles>
        <sun-elevation-variable name = "Sun_Elevation" />
        <sun-azimuth-variable name = "Sun_Azimuth" />
        <earth-incidence-variable name = "Earth_Incidence" />
        <earth-azimuth-variable name = "Earth_Azimuth" />
        <sza-target-variable name = "amsre.solar_zenith_angle" />
        <saa-target-variable name = "amsre.solar_azimuth_angle" />
    </add-amsre-solar-angles>
 */

public class AddAmsreSolarAnglesPlugin implements PostProcessingPlugin {

    @Override
    public PostProcessing createPostProcessing(Element element) {
        final AddAmsreSolarAngles.Configuration configuration = createConfiguration(element);

        final AddAmsreSolarAngles solarAnglesPostProcessing = new AddAmsreSolarAngles();
        solarAnglesPostProcessing.configure(configuration);

        return solarAnglesPostProcessing;
    }

    @Override
    public String getPostProcessingName() {
        return "add-amsre-solar-angles";
    }

    // package access for testing only tb 2016-12-14
    static AddAmsreSolarAngles.Configuration createConfiguration(Element element) {
        final AddAmsreSolarAngles.Configuration configuration = new AddAmsreSolarAngles.Configuration();

        configuration.sunElevationVariable = getNameAttribute(element, "sun-elevation-variable");
        configuration.sunAzimuthVariable = getNameAttribute(element, "sun-azimuth-variable");
        configuration.earthIncidenceVariable = getNameAttribute(element, "earth-incidence-variable");
        configuration.earthAzimuthVariable = getNameAttribute(element, "earth-azimuth-variable");

        configuration.szaVariable = getNameAttribute(element, "sza-target-variable");
        configuration.saaVariable = getNameAttribute(element, "saa-target-variable");

        return configuration;
    }

    private static String getNameAttribute(Element element, String elementName) {
        final Element childElement = JDomUtils.getMandatoryChild(element, elementName);
        final Attribute nameAttribute = JDomUtils.getMandatoryAttribute(childElement, "name");
        return nameAttribute.getValue().trim();
    }
}
