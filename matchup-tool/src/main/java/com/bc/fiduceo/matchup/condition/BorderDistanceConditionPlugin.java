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

package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.util.JDomUtils;
import org.jdom.Element;

/* The XML template for this condition class looks like:

    <border-point_distance>
        <primary>
            <nx>2</nx>
            <ny>3</ny>
        </primary>
        <secondary>
            <nx>5</nx>
            <ny>4</ny>
        </secondary>
    </border-point_distance>
 */

public class BorderDistanceConditionPlugin implements ConditionPlugin {

    @Override
    public Condition createCondition(Element element) {
        final BorderDistanceCondition.Configuration configuration = parseConfiguration(element);

        return new BorderDistanceCondition(configuration);
    }

    @Override
    public String getConditionName() {
        return "border-point_distance";
    }

    BorderDistanceCondition.Configuration parseConfiguration(Element element) {
        if (!getConditionName().equals(element.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + getConditionName() + "' expected.");
        }

        final BorderDistanceCondition.Configuration configuration = new BorderDistanceCondition.Configuration();

        final Element primaryElement = element.getChild("primary");
        if (primaryElement != null) {
            configuration.usePrimary = true;

            final String nx = JDomUtils.getMandatoryChildTextTrim(primaryElement, "nx");
            configuration.primary_x = Integer.parseInt(nx);

            final String ny = JDomUtils.getMandatoryChildTextTrim(primaryElement, "ny");
            configuration.primary_y = Integer.parseInt(ny);
        }

        final Element secondaryElement = element.getChild("secondary");
        if (secondaryElement != null) {
            configuration.useSecondary = true;

            final String nx = JDomUtils.getMandatoryChildTextTrim(secondaryElement, "nx");
            configuration.secondary_x = Integer.parseInt(nx);

            final String ny = JDomUtils.getMandatoryChildTextTrim(secondaryElement, "ny");
            configuration.secondary_y = Integer.parseInt(ny);
        }

        return configuration;
    }
}
