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
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.ObservationsSet;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.matchup.condition.ConditionEngine;
import com.bc.fiduceo.matchup.condition.ConditionEngineContext;
import com.bc.fiduceo.matchup.screening.ScreeningEngine;
import com.bc.fiduceo.math.Intersection;
import com.bc.fiduceo.math.IntersectionEngine;
import com.bc.fiduceo.math.TimeInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.tool.ToolContext;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

class PolarOrbitingMatchupStrategy extends AbstractMatchupStrategy {


    PolarOrbitingMatchupStrategy(Logger logger) {
        super(logger);
    }

    public MatchupCollection createMatchupCollection(ToolContext context) throws SQLException, IOException, InvalidRangeException {
        final MatchupCollection matchupCollection = new MatchupCollection();

        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();

        final ConditionEngine conditionEngine = new ConditionEngine();
        final ConditionEngineContext conditionEngineContext = ConditionEngine.createContext(context);
        conditionEngine.configure(useCaseConfig);

        final ScreeningEngine screeningEngine = new ScreeningEngine(context);

        final ReaderFactory readerFactory = ReaderFactory.get();

        final long timeDeltaInMillis = conditionEngine.getMaxTimeDeltaInMillis();
        final int timeDeltaSeconds = (int) (timeDeltaInMillis / 1000);

        final List<SatelliteObservation> primaryObservations = getPrimaryObservations(context);
        for (final SatelliteObservation primaryObservation : primaryObservations) {
            try (final Reader primaryReader = readerFactory.getReader(primaryObservation.getSensor().getName())) {
                final Date searchTimeStart = TimeUtils.addSeconds(-timeDeltaSeconds, primaryObservation.getStartTime());
                final Date searchTimeEnd = TimeUtils.addSeconds(timeDeltaSeconds, primaryObservation.getStopTime());

                // @todo 2 tb/tb extract method
                final Geometry primaryGeoBounds = primaryObservation.getGeoBounds();
                final boolean isPrimarySegmented = AbstractMatchupStrategy.isSegmented(primaryGeoBounds);

                primaryReader.open(primaryObservation.getDataFilePath().toFile());

                final ObservationsSet secondaryObservationsSet = getSecondaryObservations(context, searchTimeStart, searchTimeEnd);

                // todo se multisensor
                // needed by method applyConditionsAndScreenings(...) which is ready to handle multiple secondary sensor
                final HashMap<String, Reader> secondaryReaderMap = new HashMap<>();

                // todo se multisensor
                // create(0) is still only one secondary sensor case
                final String secondarySensorName_CaseOneSecondary = useCaseConfig.getSecondarySensors().get(0).getName();
                // todo se multisensor
                // still only one secondary sensor case
                final List<SatelliteObservation> secondaryObservations = secondaryObservationsSet.get(secondarySensorName_CaseOneSecondary);
                for (final SatelliteObservation secondaryObservation : secondaryObservations) {
                    // todo se multisensor
                    // still only one secondary sensor case
                    try (Reader secondaryReader = readerFactory.getReader(secondarySensorName_CaseOneSecondary)) {
                        secondaryReader.open(secondaryObservation.getDataFilePath().toFile());
                        // todo se multisensor
                        // still only one secondary sensor case
                        secondaryReaderMap.put(secondarySensorName_CaseOneSecondary, secondaryReader);

                        final Intersection[] intersectingIntervals = IntersectionEngine.getIntersectingIntervals(primaryObservation, secondaryObservation);
                        if (intersectingIntervals.length == 0) {
                            continue;
                        }

                        final MatchupSet matchupSet = new MatchupSet();
                        matchupSet.setPrimaryObservationPath(primaryObservation.getDataFilePath());
                        matchupSet.setPrimaryProcessingVersion(primaryObservation.getVersion());
                        // todo se multisensor
                        // still only one secondary sensor case
                        matchupSet.setSecondaryObservationPath(secondarySensorName_CaseOneSecondary, secondaryObservation.getDataFilePath());
                        // todo se multisensor
                        // still only one secondary sensor case
                        matchupSet.setSecondaryProcessingVersion(secondarySensorName_CaseOneSecondary, secondaryObservation.getVersion());

                        // @todo 2 tb/tb extract method
                        final Geometry secondaryGeoBounds = secondaryObservation.getGeoBounds();
                        final boolean isSecondarySegmented = AbstractMatchupStrategy.isSegmented(secondaryGeoBounds);

                        for (final Intersection intersection : intersectingIntervals) {
                            final TimeInfo timeInfo = intersection.getTimeInfo();
                            if (timeInfo.getMinimalTimeDelta() < timeDeltaInMillis) {
                                final PixelLocator primaryPixelLocator = getPixelLocator(primaryReader, isPrimarySegmented, (Polygon) intersection.getPrimaryGeometry());
                                final PixelLocator secondaryPixelLocator = getPixelLocator(secondaryReader, isSecondarySegmented, (Polygon) intersection.getSecondaryGeometry());

                                if (primaryPixelLocator == null || secondaryPixelLocator == null) {
                                    logger.warning("Unable to create valid pixel locators. Skipping intersection segment.");
                                    continue;
                                }

                                SampleCollector sampleCollector = new SampleCollector(context, primaryPixelLocator);
                                sampleCollector.addPrimarySamples((Polygon) intersection.getGeometry(), matchupSet, primaryReader.getTimeLocator());

                                sampleCollector = new SampleCollector(context, secondaryPixelLocator);
                                // todo se multisensor
                                // still only one secondary sensor case
                                final List<SampleSet> completeSamples = sampleCollector.addSecondarySamples(matchupSet.getSampleSets(), secondaryReader.getTimeLocator(), secondarySensorName_CaseOneSecondary);
                                matchupSet.setSampleSets(completeSamples);

                                if (matchupSet.getNumObservations() > 0) {
                                    // todo se multisensor
                                    // still only one secondary sensor case
                                    // uses the secondaryReaderMap instantiated above
                                    applyConditionsAndScreenings(matchupSet, conditionEngine, conditionEngineContext, screeningEngine, primaryReader, secondaryReaderMap);
                                    if (matchupSet.getNumObservations() > 0) {
                                        matchupCollection.add(matchupSet);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return matchupCollection;
    }

}
