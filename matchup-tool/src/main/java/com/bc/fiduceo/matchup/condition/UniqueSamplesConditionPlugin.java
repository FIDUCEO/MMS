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

package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.util.JDomUtils;
import org.esa.snap.core.util.StringUtils;
import org.jdom.Element;

public class UniqueSamplesConditionPlugin implements ConditionPlugin {

    private static final String CONDITION_NAME = "unique-samples";

    @Override
    public Condition createCondition(Element element) {
        final UniqueSamplesCondition.Configuration configuration = parseConfig(element);
        return new UniqueSamplesCondition(configuration);
    }

    @Override
    public String getConditionName() {
        return CONDITION_NAME;
    }

    // package access for testing only  tb 2017-10-02
    static UniqueSamplesCondition.Configuration parseConfig(Element element) {
        if (!CONDITION_NAME.equals(element.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + CONDITION_NAME + "' expected.");
        }

        final UniqueSamplesCondition.Configuration configuration = new UniqueSamplesCondition.Configuration();

        final String referenceSensorKey = JDomUtils.getMandatoryChildTextTrim(element, "reference-sensor");
        if (StringUtils.isNullOrEmpty(referenceSensorKey)) {
            throw new RuntimeException("Missing reference-sensor name in configuration.");
        }
        configuration.referenceSensorKey = referenceSensorKey;

        final String associatedSensorKey = JDomUtils.getMandatoryChildTextTrim(element, "associated-sensor");
        if (StringUtils.isNullOrEmpty(associatedSensorKey)) {
            throw new RuntimeException("Missing associated-sensor name in configuration.");
        }
        configuration.associatedSensorKey = associatedSensorKey;

        return configuration;
    }
}
