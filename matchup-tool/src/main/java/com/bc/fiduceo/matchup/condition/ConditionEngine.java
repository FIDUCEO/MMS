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

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.tool.ToolContext;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

public class ConditionEngine {

    public static final String TAG_NAME_CONDITIONS = "conditions";
    private final List<Condition> conditionsList;

    public ConditionEngine() {
        conditionsList = new ArrayList<>();
    }

    public void process(MatchupSet matchupSet, ConditionEngineContext context) {
        for (final Condition condition : conditionsList) {
            condition.apply(matchupSet, context);
        }
    }

    @SuppressWarnings("unchecked")
    public void configure(UseCaseConfig useCaseConfig) {
        final Element conditionsElem = useCaseConfig.getDomElement(TAG_NAME_CONDITIONS);
        if (conditionsElem != null) {
            final ConditionFactory conditionFactory = ConditionFactory.get();

            final List<Element> children = conditionsElem.getChildren();
            for (Element child : children) {
                final Condition condition = conditionFactory.getCondition(child);
                if (condition != null) {
                    conditionsList.add(condition);
                }
            }
        }
        
        conditionsList.add(new TimeRangeCondition());
    }

    public long getMaxTimeDeltaInMillis() {
        for (Condition condition : conditionsList) {
            if (condition instanceof TimeDeltaCondition) {
                final TimeDeltaCondition tdc = (TimeDeltaCondition) condition;
                return tdc.getMaxTimeDeltaInMillis();
            }
        }
        return 0;   // @todo 2 tb /** should'nt we return a large number here? If no time condition is set,
        // the user does not want to check time differences, return 0 does the opposite 2016-09-20
    }

    public static ConditionEngineContext createContext(ToolContext context) {
        final ConditionEngineContext conditionEngineContext = new ConditionEngineContext();
        conditionEngineContext.setStartDate(context.getStartDate());
        conditionEngineContext.setEndDate(context.getEndDate());
        conditionEngineContext.validateTime(); // don't remove this line!

        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();

        final Sensor primarySensor = useCaseConfig.getPrimarySensor();
        final Dimension primaryExtractSize = useCaseConfig.getDimensionFor(primarySensor.getName());
        conditionEngineContext.setPrimaryExtractSize(primaryExtractSize);

        final List<Sensor> secondarySensors = useCaseConfig.getSecondarySensors();
        if (secondarySensors.size() == 1) {
            final Dimension secondaryExtractSize = useCaseConfig.getDimensionFor(secondarySensors.get(0).getName());
            conditionEngineContext.setSecondaryExtractSize(secondaryExtractSize, SampleSet.getOnlyOneSecondaryKey());
        } else {
            for (Sensor secondarySensor : secondarySensors) {
                final String sensorName = secondarySensor.getName();
                final Dimension secondaryExtractSize = useCaseConfig.getDimensionFor(sensorName);
                conditionEngineContext.setSecondaryExtractSize(secondaryExtractSize, sensorName);
            }
        }

        return conditionEngineContext;
    }
}
