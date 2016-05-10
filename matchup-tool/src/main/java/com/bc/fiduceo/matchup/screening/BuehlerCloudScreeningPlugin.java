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

public class BuehlerCloudScreeningPlugin implements ScreeningPlugin {

    @Override
    public Screening createScreening(Element element) {
        final BuehlerCloudScreening.Configuration configuration = createConfiguration(element);

        final BuehlerCloudScreening screening = new BuehlerCloudScreening();
        screening.configure(configuration);
        return screening;
    }

    @Override
    public String getScreeningName() {
        return "buehler-cloud";
    }

    static BuehlerCloudScreening.Configuration createConfiguration(Element element) {
        final BuehlerCloudScreening.Configuration configuration = new BuehlerCloudScreening.Configuration();

        final Element primaryNarrowChannel = element.getChild("primary-narrow-channel");
        if (primaryNarrowChannel != null) {
            final Attribute name = primaryNarrowChannel.getAttribute("name");
            if (name != null) {
                configuration.primaryNarrowChannelName = name.getValue();
            }
        }

        final Element primaryWideChannel = element.getChild("primary-wide-channel");
        if (primaryWideChannel != null) {
            final Attribute name = primaryWideChannel.getAttribute("name");
            if (name != null) {
                configuration.primaryWideChannelName = name.getValue();
            }
        }

        final Element primaryVZAVariable = element.getChild("primary-vza");
        if (primaryVZAVariable != null) {
            final Attribute name = primaryVZAVariable.getAttribute("name");
            if (name != null) {
                configuration.primaryVZAVariableName = name.getValue();
            }
        }

        final Element secondaryNarrowChannel = element.getChild("secondary-narrow-channel");
        if (secondaryNarrowChannel != null) {
            final Attribute name = secondaryNarrowChannel.getAttribute("name");
            if (name != null) {
                configuration.secondaryNarrowChannelName = name.getValue();
            }
        }

        final Element secondryWideChannel = element.getChild("secondary-wide-channel");
        if (secondryWideChannel != null) {
            final Attribute name = secondryWideChannel.getAttribute("name");
            if (name != null) {
                configuration.secondaryWideChannelName = name.getValue();
            }
        }

        final Element secondaryVZAVariable = element.getChild("secondary-vza");
        if (secondaryVZAVariable != null) {
            final Attribute name = secondaryVZAVariable.getAttribute("name");
            if (name != null) {
                configuration.secondaryVZAVariableName = name.getValue();
            }
        }

        return configuration;
    }
}
