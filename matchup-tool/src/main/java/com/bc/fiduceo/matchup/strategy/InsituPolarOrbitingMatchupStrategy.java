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

package com.bc.fiduceo.matchup.strategy;

import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.condition.ConditionEngine;
import com.bc.fiduceo.matchup.condition.ConditionEngineContext;
import com.bc.fiduceo.matchup.screening.ScreeningEngine;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.tool.ToolContext;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

class InsituPolarOrbitingMatchupStrategy extends AbstractMatchupStrategy {

    InsituPolarOrbitingMatchupStrategy(Logger logger) {
        super(logger);
    }

    @Override
    public MatchupCollection createMatchupCollection(ToolContext context) throws SQLException, IOException, InvalidRangeException {
        final MatchupCollection matchupCollection = new MatchupCollection();

        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();

        final ConditionEngine conditionEngine = new ConditionEngine();
        final ConditionEngineContext conditionEngineContext = ConditionEngine.createContext(context);
        conditionEngine.configure(useCaseConfig);

        final ScreeningEngine screeningEngine = new ScreeningEngine();
        screeningEngine.configure(useCaseConfig);

        final ReaderFactory readerFactory = ReaderFactory.get(context.getGeometryFactory());

        final long timeDeltaInMillis = conditionEngine.getMaxTimeDeltaInMillis();
        final int timeDeltaSeconds = (int) (timeDeltaInMillis / 1000);

        final List<SatelliteObservation> insituObservations = getPrimaryObservations(context);
        for (final SatelliteObservation insituObservation : insituObservations) {
            try (final Reader primaryReader = readerFactory.getReader(insituObservation.getSensor().getName())) {
                final Date searchTimeStart = TimeUtils.addSeconds(-timeDeltaSeconds, insituObservation.getStartTime());
                final Date searchTimeEnd = TimeUtils.addSeconds(timeDeltaSeconds, insituObservation.getStopTime());

                // @todo 1 tb/tb intersect time interval with processing interval 2016-11-04
                // @todo 1 tb/tb continue here 2016-11-ß4

                System.out.println("searchTimeStart = " + searchTimeStart);
                System.out.println("searchTimeEnd = " + searchTimeEnd);
            }
        }

        return matchupCollection;
    }
}
