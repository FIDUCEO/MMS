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

import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

/* The XML template for this condition class looks like:

    <overlap-remove>
        <reference>PRIMARY</reference>
    </overlap-remove>

    Valid reference values are: PRIMARY, SECONDARY
 */
class OverlapRemoveCondition implements Condition {

    private final boolean primary;
    private final String secondaryName;

    OverlapRemoveCondition() {
        primary = true;
        secondaryName = null;
    }

    OverlapRemoveCondition(final String secondaryName) {
        this.primary = false;
        this.secondaryName = secondaryName;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void apply(MatchupSet matchupSet, ConditionEngineContext context) {
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        if (sampleSets.size() <= 1) {
            return;
        }

        final Dimension dimension = getDimension(context);

        // we order the sample sets here by location in the file. First top-down then left-right.
        // This way we can apply a very fast overlap-remove operation as we just need to check the close vicinity
        // of the matchup under investigation tb 2016-11-23
        final Comparator<SampleSet> orderedComparator = (o1, o2) -> {
            final Sample left;
            final Sample right;
            if (primary) {
                left = o1.getPrimary();
                right = o2.getPrimary();
            } else {
                left = o1.getSecondary(secondaryName);
                right = o2.getSecondary(secondaryName);
            }
            final int compareY = Integer.compare(left.getY(), right.getY());
            if (compareY == 0) {
                return Integer.compare(left.getX(), right.getX());
            } else {
                return compareY;
            }
        };

        final NavigableSet<SampleSet> orderedSampleSets = new TreeSet<>(orderedComparator);
        orderedSampleSets.addAll(sampleSets);

        final NonOverlappingCollector collector = new NonOverlappingCollector(dimension.getNx(), dimension.getNy(), primary, secondaryName);
        for (final SampleSet sampleSet : orderedSampleSets) {
            collector.add(sampleSet);
        }

        matchupSet.setSampleSets(collector.get());
    }

    Dimension getDimension(ConditionEngineContext context) {
        if (primary) {
            return context.getPrimaryExtractSize();
        }
        // todo se multisensor
        return context.getSecondaryExtractSize(SampleSet.getOnlyOneSecondaryKey());
    }
}
