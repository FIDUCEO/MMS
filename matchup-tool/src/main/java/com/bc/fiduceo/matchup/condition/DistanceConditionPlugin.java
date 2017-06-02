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

import static com.bc.fiduceo.util.JDomUtils.getMandatoryChildren;
import static com.bc.fiduceo.util.JDomUtils.getValueFromNamesAttribute;

import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.util.JDomUtils;
import org.esa.snap.core.util.StringUtils;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

public class DistanceConditionPlugin implements ConditionPlugin {

    public static final String TAG_NAME_CONDITION_NAME = "spherical-distance";
    public static final String TAG_NAME_MAX_PIXEL_DISTANCE_KM = "max-pixel-distance-km";

    @Override
    public Condition createCondition(Element element) {
        if (!getConditionName().equals(element.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + getConditionName() + "' expected.");
        }

        DistanceCondition noSecondaryNameCondition = null;

        final List<Element> children = getMandatoryChildren(element, TAG_NAME_MAX_PIXEL_DISTANCE_KM);
        final ArrayList<DistanceCondition> conditions = new ArrayList<>();
        for (Element child : children) {
            final Double maxDistanceInKm = Double.valueOf(JDomUtils.getMandatoryText(child));
            final String names = getValueFromNamesAttribute(child);
            if (StringUtils.isNullOrEmpty(names)) {
                if (noSecondaryNameCondition != null) {
                    throw new RuntimeException("In the mode 'no secondary sensor names' it is not allowed to define a DistanceCondition twice.");
                }
                final String secondarySensorName = SampleSet.getOnlyOneSecondaryKey();
                noSecondaryNameCondition = new DistanceCondition(maxDistanceInKm);
                noSecondaryNameCondition.setSecondarySensorName(secondarySensorName);
            } else  {
                final String[] strings = StringUtils.stringToArray(names, ",");
                for (String secondarySensorName : strings) {
                    final DistanceCondition condition = new DistanceCondition(maxDistanceInKm);
                    condition.setSecondarySensorName(secondarySensorName);
                    conditions.add(condition);
                }
            }
        }

        if (noSecondaryNameCondition != null) {
            if (conditions.size()>0){
                throw new RuntimeException("It is not allowed to define distance conditions with and without secondary sensor names concurrently.");
            }
            return noSecondaryNameCondition;
        }
        if (conditions.size() == 1) {
            return conditions.get(0);
        }
        return (matchupSet, context) -> {
            for (DistanceCondition condition : conditions) {
                condition.apply(matchupSet, context);
            }
        };
    }

    @Override
    public String getConditionName() {
        return TAG_NAME_CONDITION_NAME;
    }
}
