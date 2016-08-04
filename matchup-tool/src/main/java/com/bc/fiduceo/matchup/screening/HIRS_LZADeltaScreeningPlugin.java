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


import org.jdom.Element;

public class HIRS_LZADeltaScreeningPlugin implements ScreeningPlugin {

    @Override
    public Screening createScreening(Element element) {
        final HIRS_LZADeltaScreening screening = new HIRS_LZADeltaScreening();

        final HIRS_LZADeltaScreening.Configuration configuration = createConfiguration(element);
        screening.configure(configuration);
        return screening;
    }

    @Override
    public String getScreeningName() {
        return "hirs-lza-delta";
    }

    static HIRS_LZADeltaScreening.Configuration createConfiguration(Element rootElement) {
        final HIRS_LZADeltaScreening.Configuration configuration = new HIRS_LZADeltaScreening.Configuration();

        final Element maxLzaDeltaElement = rootElement.getChild("max-lza-delta");
        if (maxLzaDeltaElement != null) {
            final String maxLzaDeltaElementValue = maxLzaDeltaElement.getValue();
            configuration.maxLzaDelta = Float.parseFloat(maxLzaDeltaElementValue);
        }
        return configuration;
    }
}
