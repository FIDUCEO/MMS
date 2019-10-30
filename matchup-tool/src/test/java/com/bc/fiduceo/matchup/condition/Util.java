package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.core.Sample;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.SampleSet;

import java.util.List;

class Util {

    static void addSampleSet(int primaryX, int primaryY, int secondaryX, int secondaryY,  MatchupSet matchupSet) {
        addSampleSet(primaryX, primaryY, secondaryX, secondaryY, SampleSet.getOnlyOneSecondaryKey(), matchupSet);
    }

    static SampleSet addSampleSet(int primaryX, int primaryY, int secondaryX, int secondaryY, String secondaryName, MatchupSet matchupSet) {
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(primaryX, primaryY, -22.5, 18.98, 111027));
        sampleSet.setSecondary(secondaryName, new Sample(secondaryX, secondaryY, -23.5, 19.98, 121027));
        sampleSets.add(sampleSet);

        return sampleSet;
    }
}
