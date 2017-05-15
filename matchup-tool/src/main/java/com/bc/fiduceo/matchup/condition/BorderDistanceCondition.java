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
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;

import java.util.ArrayList;
import java.util.List;

class BorderDistanceCondition implements Condition {

    private final Configuration configuration;

    BorderDistanceCondition(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void apply(MatchupSet matchupSet, ConditionEngineContext context) {
        final Dimension primarySize = context.getPrimarySize();
        final int maxXPrimary = primarySize.getNx() - 1 - configuration.primary_x;
        final int maxYPrimary = primarySize.getNy() - 1 - configuration.primary_y;

        final Dimension secondarySize = context.getSecondarySize();
        final int maxXSecondary = secondarySize.getNx() - 1 - configuration.secondary_x;
        final int maxYSecondary = secondarySize.getNy() - 1 - configuration.secondary_y;

        final List<SampleSet> sourceSamples = matchupSet.getSampleSets();
        final List<SampleSet> targetSamples = new ArrayList<>();
        for (final SampleSet sampleSet : sourceSamples) {
            if (configuration.usePrimary) {
                final Sample primary = sampleSet.getPrimary();
                if (primary.x < configuration.primary_x || primary.x > maxXPrimary) {
                    continue;
                }

                if (primary.y < configuration.primary_y || primary.y > maxYPrimary) {
                    continue;
                }
            }

            if (configuration.useSecondary) {
                final Sample secondary = sampleSet.getSecondary(SampleSet.ONLY_ONE_SECONDARY);
                if (secondary.x < configuration.secondary_x || secondary.x > maxXSecondary) {
                    continue;
                }

                if (secondary.y < configuration.secondary_y || secondary.y > maxYSecondary) {
                    continue;
                }
            }

            targetSamples.add(sampleSet);
        }

        matchupSet.setSampleSets(targetSamples);
        sourceSamples.clear();
    }

    static class Configuration {
        int primary_x;
        int primary_y;
        int secondary_x;
        int secondary_y;
        boolean usePrimary;
        boolean useSecondary;
    }
}
