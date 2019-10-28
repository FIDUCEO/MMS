package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.matchup.MatchupSet;

class PixelPositionCondition implements Condition {

    @Override
    public void apply(MatchupSet matchupSet, ConditionEngineContext context) {
        throw new RuntimeException("not implemented");
    }

    static class Configuration {
        int minX;
        int maxX;
        int minY;
        int maxY;

        public Configuration() {
            minX = Integer.MIN_VALUE;
            maxX = Integer.MAX_VALUE;
            minY = Integer.MIN_VALUE;
            maxY = Integer.MAX_VALUE;
        }
    }
}
