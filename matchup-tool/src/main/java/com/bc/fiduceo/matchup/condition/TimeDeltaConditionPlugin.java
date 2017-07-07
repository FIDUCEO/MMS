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

import static com.bc.fiduceo.util.JDomUtils.*;

import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.util.JDomUtils;
import org.esa.snap.core.util.StringUtils;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * XML Example needed? see --> {@link TimeDeltaCondition}
 * @see TimeDeltaCondition
 */
public class TimeDeltaConditionPlugin implements ConditionPlugin {

    public static final String TAG_NAME_CONDITION_NAME = "time-delta";
    public static final String TAG_NAME_TIME_DELTA_SECONDS = "time-delta-seconds";

    @Override
    public Condition createCondition(Element element) {
        if (!getConditionName().equals(element.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + getConditionName() + "' expected.");
        }

        TimeDeltaCondition noSecondaryNameCondition = null;

        final List<Element> children = getMandatoryChildren(element, TAG_NAME_TIME_DELTA_SECONDS);
        final ArrayList<TimeDeltaCondition> conditions = new ArrayList<>();
        for (Element child : children) {
            final long maxTimeDeltaInMillis = Long.valueOf(JDomUtils.getMandatoryText(child)) * 1000;
            final String names = getValueFromNamesAttribute(child);
            final boolean primaryCheck = Boolean.parseBoolean(child.getAttributeValue("primaryCheck", "true"));
            final boolean secondaryCheck = Boolean.parseBoolean(child.getAttributeValue("secondaryCheck", "false"));
            if (!primaryCheck && !secondaryCheck) {
                throw new RuntimeException("At least primaryCheck or secondaryCheck mut be true.");
            }
            if (StringUtils.isNullOrEmpty(names)) {
                if (noSecondaryNameCondition != null) {
                    throw new RuntimeException("In the mode 'no secondary sensor names' it is not allowed to define a TimeDeltaCondition twice.");
                }
                final String secondarySensorName = SampleSet.getOnlyOneSecondaryKey();
                noSecondaryNameCondition = new TimeDeltaCondition(maxTimeDeltaInMillis);
                noSecondaryNameCondition.setSecondarySensorNames(secondarySensorName);
            } else {
                final String[] strings = Stream.of(names.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
                if (secondaryCheck && strings.length < 2) {
                    throw new RuntimeException("If secondaryCheck is true at least two secondary sensor names are needed.");
                }
                final TimeDeltaCondition condition = new TimeDeltaCondition(maxTimeDeltaInMillis);
                condition.setPrimaryCheck(primaryCheck);
                condition.setSecondaryCheck(secondaryCheck);
                condition.setSecondarySensorNames(strings);
                conditions.add(condition);
            }
        }

        if (noSecondaryNameCondition != null) {
            if (conditions.size() > 0) {
                throw new RuntimeException("It is not allowed to define time delta conditions with and without secondary sensor names concurrently.");
            }
            return noSecondaryNameCondition;
        }
        if (conditions.size() == 1) {
            return conditions.get(0);
        }
        return (matchupSet, context) -> {
            for (TimeDeltaCondition condition : conditions) {
                condition.apply(matchupSet, context);
            }
        };
    }

    @Override
    public String getConditionName() {
        return TAG_NAME_CONDITION_NAME;
    }
}
