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
import com.bc.fiduceo.core.Sample;
import com.bc.fiduceo.matchup.SampleSet;

import java.util.ArrayList;
import java.util.List;

class BorderDistanceCondition implements Condition {

    private final List<Configuration> configurations;

    BorderDistanceCondition(List<Configuration> configurations) {
        this.configurations = configurations;
    }

    @Override
    public void apply(MatchupSet matchupSet, ConditionEngineContext context) {
        List<SampleSet> sourceSamples = matchupSet.getSampleSets();
        List<SampleSet> targetSamples = sourceSamples;
        for (Configuration configuration : configurations) {
            targetSamples = new ArrayList<>();
            if (configuration.usePrimary) {
                final Dimension primarySize = context.getPrimarySize();
                final int maxXPrimary = primarySize.getNx() - 1 - configuration.primary_x;
                final int maxYPrimary = primarySize.getNy() - 1 - configuration.primary_y;
                for (final SampleSet sampleSet : sourceSamples) {
                    final Sample primary = sampleSet.getPrimary();
                    final int primaryX = primary.getX();
                    if (primaryX < configuration.primary_x || primaryX > maxXPrimary) {
                        continue;
                    }

                    final int primaryY = primary.getY();
                    if (primaryY < configuration.primary_y || primaryY > maxYPrimary) {
                        continue;
                    }
                    targetSamples.add(sampleSet);
                }
            } else if (configuration.useSecondary) {
                final String secondaryName = configuration.secondaryName;
                final Dimension secondarySize = context.getSecondarySize(secondaryName);
                final int maxXSecondary = secondarySize.getNx() - 1 - configuration.secondary_x;
                final int maxYSecondary = secondarySize.getNy() - 1 - configuration.secondary_y;
                for (final SampleSet sampleSet : sourceSamples) {
                    final Sample secondary = sampleSet.getSecondary(secondaryName);
                    final int secondaryX = secondary.getX();
                    if (secondaryX < configuration.secondary_x || secondaryX > maxXSecondary) {
                        continue;
                    }

                    final int secondaryY = secondary.getY();
                    if (secondaryY < configuration.secondary_y || secondaryY > maxYSecondary) {
                        continue;
                    }
                    targetSamples.add(sampleSet);
                }
            }
            sourceSamples.clear();
            sourceSamples = targetSamples;
        }

        matchupSet.setSampleSets(targetSamples);
    }

    static class Configuration {
        int primary_x;
        int primary_y;
        int secondary_x;
        int secondary_y;
        boolean usePrimary;
        boolean useSecondary;
        String secondaryName = SampleSet.getOnlyOneSecondaryKey();
    }
}
