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

package com.bc.fiduceo.matchup.screening;

import org.jdom.Attribute;
import org.jdom.Element;

/* The XML template for this screening class looks like:

    <angular-cosine-proportion>
        <primary-variable name="blabla" />

        <secondary-variable name="blabla" />

        <threshold>0.01</threshold>
    </angular-cosine-proportion>
 */

public class AngularCosineProportionScreeningPlugin implements ScreeningPlugin {

    @Override
    public Screening createScreening(Element element) {
        final AngularCosineProportionScreening screening = new AngularCosineProportionScreening();
        final AngularCosineProportionScreening.Configuration configuration = createConfiguration(element);
        screening.configure(configuration);
        return screening;
    }

    @Override
    public String getScreeningName() {
        return "angular-cosine-proportion";
    }

    static AngularCosineProportionScreening.Configuration createConfiguration(Element element) {
        final AngularCosineProportionScreening.Configuration configuration = new AngularCosineProportionScreening.Configuration();

        final Element primaryVariableElement = element.getChild("primary-variable");
        if (primaryVariableElement != null) {
            final Attribute name = primaryVariableElement.getAttribute("name");
            if (name != null) {
                configuration.primaryVariableName = name.getValue();
            }
        }

        final Element secondaryVariableElement = element.getChild("secondary-variable");
        if (secondaryVariableElement != null) {
            final Attribute name = secondaryVariableElement.getAttribute("name");
            if (name != null) {
                configuration.secondaryVariableName = name.getValue();
            }
        }

        final Element thresholdElement = element.getChild("threshold");
        if (thresholdElement != null) {
            final String thresholdString = thresholdElement.getValue();
            configuration.threshold = Double.parseDouble(thresholdString);
        }

        return configuration;
    }
}
