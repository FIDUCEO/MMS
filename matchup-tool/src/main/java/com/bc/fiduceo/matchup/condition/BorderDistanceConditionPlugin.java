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


public class BorderDistanceConditionPlugin implements ConditionPlugin {

    @Override
    public Condition createCondition(Element element) {
        if (!getConditionName().equals(element.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + getConditionName() + "' expected.");
        }

        final String nx = JDomUtils.getMandatoryChildTextTrim(element, "nx");
        final String ny = JDomUtils.getMandatoryChildTextTrim(element, "ny");

        return new BorderDistanceCondition(Integer.parseInt(nx), Integer.parseInt(ny));
    }

    @Override
    public String getConditionName() {
        return "border-distance";
    }
}
