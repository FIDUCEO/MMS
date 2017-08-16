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

package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.SampleSet;

import java.util.ArrayList;
import java.util.List;

class TimeRangeCondition implements Condition {

    @Override
    public void apply(MatchupSet matchupSet, ConditionEngineContext context) {
        final long endTime = context.getEndDate().getTime();
        final long startTime = context.getStartDate().getTime();

        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        final List<SampleSet> targetSets = new ArrayList<>();
        for (SampleSet sampleSet : sampleSets) {
            final long time = sampleSet.getPrimary().getTime();
            if (time >= startTime && time <= endTime) {
                targetSets.add(sampleSet);
            }
        }
        matchupSet.setSampleSets(targetSets);
        sampleSets.clear();
    }
}
