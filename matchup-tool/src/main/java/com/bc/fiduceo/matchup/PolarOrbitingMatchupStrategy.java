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

package com.bc.fiduceo.matchup;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.QueryParameter;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
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
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

class PolarOrbitingMatchupStrategy extends AbstractMatchupStrategy{

    private final Logger logger;

    PolarOrbitingMatchupStrategy(Logger logger) {
        this.logger = logger;
    }

    MatchupCollection createMatchupCollection(ToolContext context) throws SQLException, IOException, InvalidRangeException {
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

        final List<SatelliteObservation> primaryObservations = getPrimaryObservations(context);
        for (final SatelliteObservation primaryObservation : primaryObservations) {
            try (final Reader primaryReader = readerFactory.getReader(primaryObservation.getSensor().getName())) {
                primaryReader.open(primaryObservation.getDataFilePath().toFile());

                final Date searchTimeStart = TimeUtils.addSeconds(-timeDeltaSeconds, primaryObservation.getStartTime());
                final Date searchTimeEnd = TimeUtils.addSeconds(timeDeltaSeconds, primaryObservation.getStopTime());

                // @todo 2 tb/tb extract method
                final Geometry primaryGeoBounds = primaryObservation.getGeoBounds();
                final boolean isPrimarySegmented = PolarOrbitingMatchupStrategy.isSegmented(primaryGeoBounds);

                final List<SatelliteObservation> secondaryObservations = getSecondaryObservations(context, searchTimeStart, searchTimeEnd);
                for (final SatelliteObservation secondaryObservation : secondaryObservations) {
                    try (Reader secondaryReader = readerFactory.getReader(secondaryObservation.getSensor().getName())) {
                        secondaryReader.open(secondaryObservation.getDataFilePath().toFile());

                        final Intersection[] intersectingIntervals = IntersectionEngine.getIntersectingIntervals(primaryObservation, secondaryObservation);
                        if (intersectingIntervals.length == 0) {
                            continue;
                        }

                        final MatchupSet matchupSet = new MatchupSet();
                        matchupSet.setPrimaryObservationPath(primaryObservation.getDataFilePath());
                        matchupSet.setSecondaryObservationPath(secondaryObservation.getDataFilePath());

                        // @todo 2 tb/tb extract method
                        final Geometry secondaryGeoBounds = secondaryObservation.getGeoBounds();
                        final boolean isSecondarySegmented = PolarOrbitingMatchupStrategy.isSegmented(secondaryGeoBounds);

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
                                final List<SampleSet> completeSamples = sampleCollector.addSecondarySamples(matchupSet.getSampleSets(), secondaryReader.getTimeLocator());
                                matchupSet.setSampleSets(completeSamples);

                                if (matchupSet.getNumObservations() > 0) {
                                    final Dimension primarySize = primaryReader.getProductSize();
                                    conditionEngineContext.setPrimarySize(primarySize);
                                    final Dimension secondarySize = secondaryReader.getProductSize();
                                    conditionEngineContext.setSecondarySize(secondarySize);

                                    logger.info("Found " + matchupSet.getNumObservations() + " matchup pixels");
                                    conditionEngine.process(matchupSet, conditionEngineContext);
                                    logger.info("Remaining " + matchupSet.getNumObservations() + " after condition processing");

                                    screeningEngine.process(matchupSet, primaryReader, secondaryReader);
                                    logger.info("Remaining " + matchupSet.getNumObservations() + " after matchup screening");

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

    // package access for testing only tb 2016-02-23
    static QueryParameter getPrimarySensorParameter(ToolContext context) {
        final QueryParameter parameter = new QueryParameter();
        final Sensor primarySensor = context.getUseCaseConfig().getPrimarySensor();
        if (primarySensor == null) {
            throw new RuntimeException("primary sensor not present in configuration file");
        }

        PolarOrbitingMatchupStrategy.assignSensor(parameter, primarySensor);
        parameter.setStartTime(context.getStartDate());
        parameter.setStopTime(context.getEndDate());
        return parameter;
    }

    // package access for testing only tb 2016-03-14
    static QueryParameter getSecondarySensorParameter(UseCaseConfig useCaseConfig, Date searchTimeStart, Date searchTimeEnd) {
        final QueryParameter parameter = new QueryParameter();
        final Sensor secondarySensor = PolarOrbitingMatchupStrategy.getSecondarySensor(useCaseConfig);
        PolarOrbitingMatchupStrategy.assignSensor(parameter, secondarySensor);
        parameter.setStartTime(searchTimeStart);
        parameter.setStopTime(searchTimeEnd);
        return parameter;
    }

    // package access for testing only tb 2016-03-14
    static Sensor getSecondarySensor(UseCaseConfig useCaseConfig) {
        final List<Sensor> additionalSensors = useCaseConfig.getAdditionalSensors();
        if (additionalSensors.size() != 1) {
            throw new RuntimeException("Unable to run matchup with given sensor number");
        }

        return additionalSensors.get(0);
    }

    // package access for testing only tb 2016-11-04
    static void assignSensor(QueryParameter parameter, Sensor sensor) {
        parameter.setSensorName(sensor.getName());
        final String dataVersion = sensor.getDataVersion();
        if (StringUtils.isNotNullAndNotEmpty(dataVersion)) {
            parameter.setVersion(dataVersion);
        }
    }

    // package access for testing only tb 2016-11-04
    static boolean isSegmented(Geometry primaryGeoBounds) {
        return primaryGeoBounds instanceof GeometryCollection && ((GeometryCollection) primaryGeoBounds).getGeometries().length > 1;
    }

    // package access for testing only tb 2016-11-04
    static PixelLocator getPixelLocator(Reader reader, boolean isSegmented, Polygon polygon) throws IOException {
        final PixelLocator pixelLocator;
        if (isSegmented) {
            pixelLocator = reader.getSubScenePixelLocator(polygon);
        } else {
            pixelLocator = reader.getPixelLocator();
        }
        return pixelLocator;
    }

    private List<SatelliteObservation> getPrimaryObservations(ToolContext context) throws SQLException {
        final QueryParameter parameter = PolarOrbitingMatchupStrategy.getPrimarySensorParameter(context);
        logger.info("Requesting primary data ... (" + parameter.getSensorName() + ", " + parameter.getStartTime() + ", " + parameter.getStopTime());

        final Storage storage = context.getStorage();
        final List<SatelliteObservation> primaryObservations = storage.get(parameter);

        logger.info("Received " + primaryObservations.size() + " primary satellite observations");

        return primaryObservations;
    }

    private List<SatelliteObservation> getSecondaryObservations(ToolContext context, Date searchTimeStart, Date searchTimeEnd) throws SQLException {
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
        final QueryParameter parameter = PolarOrbitingMatchupStrategy.getSecondarySensorParameter(useCaseConfig, searchTimeStart, searchTimeEnd);
        logger.info("Requesting secondary data ... (" + parameter.getSensorName() + ", " + parameter.getStartTime() + ", " + parameter.getStopTime());

        final Storage storage = context.getStorage();
        final List<SatelliteObservation> secondaryObservations = storage.get(parameter);

        logger.info("Received " + secondaryObservations.size() + " secondary satellite observations");

        return secondaryObservations;
    }
}
