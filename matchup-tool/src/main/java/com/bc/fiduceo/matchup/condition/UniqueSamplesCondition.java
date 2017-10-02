/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.core.Sample;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.math.SphericalDistance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UniqueSamplesCondition implements Condition {

    private final String referenceSensorKey;
    private final String associatedSensorKey;

    private boolean isReferencePrimary;

    UniqueSamplesCondition(Configuration configuration) {
        this.referenceSensorKey = configuration.referenceSensorKey;
        this.associatedSensorKey = configuration.associatedSensorKey;
    }

    @Override
    public void apply(MatchupSet matchupSet, ConditionEngineContext context) {
        isReferencePrimary = checkReferenceLocation(matchupSet);
        final HashMap<String, ReferenceContainer> references = createReferenceSet(matchupSet);

        final List<SampleSet> resultList = new ArrayList<>();

        final Set<Map.Entry<String, ReferenceContainer>> entries = references.entrySet();
        for (final Map.Entry<String, ReferenceContainer> entry : entries) {
            final ReferenceContainer referenceContainer = entry.getValue();
            final List<Sample> associatedList = referenceContainer.associated;
            if (associatedList.size() == 1) {
                final SampleSet sampleSet = createSampleSet(referenceContainer.reference, referenceContainer.associated.get(0));
                resultList.add(sampleSet);
                continue;
            }

            final double refLon = referenceContainer.reference.getLon();
            final double refLat = referenceContainer.reference.getLat();
            double minDistance = Double.MAX_VALUE;
            Sample closest = null;
            final SphericalDistance sphericalDistance = new SphericalDistance(refLon, refLat);
            for (final Sample associated : associatedList) {
                final double distance = sphericalDistance.distance(associated.getLon(), associated.getLat());
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = associated;
                }
            }
            final SampleSet sampleSet = createSampleSet(referenceContainer.reference, closest);
            resultList.add(sampleSet);
        }

        matchupSet.setSampleSets(resultList);
    }

    private SampleSet createSampleSet(Sample reference, Sample associated) {
        final SampleSet sampleSet = new SampleSet();
        if (isReferencePrimary) {
            sampleSet.setPrimary(reference);
            sampleSet.setSecondary(associatedSensorKey, associated);
        } else {
            sampleSet.setPrimary(associated);
            sampleSet.setSecondary(referenceSensorKey, reference);
        }

        return sampleSet;
    }

    private boolean checkReferenceLocation(MatchupSet matchupSet) {
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        if (sampleSets.size() == 0) {
            return false;
        }

        final SampleSet sampleSet = sampleSets.get(0);
        final Sample secondary = sampleSet.getSecondary(referenceSensorKey);
        return secondary == null;
    }

    private HashMap<String, ReferenceContainer> createReferenceSet(MatchupSet matchupSet) {
        final HashMap<String, ReferenceContainer> references = new HashMap<>();

        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        for (final SampleSet sampleSet : sampleSets) {
            final Sample reference = getReference(sampleSet);
            final String key = createKey(reference);

            final ReferenceContainer referenceContainer = references.computeIfAbsent(key, k -> new ReferenceContainer(reference));

            final Sample associated = getAssociated(sampleSet);
            referenceContainer.associated.add(associated);
        }
        return references;
    }

    // package access for testing only tb 2017-10-02
    static String createKey(Sample reference) {
        final int x = reference.getX();
        final int y = reference.getY();
        return x + "_" + y;
    }

    // package access for testing only tb 2017-10-02
    Sample getReference(SampleSet sampleSet) {
        final Sample reference = sampleSet.getSecondary(referenceSensorKey);
        if (reference == null) {
            return sampleSet.getPrimary();
        }
        return reference;
    }

    // package access for testing only tb 2017-10-02
    Sample getAssociated(SampleSet sampleSet) {
        final Sample reference = sampleSet.getSecondary(referenceSensorKey);
        if (reference != null) {
            return sampleSet.getPrimary();
        }
        return sampleSet.getSecondary(associatedSensorKey);
    }

    private class ReferenceContainer {
        Sample reference;
        List<Sample> associated;

        ReferenceContainer(Sample reference) {
            this.reference = reference;
            associated = new ArrayList<>();
        }
    }

    static class Configuration {
        String referenceSensorKey;
        String associatedSensorKey;
    }
}
