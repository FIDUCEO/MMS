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
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.SampleSet;

import java.util.ArrayList;
import java.util.List;

/*
 * The XML template for this condition class looks like:
 *
 *      <time-delta>
 *          <time-delta-seconds names="name1, name2, ..."
 *                              secondaryCheck="true">   <!-- default is false -->
 *              300
 *          </time-delta-seconds>
 *      </time-delta>
 *
 *      or
 *
 *      <!-- In this second case for each combination can be defined a discrete time delta -->
 *      <!-- If all time delta have the same value set this example is similar to the example above -->
 *      <time-delta>
 *          <time-delta-seconds names="name1">
 *              300
 *          </time-delta-seconds>
 *          <time-delta-seconds names="name2">
 *              250
 *          </time-delta-seconds>
 *          <time-delta-seconds names="name3,name4,..."
 *                              primaryCheck="false"     <!-- default is true -->
 *                              secondaryCheck="true">   <!-- default is false -->
 *              400
 *          </time-delta-seconds>
 *          ...
 *      </time-delta>
 *
 */
class TimeDeltaCondition implements Condition {

    private final long maxTimeDeltaInMillis;
    private String[] secondarySensorNames = {SampleSet.getOnlyOneSecondaryKey()};
    private boolean primaryCheck = true;
    private boolean secondaryCheck = false;

    TimeDeltaCondition(long maxTimeDeltaInMillis) {
        this.maxTimeDeltaInMillis = maxTimeDeltaInMillis;
    }

    @Override
    public void apply(MatchupSet matchupSet, ConditionEngineContext context) {
        final List<SampleSet> sourceSamples = matchupSet.getSampleSets();
        final List<SampleSet> targetSamples = new ArrayList<>();
        for (final SampleSet sampleSet : sourceSamples) {
            boolean primaryIsValid = isValidDifferenceToPrimary(sampleSet);
            boolean secondaryIsValid = isValidDifferenceBetweenSecondaries(sampleSet);
            if (primaryIsValid && secondaryIsValid) {
                targetSamples.add(sampleSet);
            }
        }
        matchupSet.setSampleSets(targetSamples);
        sourceSamples.clear();
    }

    long getMaxTimeDeltaInMillis() {
        return maxTimeDeltaInMillis;
    }

    String[] getSecondarySensorNames() {
        return secondarySensorNames;
    }

    void setSecondarySensorNames(String... secondarySensorNames) {
        this.secondarySensorNames = secondarySensorNames;
    }

    void setPrimaryCheck(boolean primaryCheck) {
        this.primaryCheck = primaryCheck;
    }

    void setSecondaryCheck(boolean secondaryCheck) {
        this.secondaryCheck = secondaryCheck;
    }

    private boolean isValidDifferenceToPrimary(SampleSet sampleSet) {
        if (primaryCheck) {
            for (String secondarySensorName : secondarySensorNames) {
                final Sample s1 = sampleSet.getPrimary();
                final Sample s2 = sampleSet.getSecondary(secondarySensorName);
                if (isInvalid(s1, s2)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValidDifferenceBetweenSecondaries(SampleSet sampleSet) {
        if (secondaryCheck) {
            final int numNames = secondarySensorNames.length;
            for (int i = 0; i < numNames - 1; i++) {
                final Sample s1 = sampleSet.getSecondary(secondarySensorNames[i]);
                for (int j = i + 1; j < numNames; j++) {
                    final Sample s2 = sampleSet.getSecondary(secondarySensorNames[j]);
                    if (isInvalid(s1, s2)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isInvalid(Sample primary, Sample secondary) {
        final long actualTimeDelta = Math.abs(primary.getTime() - secondary.getTime());
        return actualTimeDelta > maxTimeDeltaInMillis;
    }
}
