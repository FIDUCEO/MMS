package com.bc.fiduceo.matchup.screening;

import com.bc.fiduceo.matchup.MatchupCollection;

public interface Screening {

    MatchupCollection execute(MatchupCollection matchupCollection);
}
