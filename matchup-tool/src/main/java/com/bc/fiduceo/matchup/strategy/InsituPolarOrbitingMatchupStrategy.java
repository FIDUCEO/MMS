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

import com.bc.fiduceo.core.*;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.matchup.condition.ConditionEngine;
import com.bc.fiduceo.matchup.condition.ConditionEngineContext;
import com.bc.fiduceo.matchup.screening.ScreeningEngine;
import com.bc.fiduceo.math.TimeInterval;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.tool.ToolContext;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

class InsituPolarOrbitingMatchupStrategy extends AbstractMatchupStrategy {

    private static final Interval singlePixel = new Interval(1, 1);
    private static final int PRIM_IDX = 0;

    private String[] secSensorNames;
    private Map<String, Map<Path, List<MatchupSet>>> mapMatchupSetsSatelliteOrder;
    private Path[] paths;
    private String[] versions;
    private Sample[] samples;
    private MatchupSet currentMatchupSet;
    private MatchupCollection matchupCollection;

    InsituPolarOrbitingMatchupStrategy(Logger logger) {
        super(logger);
    }

    @Override
    public MatchupCollection createMatchupCollection(ToolContext context) throws SQLException, IOException, InvalidRangeException {
        matchupCollection = new MatchupCollection();
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();

        final ConditionEngineContext conditionEngineContext = ConditionEngine.createContext(context);
        final ConditionEngine conditionEngine = new ConditionEngine();
        conditionEngine.configure(useCaseConfig);

        final ScreeningEngine screeningEngine = new ScreeningEngine(context);

        final GeometryFactory geometryFactory = context.getGeometryFactory();
        final ReaderFactory readerFactory = ReaderFactory.get(geometryFactory);

        final long timeDeltaInMillis = conditionEngine.getMaxTimeDeltaInMillis();
        final int timeDeltaSeconds = (int) (timeDeltaInMillis / 1000);
        final TimeInterval processingInterval = new TimeInterval(context.getStartDate(), context.getEndDate());

        // @todo 2 se/?? ... renaming of class SatelliteObservation ... 2017-05-15
        // see Trello https://trello.com/c/RvivyMUF
        final List<SatelliteObservation> insituObservations = getPrimaryObservations(context);
        if (insituObservations.size() == 0) {
            logger.warning("No insitu data in time interval:" + context.getStartDate() + " - " + context.getEndDate());
            return matchupCollection;
        }

        final Date searchTimeStart = TimeUtils.addSeconds(-timeDeltaSeconds, context.getStartDate());
        final Date searchTimeEnd = TimeUtils.addSeconds(timeDeltaSeconds, context.getEndDate());
        final Map<String, List<SatelliteObservation>> mapSecondaryObservations = getSecondaryObservations(context, searchTimeStart, searchTimeEnd);
        secSensorNames = mapSecondaryObservations.keySet().toArray(new String[]{});

        final Map<String, Map<Path, List<MatchupSet>>> mapMatchupSetsInsituOrder = new HashMap<>();
        mapMatchupSetsSatelliteOrder = new HashMap<>();
        for (String secSensorName : secSensorNames) {
            mapMatchupSetsInsituOrder.put(secSensorName, new HashMap<>());
            mapMatchupSetsSatelliteOrder.put(secSensorName, new HashMap<>());
        }
        final Map<Path, String> insituProduktSensorName = new HashMap<>();

        for (final SatelliteObservation insituObservation : insituObservations) {
            final String sensorName = insituObservation.getSensor().getName();
            final Path insituPath = insituObservation.getDataFilePath();
            insituProduktSensorName.put(insituPath, sensorName);
            try (final Reader insituReader = readerFactory.getReader(sensorName)) {
                insituReader.open(insituPath.toFile());

                for (String secSensorName : secSensorNames) {
                    final List<SatelliteObservation> secondaryObservations = mapSecondaryObservations.get(secSensorName);
                    final Map<Path, List<MatchupSet>> matchupSetsInsituOrder = mapMatchupSetsInsituOrder.get(secSensorName);
                    final Map<Path, List<MatchupSet>> matchupSetsSatelliteOrder = mapMatchupSetsSatelliteOrder.get(secSensorName);

                    final List<MatchupSet> matchupSets = getInsituSamplesPerSatellite(geometryFactory, timeDeltaInMillis, processingInterval, secondaryObservations, insituReader);
                    for (final MatchupSet matchupSet : matchupSets) {
                        matchupSet.setPrimaryObservationPath(insituPath);
                        matchupSet.setPrimaryProcessingVersion(insituObservation.getVersion());
                        final Path secPath = matchupSet.getSecondaryObservationPath(SampleSet.ONLY_ONE_SECONDARY);

                        final List<MatchupSet> satelliteSets;
                        if (matchupSetsSatelliteOrder.containsKey(secPath)) {
                            satelliteSets = matchupSetsSatelliteOrder.get(secPath);
                        } else {
                            satelliteSets = new ArrayList<>();
                            matchupSetsSatelliteOrder.put(secPath, satelliteSets);
                        }

                        satelliteSets.add(matchupSet);

                        final List<MatchupSet> insituSets;
                        if (matchupSetsInsituOrder.containsKey(insituPath)) {
                            insituSets = matchupSetsInsituOrder.get(insituPath);
                        } else {
                            insituSets = new ArrayList<>();
                            matchupSetsInsituOrder.put(insituPath, insituSets);
                        }

                        insituSets.add(matchupSet);
                    }
                }
            }
        }

        for (String secSensorName : secSensorNames) {
            final Map<Path, List<MatchupSet>> matchupSetsInsituOrder = mapMatchupSetsInsituOrder.get(secSensorName);
            final Map<Path, List<MatchupSet>> matchupSetsSatelliteOrder = mapMatchupSetsSatelliteOrder.get(secSensorName);
            for (Map.Entry<Path, List<MatchupSet>> pathListEntry : matchupSetsSatelliteOrder.entrySet()) {
                final Path secondaryPath = pathListEntry.getKey();
                final List<MatchupSet> matchupSets = pathListEntry.getValue();

                try (Reader secondaryReader = readerFactory.getReader(secSensorName)) {
                    secondaryReader.open(secondaryPath.toFile());

                    final PixelLocator pixelLocator = secondaryReader.getPixelLocator();
                    final TimeLocator timeLocator = secondaryReader.getTimeLocator();

                    final SampleCollector sampleCollector = new SampleCollector(context, pixelLocator);

                    for (MatchupSet matchupSet : matchupSets) {
                        final Path insituPath = matchupSet.getPrimaryObservationPath();
                        final String sensorName = insituProduktSensorName.get(insituPath);
                        final List<SampleSet> completeSamples = sampleCollector.addSecondarySamples(matchupSet.getSampleSets(), timeLocator);
                        matchupSet.setSampleSets(completeSamples);

                        try (final Reader insituReader = readerFactory.getReader(sensorName)) {
                            insituReader.open(insituPath.toFile());
                            if (matchupSet.getNumObservations() > 0) {
                                applyConditionsAndScreenings(matchupSet, conditionEngine, conditionEngineContext, screeningEngine, insituReader, secondaryReader);
                                if (matchupSet.getNumObservations() == 0) {
                                    matchupSets.remove(matchupSet);
                                    matchupSetsInsituOrder.get(insituPath).remove(matchupSet);
                                } else {
                                    matchupCollection.add(matchupSet);
                                }
                            }
                        }
                    }
                }
            }
        }

        final int numSecSensors = secSensorNames.length;

        if (numSecSensors <= 1) {
            return matchupCollection;
        }

        matchupCollection = new MatchupCollection();

        paths = new Path[numSecSensors + 1];
        versions = new String[numSecSensors + 1];
        samples = new Sample[numSecSensors + 1];

        combineMatchups(0);

        return matchupCollection;
    }

    static List<SatelliteObservation> getCandidatesByTime(List<SatelliteObservation> satelliteObservations, Date insituTime, long timeDeltaInMillis) {
        final List<SatelliteObservation> candidateList = new ArrayList<>();
        for (final SatelliteObservation observation : satelliteObservations) {
            final Date startTime = new Date(observation.getStartTime().getTime() - timeDeltaInMillis);
            final Date stopTime = new Date(observation.getStopTime().getTime() + timeDeltaInMillis);
            final TimeInterval observationInterval = new TimeInterval(startTime, stopTime);
            if (observationInterval.contains(insituTime)) {
                candidateList.add(observation);
            }
        }
        return candidateList;
    }

    static List<SatelliteObservation> getCandidatesByGeometry(List<SatelliteObservation> satelliteObservations, Point point) {
        final List<SatelliteObservation> candidateList = new ArrayList<>();
        for (final SatelliteObservation observation : satelliteObservations) {
            final Geometry geoBounds = observation.getGeoBounds();
            if (!geoBounds.getIntersection(point).isEmpty()) {
                candidateList.add(observation);
            }
        }
        return candidateList;
    }

    static SampleSet createValidSampleSet(Sample[] samples, String[] secSensorNames) {
        for (Sample sample : samples) {
            if (sample == null) {
                return null;
            }
        }
        final SampleSet sampleSet = new SampleSet();
        for (int i = 0; i < samples.length; i++) {
            if (i == PRIM_IDX) {
                sampleSet.setPrimary(samples[i]);
            } else {
                sampleSet.setSecondary(secSensorNames[i - 1], samples[i]);
            }
        }
        return sampleSet;
    }

    static MatchupSet getValidMatchupSet(MatchupSet currentMatchupSet, Path[] paths, String[] versions, String[] secSensorNames, MatchupCollection matchupCollection) {
        if (!matchupsetIsValid(currentMatchupSet, paths, secSensorNames)) {
            if (currentMatchupSet != null
                && currentMatchupSet.getNumObservations() > 0) {
                matchupCollection.add(currentMatchupSet);
            }
            currentMatchupSet = new MatchupSet();
            for (int i = 0; i < paths.length; i++) {
                final Path path = paths[i];
                final String version = versions[i];
                if (i == PRIM_IDX) {
                    currentMatchupSet.setPrimaryObservationPath(path);
                    currentMatchupSet.setPrimaryProcessingVersion(version);
                } else {
                    final String secSensorName = secSensorNames[i - 1];
                    currentMatchupSet.setSecondaryObservationPath(secSensorName, path);
                    currentMatchupSet.setSecondaryProcessingVersion(secSensorName, version);
                }
            }
        }
        return currentMatchupSet;
    }

    static boolean matchupsetIsValid(MatchupSet currentMatchupSet, Path[] paths, String[] secSensorNames) {
        if (currentMatchupSet == null) {
            return false;
        }

        for (int i = 0; i < paths.length; i++) {
            Path path = paths[i];
            if (i == PRIM_IDX) {
                if (!path.equals(currentMatchupSet.getPrimaryObservationPath())) {
                    return false;
                }
            } else if (!path.equals(currentMatchupSet.getSecondaryObservationPath(secSensorNames[i - 1]))) {
                return false;
            }
        }
        return true;
    }

    private void combineMatchups(int depth) {
        final int secIdx = depth + 1;
        if (secIdx >= samples.length) {
            final SampleSet sampleSet = createValidSampleSet(samples, secSensorNames);
            if (sampleSet != null) {
                final MatchupSet validMatchupSet = getValidMatchupSet(currentMatchupSet, paths, versions, secSensorNames, matchupCollection);
                validMatchupSet.getSampleSets().add(sampleSet);
            }
            return;
        }
        final String secSensorName = secSensorNames[secIdx - 1];
        final Map<Path, List<MatchupSet>> matchupSetsSatelliteOrder = mapMatchupSetsSatelliteOrder.get(secSensorName);
        for (List<MatchupSet> matchupSets : matchupSetsSatelliteOrder.values()) {
            for (MatchupSet matchupSet : matchupSets) {
                final Path primaryPath = matchupSet.getPrimaryObservationPath();
                if (secIdx == 1) {
                    paths[PRIM_IDX] = primaryPath;
                    versions[PRIM_IDX] = matchupSet.getPrimaryProcessingVersion();
                } else if (!primaryPath.equals(paths[PRIM_IDX])) {
                    continue;
                }
                paths[secIdx] = matchupSet.getSecondaryObservationPath(SampleSet.ONLY_ONE_SECONDARY);
                versions[secIdx] = matchupSet.getSecondaryProcessingVersion(SampleSet.ONLY_ONE_SECONDARY);
                final List<SampleSet> sampleSets = matchupSet.getSampleSets();
                for (SampleSet sampleSet : sampleSets) {
                    final Sample primary = sampleSet.getPrimary();
                    if (secIdx == 1) {
                        samples[PRIM_IDX] = primary;
                    } else if (primary.time != samples[PRIM_IDX].time) {
                        continue;
                    }

                    samples[secIdx] = sampleSet.getSecondary(SampleSet.ONLY_ONE_SECONDARY);
                    combineMatchups(depth + 1);
                    samples[secIdx] = null;
                }
                paths[secIdx] = null;
                versions[secIdx] = null;
            }
        }
    }

    private List<MatchupSet> getInsituSamplesPerSatellite(GeometryFactory geometryFactory, long timeDeltaInMillis, TimeInterval processingInterval,
                                                          List<SatelliteObservation> secondaryObservations, Reader insituReader) throws IOException, InvalidRangeException {
        final HashMap<String, MatchupSet> observationsPerProduct = new HashMap<>();

        final List<Sample> insituSamples = getInsituSamples(processingInterval, insituReader);
        for (final Sample insituSample : insituSamples) {
            final List<SatelliteObservation> candidatesByTime = getCandidatesByTime(secondaryObservations, new Date(insituSample.time), timeDeltaInMillis);
            final List<SatelliteObservation> candidatesByGeometry = getCandidatesByGeometry(candidatesByTime, geometryFactory.createPoint(insituSample.lon, insituSample.lat));

            for (SatelliteObservation candidate : candidatesByGeometry) {
                final String productName = candidate.getDataFilePath().getFileName().toString();
                MatchupSet matchupSet = observationsPerProduct.get(productName);
                if (matchupSet == null) {
                    matchupSet = new MatchupSet();
                    matchupSet.setSecondaryObservationPath(SampleSet.ONLY_ONE_SECONDARY, candidate.getDataFilePath());
                    matchupSet.setSecondaryProcessingVersion(SampleSet.ONLY_ONE_SECONDARY, candidate.getVersion());

                    observationsPerProduct.put(productName, matchupSet);
                }
                matchupSet.addPrimary(insituSample);
            }
        }
        return new ArrayList<>(observationsPerProduct.values());
    }

    private List<Sample> getInsituSamples(TimeInterval processingInterval, Reader insituReader) throws IOException, InvalidRangeException {
        final List<Sample> insituSamples = new ArrayList<>();
        final Dimension productSize = insituReader.getProductSize();
        final int height = productSize.getNy();
        for (int i = 0; i < height; i++) {
            final ArrayInt.D2 acquisitionTimeArray = insituReader.readAcquisitionTime(0, i, singlePixel);
            final int acquisitionTime = acquisitionTimeArray.getInt(0);
            final Date acquisitionDate = TimeUtils.create(acquisitionTime * 1000L);
            if (processingInterval.contains(acquisitionDate)) {
                // @todo 3 tb/tb this is SST-CCI specific - generalise the geolocation access 2016-11-07
                final Array lon = insituReader.readRaw(0, i, singlePixel, "insitu.lon");
                final Array lat = insituReader.readRaw(0, i, singlePixel, "insitu.lat");

                final Sample sample = new Sample(0, i, lon.getDouble(0), lat.getDouble(0), acquisitionDate.getTime());
                insituSamples.add(sample);
            }
        }

        return insituSamples;
    }
}
