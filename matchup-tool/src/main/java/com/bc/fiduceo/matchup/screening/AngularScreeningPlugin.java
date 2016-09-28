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

    <angular>
        <!-- omit of no screening using the primary sensor is required -->
        <primary-vza-variable name="blabla" />

        <!-- omit of no screening using the secondary sensor is required -->
        <secondary-vza-variable name="blabla" />

        <!-- set max threshold in degrees for the primary sensor VZA. Removing this tag switches primary VZA screening off. -->
        <max-primary-vza>10.0</max-primary-vza>

        <!-- set max threshold in degrees for the secondary sensor VZA. Removing this tag switches secondary VZA screening off-->
        <max-secondary-vza>10.0</max-secondary-vza>

         <!-- set max threshold in degrees for the VZA delta. Removing this tag switches VZA delta screening off -->
        <max-angle-delta>10.0</max-angle-delta>
    </angular>
 */

public class AngularScreeningPlugin implements ScreeningPlugin {

    @Override
    public Screening createScreening(Element element) {
        final AngularScreening.Configuration configuration = createConfiguration(element);
        final AngularScreening screening = new AngularScreening();
        screening.configure(configuration);
        return screening;
    }

    @Override
    public String getScreeningName() {
        return "angular";
    }

    // package access for testing only tb 2016-04-21
    static AngularScreening.Configuration createConfiguration(Element element) {
        final AngularScreening.Configuration configuration = new AngularScreening.Configuration();

        final Element primaryVZAVariable = element.getChild("primary-vza-variable");
        if (primaryVZAVariable != null) {
            final Attribute name = primaryVZAVariable.getAttribute("name");
            if (name != null) {
                configuration.primaryVariableName = name.getValue();
            }
        }

        final Element secondaryVZAVariable = element.getChild("secondary-vza-variable");
        if (secondaryVZAVariable != null) {
            final Attribute name = secondaryVZAVariable.getAttribute("name");
            if (name != null) {
                configuration.secondaryVariableName = name.getValue();
            }
        }

        final Element maxPrimaryVZA = element.getChild("max-primary-vza");
        if (maxPrimaryVZA != null) {
            final String maxPrimaryVZAValue = maxPrimaryVZA.getValue();
            configuration.maxPrimaryVZA = Double.parseDouble(maxPrimaryVZAValue);
            configuration.usePrimary = true;
        }

        final Element maxSecondaryVZA = element.getChild("max-secondary-vza");
        if (maxSecondaryVZA != null) {
            final String maxSecondaryVZAValue = maxSecondaryVZA.getValue();
            configuration.maxSecondaryVZA = Double.parseDouble(maxSecondaryVZAValue);
            configuration.useSecondary = true;
        }

        final Element maxAngleDelta = element.getChild("max-angle-delta");
        if (maxAngleDelta != null) {
            final String maxAngleDeltaValue = maxAngleDelta.getValue();
            configuration.maxAngleDelta = Double.parseDouble(maxAngleDeltaValue);
            configuration.useDelta = true;
        }

        return configuration;
    }
}
