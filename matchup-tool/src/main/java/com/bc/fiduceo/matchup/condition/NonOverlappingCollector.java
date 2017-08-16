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

import com.bc.fiduceo.core.Sample;
import com.bc.fiduceo.matchup.SampleSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

class NonOverlappingCollector {

    private final int width;
    private final int height;
    private final boolean primary;

    private final ArrayList<SampleSet> sampleSets;
    private final NavigableSet<Sample> samples;
    private String secondaryName;

    @SuppressWarnings("SuspiciousNameCombination")
    NonOverlappingCollector(int width, int height, boolean primary) {
        this(width, height, primary, SampleSet.getOnlyOneSecondaryKey());
    }

    NonOverlappingCollector(int width, int height, boolean primary, String secondaryName) {
        this.width = width;
        this.height = height;
        this.primary = primary;
        this.secondaryName = secondaryName;

        sampleSets = new ArrayList<>();

        // @todo 2 tb/** the same comparison is performed in OverlapRemoveCondition - extract class and unify tb 2017-08-16
        final Comparator<Sample> orderedComparator = (o1, o2) -> {
            final int compareY = Integer.compare(o1.getY(), o2.getY());
            if (compareY == 0) {
                return Integer.compare(o1.getX(), o2.getX());
            } else {
                return compareY;
            }
        };

        samples = new TreeSet<>(orderedComparator);
    }

    void add(SampleSet sampleSet) {
        final Sample inputSample = getSample(sampleSet);
        if (hasOverlap(inputSample)) {
            return;
        }

        sampleSets.add(sampleSet);
        samples.add(inputSample);
    }

    List<SampleSet> get() {
        return sampleSets;
    }

    boolean areOverlapping(Sample p, Sample q) {
        return Math.abs(p.getX() - q.getX()) < width && Math.abs(p.getY() - q.getY()) < height;
    }

    Sample getSample(SampleSet sampleSet) {
        if (primary) {
            return sampleSet.getPrimary();
        }

        return sampleSet.getSecondary(secondaryName);
    }

    private boolean hasOverlap(Sample inputSample) {
        for (Iterator<Sample> iterator = samples.descendingIterator(); iterator.hasNext(); ) {
            final Sample other = iterator.next();
            final int otherY = other.getY();
            final int inputSampleY = inputSample.getY();
            if (otherY < inputSampleY - height) {
                break;
            }
            if (otherY > inputSampleY + height) {
                continue;
            }
            if (areOverlapping(inputSample, other)) {
                return true;
            }
        }
        return false;
    }
}
