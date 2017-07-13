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


import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.util.JDomUtils;
import org.jdom.Element;

import java.util.stream.Stream;

public class OverlapRemoveConditionPlugin implements ConditionPlugin {

    public static final String TAG_NAME_CONDITION_NAME = "overlap-remove";

    @Override
    public Condition createCondition(Element element) {
        final Element referenceElem = JDomUtils.getMandatoryChild(element, "reference");
        final String referenceText = referenceElem.getTextTrim();
        if ("PRIMARY".equals(referenceText)) {
            return new OverlapRemoveCondition();
        } else if ("SECONDARY".equals(referenceText)) {
            final String namesFromAtt = JDomUtils.getValueFromNamesAttribute(referenceElem);
            final String[] names;
            if (namesFromAtt != null) {
                names = Stream.of(namesFromAtt.split(",")).map(String::trim).filter(s -> s.length() > 0).toArray(String[]::new);
            } else {
                names = null;
            }
            if (names == null || names.length == 0) {
                return new OverlapRemoveCondition(SampleSet.getOnlyOneSecondaryKey());
            }
            if (names.length == 1) {
                return new OverlapRemoveCondition(names[0]);
            }
            final Condition[] conditions = new Condition[names.length];
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                conditions[i] = new OverlapRemoveCondition(name);
            }
            return (matchupSet, context) -> {
                for (Condition condition : conditions) {
                    condition.apply(matchupSet, context);
                }
            };
        }
        throw new RuntimeException("Invalid reference for overlap removal: " + referenceText);
    }

    @Override
    public String getConditionName() {
        return TAG_NAME_CONDITION_NAME;
    }
}
