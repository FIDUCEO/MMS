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

        if (configuration.isPrimary) {
            applyPrimary(sampleSets, result);
        } else {
            applySecondaries(sampleSets, result);
        }

        matchupSet.setSampleSets(result);
        sampleSets.clear();
    }

    private void applyPrimary(List<SampleSet> sampleSets, List<SampleSet> result) {
        final int minX = configuration.minX;
        final int maxX = configuration.maxX;
        final int minY = configuration.minY;
        final int maxY = configuration.maxY;

        for (SampleSet sampleSet : sampleSets) {
            final Sample primary = sampleSet.getPrimary();
            final int primaryX = primary.getX();
            final int primaryY = primary.getY();
            if (primaryX < minX || primaryX > maxX || primaryY < minY || primaryY > maxY) {
                continue;
            }
            result.add(sampleSet);
        }
    }

    private void applySecondaries(List<SampleSet> sampleSets, List<SampleSet> result) {
        final int minX = configuration.minX;
        final int maxX = configuration.maxX;
        final int minY = configuration.minY;
        final int maxY = configuration.maxY;

        final String[] secondaryNames;
        if (configuration.secondaryNames.length == 0) {
            secondaryNames = new String[]{SampleSet.getOnlyOneSecondaryKey()};
        } else {
            secondaryNames = configuration.secondaryNames;
        }

        for (SampleSet sampleSet : sampleSets) {
            boolean keep = true;
            for (final String secondaryName : secondaryNames) {
                final Sample secondary = sampleSet.getSecondary(secondaryName);
                if (secondary == null) {
                    continue;   // we cannot assume all sensors in all matchups tb 2019-10-30
                }
                final int primaryX = secondary.getX();
                final int primaryY = secondary.getY();
                if (primaryX < minX || primaryX > maxX || primaryY < minY || primaryY > maxY) {
                    keep = false;
                }
            }

            if (keep) {
                result.add(sampleSet);
            }
        }
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
