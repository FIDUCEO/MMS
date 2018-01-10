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

import static com.bc.fiduceo.core.UseCaseConfig.load;
import static com.bc.fiduceo.util.JDomUtils.setNameAttribute;
import static com.bc.fiduceo.util.JDomUtils.setNamesAttribute;

import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.core.UseCaseConfigBuilder;
import com.bc.fiduceo.matchup.condition.ConditionEngine;
import com.bc.fiduceo.matchup.condition.DistanceConditionPlugin;
import com.bc.fiduceo.matchup.condition.OverlapRemoveConditionPlugin;
import com.bc.fiduceo.matchup.condition.TimeDeltaConditionPlugin;
import org.esa.snap.core.util.StringUtils;
import org.jdom.Element;

public class MatchupToolTestUseCaseConfigBuilder extends UseCaseConfigBuilder {

    public MatchupToolTestUseCaseConfigBuilder(String name) {
        super(name);

    }

    public MatchupToolTestUseCaseConfigBuilder withTimeDeltaSeconds(long seconds, String secondarySensorName) {
        final Element conditions = ensureChild(getRootElement(), ConditionEngine.TAG_NAME_CONDITIONS);
        final Element timeDelta = ensureChild(conditions, TimeDeltaConditionPlugin.TAG_NAME_CONDITION_NAME);
        final Element childElem = addChild(timeDelta, TimeDeltaConditionPlugin.TAG_NAME_TIME_DELTA_SECONDS, seconds);
        if (secondarySensorName != null) {
            setNamesAttribute(childElem, secondarySensorName);
        }
        return this;
    }

    public MatchupToolTestUseCaseConfigBuilder withMaxPixelDistanceKm(float distance, String secondarySensorName) {
        final Element conditions = ensureChild(getRootElement(), ConditionEngine.TAG_NAME_CONDITIONS);
        final Element sphericalDistance = ensureChild(conditions, DistanceConditionPlugin.TAG_NAME_CONDITION_NAME);
        final Element childElem = addChild(sphericalDistance, DistanceConditionPlugin.TAG_NAME_MAX_PIXEL_DISTANCE_KM, distance);
        if (secondarySensorName != null) {
            setNamesAttribute(childElem, secondarySensorName);
        }
        return this;
    }

    public MatchupToolTestUseCaseConfigBuilder withOverlapRemoval(String reference) {
        final Element conditions = ensureChild(getRootElement(), ConditionEngine.TAG_NAME_CONDITIONS);
        final Element overlapRemove = ensureChild(conditions, OverlapRemoveConditionPlugin.TAG_NAME_CONDITION_NAME);
        addChild(overlapRemove, "reference", reference);
        return this;
    }

    public UseCaseConfig createConfig() {
        final UseCaseConfig useCaseConfig = load(getStream());
        useCaseConfig.setTestRun(true);
        return useCaseConfig;
    }

    MatchupToolTestUseCaseConfigBuilder withLocationElement(double lon, double lat) {
        final Element location = ensureChild(getRootElement(), "location");

        addChild(location, "lon", Double.toString(lon));
        addChild(location, "lat", Double.toString(lat));

        return this;
    }

    UseCaseConfigBuilder withAngularScreening(String primaryVariableName, String secondaryVariableName, float maxPrimaryVza, float maxSecondaryVza, float maxDelta) {
        final Element screenings = ensureChild(getRootElement(), "screenings");
        final Element angular = ensureChild(screenings, "angular");

        final Element primaryVariable = ensureChild(angular, "primary-vza-variable");
        setNameAttribute(primaryVariable, primaryVariableName);

        final Element secondaryVariable = ensureChild(angular, "secondary-vza-variable");
        setNameAttribute(secondaryVariable, secondaryVariableName);

        if (!Float.isNaN(maxPrimaryVza)) {
            addChild(angular, "max-primary-vza", maxPrimaryVza);
        }

        if (!Float.isNaN(maxSecondaryVza)) {
            addChild(angular, "max-secondary-vza", maxSecondaryVza);
        }

        if (!Float.isNaN(maxDelta)) {
            addChild(angular, "max-angle-delta", maxDelta);
        }

        return this;
    }

    UseCaseConfigBuilder withAngularCosineScreening(String primaryVZAName, String secondaryVZAName, float threshold) {
        final Element screenings = ensureChild(getRootElement(), "screenings");
        final Element angular = ensureChild(screenings, "angular-cosine-proportion");

        final Element primaryVariable = ensureChild(angular, "primary-variable");
        setNameAttribute(primaryVariable, primaryVZAName);

        final Element secondaryVariable = ensureChild(angular, "secondary-variable");
        setNameAttribute(secondaryVariable, secondaryVZAName);

        if (!Float.isNaN(threshold)) {
            addChild(angular, "threshold", threshold);
        }

        return this;
    }

    UseCaseConfigBuilder withHIRS_LZA_Screening(float maxAngularDelta) {
        final Element screenings = ensureChild(getRootElement(), "screenings");
        final Element lzaDelta = ensureChild(screenings, "hirs-lza-delta");

        if (!Float.isNaN(maxAngularDelta)) {
            addChild(lzaDelta, "max-lza-delta", maxAngularDelta);
        }

        return this;
    }

    UseCaseConfigBuilder withPixelValueScreening(String primaryExpression, String secondaryExpression) {
        final Element screenings = ensureChild(getRootElement(), "screenings");
        final Element pixelScreening = ensureChild(screenings, "pixel-value");

        if (StringUtils.isNotNullAndNotEmpty(primaryExpression)) {
            addChild(pixelScreening, "primary-expression", primaryExpression);
        }

        if (StringUtils.isNotNullAndNotEmpty(secondaryExpression)) {
            addChild(pixelScreening, "secondary-expression", secondaryExpression);
        }

        return this;
    }

    MatchupToolTestUseCaseConfigBuilder withAtsrAngularScreening(double nadirAngleDelta, double fwardAngleDelta) {
        final Element screenings = ensureChild(getRootElement(), "screenings");
        final Element angularScreening = ensureChild(screenings, "atsr-angular");

        addChild(angularScreening, "angle-delta-nadir", Double.toString(nadirAngleDelta));
        addChild(angularScreening, "angle-delta-fward", Double.toString(fwardAngleDelta));

        return this;
    }

    UseCaseConfigBuilder withBuehlerCloudScreening(String primaryNarrow, String primaryWide, String primaryVza, String secondaryNarrow, String secondaryWide, String secondaryVza) {
        final Element screenings = ensureChild(getRootElement(), "screenings");
        final Element angularScreening = ensureChild(screenings, "buehler-cloud");

        Element element = addChild(angularScreening, "primary-narrow-channel");
        setNameAttribute(element, primaryNarrow);

        element = addChild(angularScreening, "primary-wide-channel");
        setNameAttribute(element, primaryWide);

        element = addChild(angularScreening, "primary-vza");
        setNameAttribute(element, primaryVza);

        element = addChild(angularScreening, "secondary-narrow-channel");
        setNameAttribute(element, secondaryNarrow);

        element = addChild(angularScreening, "secondary-wide-channel");
        setNameAttribute(element, secondaryWide);

        element = addChild(angularScreening, "secondary-vza");
        setNameAttribute(element, secondaryVza);

        return this;
    }
}
