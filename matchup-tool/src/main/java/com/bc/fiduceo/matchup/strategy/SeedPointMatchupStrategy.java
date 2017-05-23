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
package com.bc.fiduceo.matchup.strategy;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.SamplingPoint;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.matchup.condition.ConditionEngine;
import com.bc.fiduceo.matchup.condition.ConditionEngineContext;
import com.bc.fiduceo.matchup.screening.ScreeningEngine;
import com.bc.fiduceo.math.Intersection;
import com.bc.fiduceo.math.IntersectionEngine;
import com.bc.fiduceo.math.TimeInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.tool.ToolContext;
import com.bc.fiduceo.util.SobolSamplingPointGenerator;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.InvalidRangeException;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class SeedPointMatchupStrategy extends AbstractMatchupStrategy {

    SeedPointMatchupStrategy(Logger logger) {
        super(logger);
    }

    @Override
    public MatchupCollection createMatchupCollection(ToolContext context) throws SQLException, IOException, InvalidRangeException {
        final MatchupCollection matchupCollection = new MatchupCollection();

        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();

        final int numRandomSeedPoints = useCaseConfig.getNumRandomSeedPoints();
        if (numRandomSeedPoints < 1) {
            throw new RuntimeException("Number of random seed points greater than zero expected.");
        }

        final ConditionEngine conditionEngine = new ConditionEngine();
        final ConditionEngineContext conditionEngineContext = ConditionEngine.createContext(context);
        conditionEngine.configure(useCaseConfig);

        final ScreeningEngine screeningEngine = new ScreeningEngine(context);

        final GeometryFactory geometryFactory = context.getGeometryFactory();
        final ReaderFactory readerFactory = ReaderFactory.get(geometryFactory);

        final long timeDeltaInMillis = conditionEngine.getMaxTimeDeltaInMillis();
        final int timeDeltaSeconds = (int) (timeDeltaInMillis / 1000);

        final SobolSamplingPointGenerator sobolSamplingPointGenerator = new SobolSamplingPointGenerator(true);
        final long contextStart = context.getStartDate().getTime();
        final long contextEnd = context.getEndDate().getTime();
        final List<SamplingPoint> seedPoints = sobolSamplingPointGenerator.createSamples(numRandomSeedPoints, 0, contextStart, contextEnd);

        final List<SatelliteObservation> primaryObservations = getPrimaryObservations(context);
        for (final SatelliteObservation primaryObservation : primaryObservations) {
            try (final Reader primaryReader = readerFactory.getReader(primaryObservation.getSensor().getName())) {
                final Date primaryStartTime = primaryObservation.getStartTime();
                final Date primaryStopTime = primaryObservation.getStopTime();

                // @todo 2 tb/tb extract method
                final Geometry primaryGeoBounds = primaryObservation.getGeoBounds();
                final boolean isPrimarySegmented = AbstractMatchupStrategy.isSegmented(primaryGeoBounds);
                final Geometry[] primaryGeometries;
                if (isPrimarySegmented) {
                    primaryGeometries = ((GeometryCollection) primaryGeoBounds).getGeometries();
                } else {
                    primaryGeometries = new Geometry[]{primaryGeoBounds};
                }

                final List<SamplingPoint> primarySeedPoints = getPrimarySeedPoints(geometryFactory, seedPoints, primaryStartTime, primaryStopTime, primaryGeometries);

                final Path primaryObservationDataFilePath = primaryObservation.getDataFilePath();
                primaryReader.open(primaryObservationDataFilePath.toFile());

//                following lines are for test purposes only
//                >>>>>  start  <<<<<
//                final Dimension productSize = primaryReader.getProductSize();
//                final int numScanlines = productSize.getNy();
//                final int numPoints = primarySeedPoints.size();
//                System.out.println("Num Scanlines: " + numScanlines + "   num seed points: " + numPoints);
//                final int equation = (int) (1.0 * numPoints / numScanlines * 2280);
//                System.out.println("Equates to " + equation + " seed points at 2280 scanlines per MHS orbit");
//                >>>>>  e n d  <<<<<

                final Date searchTimeStart = TimeUtils.addSeconds(-timeDeltaSeconds, primaryStartTime);
                final Date searchTimeEnd = TimeUtils.addSeconds(timeDeltaSeconds, primaryStopTime);
                final Map<String, List<SatelliteObservation>> mapSecondaryObservations = getSecondaryObservations(context, searchTimeStart, searchTimeEnd);

                final MatchupSet primaryMatchups = getPrimaryMatchupSet(primaryReader, primarySeedPoints, primaryObservationDataFilePath);
                if (primaryMatchups == null) {
                    continue;
                }

                // todo se multisensor
                final String secondarySensorName_CaseOneSecondary = useCaseConfig.getSecondarySensors().get(0).getName();
                final List<SatelliteObservation> secondaryObservations = mapSecondaryObservations.get(secondarySensorName_CaseOneSecondary);

                for (final SatelliteObservation secondaryObservation : secondaryObservations) {
                    try (Reader secondaryReader = readerFactory.getReader(secondaryObservation.getSensor().getName())) {
                        secondaryReader.open(secondaryObservation.getDataFilePath().toFile());

                        final Intersection[] intersectingIntervals = IntersectionEngine.getIntersectingIntervals(primaryObservation, secondaryObservation);
                        if (intersectingIntervals.length == 0) {
                            continue;
                        }

                        final MatchupSet matchupSet = new MatchupSet();
                        matchupSet.setPrimaryObservationPath(primaryObservationDataFilePath);
                        matchupSet.setPrimaryProcessingVersion(primaryObservation.getVersion());
                        matchupSet.setSecondaryObservationPath(SampleSet.ONLY_ONE_SECONDARY, secondaryObservation.getDataFilePath());
                        matchupSet.setSecondaryProcessingVersion(SampleSet.ONLY_ONE_SECONDARY, secondaryObservation.getVersion());

                        // @todo 2 tb/tb extract method
                        final Geometry secondaryGeoBounds = secondaryObservation.getGeoBounds();
                        final boolean isSecondarySegmented = AbstractMatchupStrategy.isSegmented(secondaryGeoBounds);

                        for (final Intersection intersection : intersectingIntervals) {
                            final TimeInfo timeInfo = intersection.getTimeInfo();
                            if (timeInfo.getMinimalTimeDelta() < timeDeltaInMillis) {
                                final PixelLocator secondaryPixelLocator = getPixelLocator(secondaryReader, isSecondarySegmented, (Polygon) intersection.getSecondaryGeometry());

                                if (secondaryPixelLocator == null) {
                                    logger.warning("Unable to create valid pixel locators. Skipping intersection segment.");
                                    continue;
                                }

                                SampleCollector sampleCollector = new SampleCollector(context, secondaryPixelLocator);
                                final List<SampleSet> completeSamples = sampleCollector.addSecondarySamples(primaryMatchups.getSampleSets(), secondaryReader.getTimeLocator());
                                matchupSet.setSampleSets(completeSamples);

                                if (matchupSet.getNumObservations() > 0) {
                                    applyConditionsAndScreenings(matchupSet, conditionEngine, conditionEngineContext, screeningEngine, primaryReader, secondaryReader);
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

    private MatchupSet getPrimaryMatchupSet(Reader primaryReader, List<SamplingPoint> primarySeedPoints, Path primaryObservationDataFilePath) throws IOException {
        final MatchupSet primaryMatchups = new MatchupSet();
        final PixelLocator primaryPixelLocator = primaryReader.getPixelLocator();
        if (primaryPixelLocator == null) {
            logger.warning("Unable to create valid pixel locators. Skipping primary observation '" + primaryObservationDataFilePath.toString() + "'.");
            return null;
        }

        final Dimension primProductSize = primaryReader.getProductSize();
        final TimeLocator primTimeLocator = primaryReader.getTimeLocator();
        for (SamplingPoint psp : primarySeedPoints) {
            final Point2D[] locations = primaryPixelLocator.getPixelLocation(psp.getLon(), psp.getLat());
            for (Point2D loc : locations) {
                final double x1 = loc.getX();
                final double y1 = loc.getY();
                if (x1 >= 0 && y1 >= 0
                    && x1 < primProductSize.getNx()
                    && y1 < primProductSize.getNy()) {
                    final int x = (int) Math.floor(x1);
                    final int y = (int) Math.floor(y1);
                    final Point2D geo = primaryPixelLocator.getGeoLocation(x + 0.5, y + 0.5, null);
                    primaryMatchups.addPrimary(new Sample(x, y, geo.getX(), geo.getY(), primTimeLocator.getTimeFor(x, y)));
                }
            }
        }
        return primaryMatchups;
    }

    private List<SamplingPoint> getPrimarySeedPoints(GeometryFactory geometryFactory, List<SamplingPoint> seedPoints, Date primaryStartTime, Date primaryStopTime, Geometry[] primaryGeometries) {
        final List<SamplingPoint> primaryPoints = new ArrayList<>();
        for (SamplingPoint seedPoint : seedPoints) {
            final long time = seedPoint.getTime();
            final double lat = seedPoint.getLat();
            final double lon = seedPoint.getLon();
            final Point point = geometryFactory.createPoint(lon, lat);
            if (time >= primaryStartTime.getTime() && time <= primaryStopTime.getTime()) {
                for (Geometry geometry : primaryGeometries) {
                    if (geometry.getIntersection(point) != null) {
                        primaryPoints.add(seedPoint);
                        break;
                    }
                }
            }
        }
        return primaryPoints;
    }
}
