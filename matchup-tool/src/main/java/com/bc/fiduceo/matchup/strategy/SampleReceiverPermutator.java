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
package com.bc.fiduceo.matchup.strategy;

import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.esa.snap.core.util.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class SampleReceiverPermutator {

    private final String[] secSensorNames;
    private final Map<String, Map<SatelliteObservation, List<Sample>>> secMaps;
    private final MatchupCollection matchupCollection;
    private Map<Sample, Map<SatelliteObservation, List<Sample>>> currentPrimaryMap;
    private Map<SatelliteObservation, List<Sample>> currentSampleMap;
    private SatelliteObservation currentPrimaryObservation;

    SampleReceiverPermutator(String ... secSensorNames) {
        this.secSensorNames = secSensorNames;
        secMaps = new HashMap<>();
        for (String secSensorName : secSensorNames) {
            secMaps.put(secSensorName, new HashMap<>());
        }
        currentPrimaryMap = new TreeMap<>();
        matchupCollection = new MatchupCollection();
    }

    void setCurrentPrimary(SatelliteObservation primary) {
        createMatchupSetFor(currentPrimaryObservation, currentPrimaryMap);
        currentPrimaryMap.clear();

        currentPrimaryObservation = primary;
    }

    void setPrimarySample(Sample primary) {
        if (currentPrimaryMap.containsKey(primary)) {
            currentSampleMap = currentPrimaryMap.get(primary);
        } else {
            currentSampleMap = new HashMap<>();
            currentPrimaryMap.put(primary, currentSampleMap);
        }
    }

    void addSecondarySample(SatelliteObservation secondary, Sample sample) {
        assertValidSecondarySensorType(secondary);
        final List<Sample> samples;
        if (currentSampleMap.containsKey(secondary)) {
            samples = currentSampleMap.get(secondary);
        } else {
            samples = new ArrayList<>();
            currentSampleMap.put(secondary, samples);
        }
        samples.add(sample);
    }

    private void assertValidSecondarySensorType(SatelliteObservation secondary) {
        final String name = secondary.getSensor().getName();
        final boolean valid = ArrayUtils.isMemberOf(name, secSensorNames);
        if (!valid) {
            throw new RuntimeException("Illegal secondary sensor type.");
        }
    }

    MatchupCollection getPermutations() {
        createMatchupSetFor(currentPrimaryObservation, currentPrimaryMap);
        return matchupCollection;
    }

    /**
     * use this method only in junit level tests
     *
     * @return the current {@link MatchupCollection matchupCollection}
     */
    MatchupCollection getMatchupCollection() {
        return matchupCollection;
    }

    private void createMatchupSetFor(SatelliteObservation primaryObs, Map<Sample, Map<SatelliteObservation, List<Sample>>> samplesMap) {
        if (primaryObs == null) {
            return;
        }
        final MatchupSet matchupSet = new MatchupSet();
        matchupSet.setPrimaryProcessingVersion(primaryObs.getVersion());
        matchupSet.setPrimaryObservationPath(primaryObs.getDataFilePath());
        final ArrayList<SampleSet> sampleSets = new ArrayList<>();
        matchupSet.setSampleSets(sampleSets);

        for (Map.Entry<Sample, Map<SatelliteObservation, List<Sample>>> sampleEntry : samplesMap.entrySet()) {
            for (Map<SatelliteObservation, List<Sample>> map : secMaps.values()) {
                map.clear();
            }
            final Sample primSample = sampleEntry.getKey();
            final Map<SatelliteObservation, List<Sample>> secSamplesMap = sampleEntry.getValue();

            for (Map.Entry<SatelliteObservation, List<Sample>> secSamplesEntry : secSamplesMap.entrySet()) {
                final SatelliteObservation secObs = secSamplesEntry.getKey();
                final List<Sample> samples = secSamplesEntry.getValue();
                final String sensorName = secObs.getSensor().getName();
                secMaps.get(sensorName).put(secObs, samples);
            }


            SampleSet sampleSet = null;
            int depth = 0;
            permuteWithSecundaries(primSample, depth, matchupSet, sampleSets, sampleSet);
        }
        matchupCollection.add(matchupSet);
    }

    private void permuteWithSecundaries(Sample primSample, int depth, MatchupSet matchupSet, ArrayList<SampleSet> sampleSets, SampleSet sampleSet) {
        if (depth >= secSensorNames.length) {
            return;
        }
        final String secSensorName = secSensorNames[depth];
        final Map<SatelliteObservation, List<Sample>> secObsMap = secMaps.get(secSensorName);
        if (secObsMap == null) {
            return;
        }
        for (Map.Entry<SatelliteObservation, List<Sample>> secEntry : secObsMap.entrySet()) {
            final SatelliteObservation secObs = secEntry.getKey();
            matchupSet.setSecondaryProcessingVersion(secSensorName, secObs.getVersion());
            matchupSet.setSecondaryObservationPath(secSensorName, secObs.getDataFilePath());
            for (Sample sample : secEntry.getValue()) {
                if (depth == 0) {
                    sampleSet = new SampleSet();
                    sampleSet.setPrimary(primSample);
                }
                if (depth == secSensorNames.length - 1) {
                    sampleSets.add(sampleSet);
                }
                sampleSet.setSecondary(secSensorName, sample);
                permuteWithSecundaries(primSample, depth + 1, matchupSet, sampleSets, sampleSet);
            }
        }
    }
}
