/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.fiduceo.matchup;

import com.bc.fiduceo.core.UseCaseConfigBuilder;
import com.bc.fiduceo.matchup.condition.ConditionEngine;
import com.bc.fiduceo.matchup.condition.DistanceConditionPlugin;
import com.bc.fiduceo.matchup.condition.TimeDeltaConditionPlugin;
import org.jdom.Element;

import static com.bc.fiduceo.matchup.condition.DistanceConditionPlugin.TAG_NAME_MAX_PIXEL_DISTANCE_KM;
import static com.bc.fiduceo.matchup.condition.TimeDeltaConditionPlugin.TAG_NAME_TIME_DELTA_SECONDS;

public class MatchupToolUseCaseConfigBuilder extends UseCaseConfigBuilder {

    public MatchupToolUseCaseConfigBuilder(String name) {
        super(name);
    }

    public MatchupToolUseCaseConfigBuilder withTimeDeltaSeconds(long seconds) {
        final Element conditions = ensureChild(getRootElement(), ConditionEngine.TAG_NAME_CONDITIONS);
        final Element timeDelta = ensureChild(conditions, TimeDeltaConditionPlugin.TAG_NAME_CONDITION_NAME);
        addChild(timeDelta, TAG_NAME_TIME_DELTA_SECONDS, seconds);
        return this;
    }

    public MatchupToolUseCaseConfigBuilder withMaxPixelDistanceKm(float distance) {
        final Element conditions = ensureChild(getRootElement(), ConditionEngine.TAG_NAME_CONDITIONS);
        final Element sphericalDistance = ensureChild(conditions, DistanceConditionPlugin.TAG_NAME_CONDITION_NAME );
        addChild(sphericalDistance, TAG_NAME_MAX_PIXEL_DISTANCE_KM, distance);
        return this;
    }

    public UseCaseConfigBuilder withAngularScreening(String primaryVariableName, String secondaryVariableName, float maxPrimaryVza, float maxSecondaryVza, float maxDelta) {
        final Element screenings = ensureChild(getRootElement(), "screenings");
        final Element angular = ensureChild(screenings, "angular");

        final Element primaryVariable = ensureChild(angular, "primaryVZAVariable");
        addAttribute(primaryVariable, "name", primaryVariableName);

        final Element secondaryVariable = ensureChild(angular, "secondaryVZAVariable");
        addAttribute(secondaryVariable, "name", secondaryVariableName);

        if (!Float.isNaN(maxPrimaryVza)) {
            addChild(angular, "maxPrimaryVZA", maxPrimaryVza);
        }

        if (!Float.isNaN(maxSecondaryVza)) {
            addChild(angular, "maxSecondaryVZA", maxSecondaryVza);
        }

        if (!Float.isNaN(maxDelta)) {
            addChild(angular, "maxAngleDelta", maxDelta);
        }

        return this;
    }

    public UseCaseConfigBuilder withAngularCosineScreening(String primaryVZAName, String secondaryVZAName, float threshold) {
        final Element screenings = ensureChild(getRootElement(), "screenings");
        final Element angular = ensureChild(screenings, "angular-cosine-proportion");

        final Element primaryVariable = ensureChild(angular, "primary-variable");
        addAttribute(primaryVariable, "name", primaryVZAName);

        final Element secondaryVariable = ensureChild(angular, "secondary-variable");
        addAttribute(secondaryVariable, "name", secondaryVZAName);

        if (!Float.isNaN(threshold)) {
            addChild(angular, "threshold", threshold);
        }

        return this;
    }

}
