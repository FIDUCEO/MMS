package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.core.Sample;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.SampleSet;

import java.util.ArrayList;
import java.util.List;

class PixelPositionCondition implements Condition {

    private final Configuration configuration;

    PixelPositionCondition(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void apply(MatchupSet matchupSet, ConditionEngineContext context) {
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        final List<SampleSet> result = new ArrayList<>();

        final int minX = this.configuration.minX;
        final int maxX = this.configuration.maxX;
        final int minY = this.configuration.minY;
        final int maxY = this.configuration.maxY;

        for(SampleSet sampleSet : sampleSets) {
            final Sample primary = sampleSet.getPrimary();
            final int primaryX = primary.getX();
            final int primaryY = primary.getY();
            if(primaryX >= minX && primaryX <= maxX && primaryY >= minY && primaryY <= maxY) {
                result.add(sampleSet);
            }
        }

        matchupSet.setSampleSets(result);
        sampleSets.clear();
    }

    static class Configuration {
        int minX;
        int maxX;
        int minY;
        int maxY;
        boolean isPrimary;
        String[] secondaryNames;

        public Configuration() {
            minX = Integer.MIN_VALUE;
            maxX = Integer.MAX_VALUE;
            minY = Integer.MIN_VALUE;
            maxY = Integer.MAX_VALUE;
            isPrimary = true;
            secondaryNames = new String[0];
        }
    }
}
