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

/* The XML template for this condition class looks like:

    <border-distance>
        <nx>
            2
        </nx>
        <ny>
            2
        </ny>
    </border-distance>
 */

class BorderDistanceCondition implements Condition {

    private final int deltaX;
    private final int deltaY;

    BorderDistanceCondition(int deltaX, int deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    @Override
    public void apply(MatchupSet matchupSet, ConditionsContext context) {
        final int minX = deltaX;
        final int minY = deltaY;

        final Dimension primarySize = context.getPrimarySize();
        final int maxXPrimary = primarySize.getNx() - 1 - deltaX;
        final int maxYPrimary = primarySize.getNy() - 1 - deltaY;

        final Dimension secondarySize = context.getSecondarySize();
        final int maxXSecondary = secondarySize.getNx() - 1 - deltaX;
        final int maxYSecondary = secondarySize.getNy() - 1 - deltaY;


        final List<SampleSet> sourceSamples = matchupSet.getSampleSets();
        final List<SampleSet> targetSamples = new ArrayList<>();
        for (final SampleSet sampleSet : sourceSamples) {
            final Sample primary = sampleSet.getPrimary();
            if (primary.x < minX || primary.x > maxXPrimary) {
                continue;
            }

            if (primary.y < minY || primary.y > maxYPrimary) {
                continue;
            }

            final Sample secondary = sampleSet.getSecondary();
            if (secondary.x < minX || secondary.x > maxXSecondary) {
                continue;
            }

            if (secondary.y < minY || secondary.y > maxYSecondary) {
                continue;
            }

            targetSamples.add(sampleSet);
        }

        matchupSet.setSampleSets(targetSamples);
        sourceSamples.clear();
    }
}
