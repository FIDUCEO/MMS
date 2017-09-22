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

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.Sample;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.ObservationsSet;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.matchup.condition.ConditionEngine;
import com.bc.fiduceo.matchup.condition.ConditionEngineContext;
import com.bc.fiduceo.matchup.screening.ScreeningEngine;
import com.bc.fiduceo.math.TimeInterval;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderCache;
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

    InsituPolarOrbitingMatchupStrategy(Logger logger) {
        super(logger);
    }

    @Override
    public MatchupCollection createMatchupCollection(ToolContext context) throws SQLException, IOException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();

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
            return new MatchupCollection();
        }

        final ObservationsSet secondaryObservationsSet = retrieveSecondaryObservations(context, timeDeltaSeconds);
        final String[] secSensorNames = secondaryObservationsSet.getSensorKeys();

        final Map<String, Map<Path, List<MatchupSet>>> mapMatchupSetsInsituOrder = new HashMap<>();
        final Map<String, Map<Path, List<MatchupSet>>> mapMatchupSetsSatelliteOrder = new HashMap<>();
        for (String secSensorName : secSensorNames) {
            mapMatchupSetsInsituOrder.put(secSensorName, new HashMap<>());
            mapMatchupSetsSatelliteOrder.put(secSensorName, new HashMap<>());
        }

        final String primarySensorName = useCaseConfig.getPrimarySensor().getName();
        for (final SatelliteObservation insituObservation : insituObservations) {
            final Path insituPath = insituObservation.getDataFilePath();
            try (final Reader insituReader = readerFactory.getReader(primarySensorName)) {
                insituReader.open(insituPath.toFile());

                for (String secSensorName : secSensorNames) {
                    final List<SatelliteObservation> secondaryObservations = secondaryObservationsSet.get(secSensorName);
                    final Map<Path, List<MatchupSet>> matchupSetsInsituOrder = mapMatchupSetsInsituOrder.get(secSensorName);
                    final Map<Path, List<MatchupSet>> matchupSetsSatelliteOrder = mapMatchupSetsSatelliteOrder.get(secSensorName);

                    final List<MatchupSet> matchupSets = getInsituSamplesPerSatellite(geometryFactory, timeDeltaInMillis, processingInterval, secondaryObservations, insituReader);
                    for (final MatchupSet matchupSet : matchupSets) {
                        matchupSet.setPrimaryObservationPath(insituPath);
                        matchupSet.setPrimaryProcessingVersion(insituObservation.getVersion());
                        final Path secPath = matchupSet.getSecondaryObservationPath(secSensorName);

                        final List<MatchupSet> satelliteSets = getMatchupSets(secPath, matchupSetsSatelliteOrder);
                        satelliteSets.add(matchupSet);

                        final List<MatchupSet> insituSets = getMatchupSets(insituPath, matchupSetsInsituOrder);
                        insituSets.add(matchupSet);
                    }
                }
            }
        }

        for (String secSensorName : secSensorNames) {
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
                        final List<SampleSet> completeSamples = sampleCollector.addSecondarySamples(matchupSet.getSampleSets(), timeLocator, secSensorName);
                        matchupSet.setSampleSets(completeSamples);
                    }
                }
            }
        }

        final int numSecSensors = secSensorNames.length;

        final CombineBean combineBean = new CombineBean();
        combineBean.primarySensorName = primarySensorName;
        combineBean.secSensorNames = secSensorNames;
        combineBean.paths = new Path[numSecSensors + 1];
        combineBean.versions = new String[numSecSensors + 1];
        combineBean.samples = new Sample[numSecSensors + 1];
        combineBean.matchupCollection = new MatchupCollection();
        combineBean.mapMatchupSetsSatelliteOrder = mapMatchupSetsSatelliteOrder;
        combineBean.mapMatchupSetsInsituOrder = mapMatchupSetsInsituOrder;

        combineMatchups(0, combineBean);

        final int readerCacheSize = context.getSystemConfig().getReaderCacheSize();
        final ReaderCache readerCache = new ReaderCache(readerCacheSize, readerFactory, null);
        final List<MatchupSet> matchupSets = combineBean.matchupCollection.getSets();
        final ConditionEngineContext conditionEngineContext = ConditionEngine.createContext(context);
        for (MatchupSet matchupSet : matchupSets) {
            final Path primaryObservationPath = matchupSet.getPrimaryObservationPath();
            final Reader primaryReader = readerCache.getReaderFor(primarySensorName, primaryObservationPath, null);
            final HashMap<String, Reader> secondaryReaders = new HashMap<>();
            for (String secSensorName : secSensorNames) {
                final Path secondaryObservationPath = matchupSet.getSecondaryObservationPath(secSensorName);
                final Reader reader = readerCache.getReaderFor(secSensorName, secondaryObservationPath, null);
                secondaryReaders.put(secSensorName, reader);
            }
            applyConditionsAndScreenings(matchupSet, conditionEngine, conditionEngineContext, screeningEngine, primaryReader, secondaryReaders);
        }

        return combineBean.matchupCollection;
    }

    private List<MatchupSet> getMatchupSets(Path path, Map<Path, List<MatchupSet>> matchupSetsOrdered) {
        final List<MatchupSet> matchuprSets;
        if (matchupSetsOrdered.containsKey(path)) {
            matchuprSets = matchupSetsOrdered.get(path);
        } else {
            matchuprSets = new ArrayList<>();
            matchupSetsOrdered.put(path, matchuprSets);
        }
        return matchuprSets;
    }

    private ObservationsSet retrieveSecondaryObservations(ToolContext context, int timeDeltaSeconds) throws SQLException {
        final Date searchTimeStart = TimeUtils.addSeconds(-timeDeltaSeconds, context.getStartDate());
        final Date searchTimeEnd = TimeUtils.addSeconds(timeDeltaSeconds, context.getEndDate());
        return getSecondaryObservations(context, searchTimeStart, searchTimeEnd);
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

    static List<SatelliteObservation> getCandidatesByGeometry(List<SatelliteObservation> satelliteObservations, Geometry geometry) {
        final List<SatelliteObservation> candidateList = new ArrayList<>();
        for (final SatelliteObservation observation : satelliteObservations) {
            final Geometry geoBounds = observation.getGeoBounds();
            if (!geoBounds.getIntersection(geometry).isEmpty()) {
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

    static void combineMatchups(int depth, CombineBean bean) {
        final int secIdx = depth + 1;
        if (secIdx == bean.samples.length) {
            final SampleSet sampleSet = createValidSampleSet(bean.samples, bean.secSensorNames);
            if (sampleSet != null) {
                final MatchupSet validMatchupSet = getValidMatchupSet(bean.currentMatchupSet, bean.paths, bean.versions, bean.secSensorNames, bean.matchupCollection);
                validMatchupSet.getSampleSets().add(sampleSet);
                bean.currentMatchupSet = validMatchupSet;
            }
            return;
        }
        final String secSensorName = bean.secSensorNames[depth];
        if (depth == 0) {
            final Map<Path, List<MatchupSet>> matchupSetsSatelliteOrder = bean.mapMatchupSetsSatelliteOrder.get(secSensorName);
            for (List<MatchupSet> matchupSets : matchupSetsSatelliteOrder.values()) {
                combineMatchupSets(depth, bean, matchupSets);
            }
        } else {
            final Map<Path, List<MatchupSet>> matchupSetsInsituOrder = bean.mapMatchupSetsInsituOrder.get(secSensorName);
            final List<MatchupSet> matchupSets = matchupSetsInsituOrder.get(bean.paths[PRIM_IDX]);
            if (matchupSets != null) {
                combineMatchupSets(depth, bean, matchupSets);
            }
        }
        if (depth == 0 && bean.currentMatchupSet != null) {
            bean.matchupCollection.add(bean.currentMatchupSet);
        }
    }

    static void combineMatchupSets(int depth, CombineBean bean, List<MatchupSet> matchupSets) {
        final String secSensorName = bean.secSensorNames[depth];
        int secIdx = depth + 1;
        for (MatchupSet matchupSet : matchupSets) {
            if (depth == 0) {
                bean.paths[PRIM_IDX] = matchupSet.getPrimaryObservationPath();
                bean.versions[PRIM_IDX] = matchupSet.getPrimaryProcessingVersion();
            }
            bean.paths[secIdx] = matchupSet.getSecondaryObservationPath(secSensorName);
            bean.versions[secIdx] = matchupSet.getSecondaryProcessingVersion(secSensorName);
            final List<SampleSet> sampleSets = matchupSet.getSampleSets();
            for (SampleSet sampleSet : sampleSets) {
                final Sample primary = sampleSet.getPrimary();
                if (depth == 0) {
                    bean.samples[PRIM_IDX] = primary;
                } else if (primary.getTime() != bean.samples[PRIM_IDX].getTime()) {
                    continue;
                }

                bean.samples[secIdx] = sampleSet.getSecondary(secSensorName);
                combineMatchups(depth + 1, bean);
                bean.samples[secIdx] = null;
            }
            bean.paths[secIdx] = null;
            bean.versions[secIdx] = null;
        }
    }

    private List<MatchupSet> getInsituSamplesPerSatellite(GeometryFactory geometryFactory, long timeDeltaInMillis, TimeInterval processingInterval,
                                                          List<SatelliteObservation> secondaryObservations, Reader insituReader) throws IOException, InvalidRangeException {
        final HashMap<String, MatchupSet> observationsPerProduct = new HashMap<>();

        final List<Sample> insituSamples = getInsituSamples(processingInterval, insituReader);
        for (final Sample insituSample : insituSamples) {
            final List<SatelliteObservation> candidatesByTime = getCandidatesByTime(secondaryObservations, new Date(insituSample.getTime()), timeDeltaInMillis);
            final Geometry point = geometryFactory.createPoint(insituSample.getLon(), insituSample.getLat());
            final List<SatelliteObservation> candidatesByGeometry = getCandidatesByGeometry(candidatesByTime, point);

            for (SatelliteObservation candidate : candidatesByGeometry) {
                final String secSensorName = candidate.getSensor().getName();
                final String productName = candidate.getDataFilePath().getFileName().toString();
                MatchupSet matchupSet = observationsPerProduct.get(productName);
                if (matchupSet == null) {
                    matchupSet = new MatchupSet();
                    matchupSet.setSecondaryObservationPath(secSensorName, candidate.getDataFilePath());
                    matchupSet.setSecondaryProcessingVersion(secSensorName, candidate.getVersion());

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

        final String longitudeVariableName = insituReader.getLongitudeVariableName();
        final String latitudeVariableName = insituReader.getLatitudeVariableName();

        for (int i = 0; i < height; i++) {
            final ArrayInt.D2 acquisitionTimeArray = insituReader.readAcquisitionTime(0, i, singlePixel);
            final int acquisitionTime = acquisitionTimeArray.getInt(0);
            final Date acquisitionDate = TimeUtils.create(acquisitionTime * 1000L);
            if (processingInterval.contains(acquisitionDate)) {
                final Array lon = insituReader.readRaw(0, i, singlePixel, longitudeVariableName);
                final Array lat = insituReader.readRaw(0, i, singlePixel, latitudeVariableName);

                final Sample sample = new Sample(0, i, lon.getDouble(0), lat.getDouble(0), acquisitionDate.getTime());
                insituSamples.add(sample);
            }
        }

        return insituSamples;
    }

    static class CombineBean {

        public String primarySensorName;
        public String[] secSensorNames;
        public Map<String, Map<Path, List<MatchupSet>>> mapMatchupSetsSatelliteOrder;
        public Map<String, Map<Path, List<MatchupSet>>> mapMatchupSetsInsituOrder;
        private Path[] paths;
        private String[] versions;
        private Sample[] samples;
        private MatchupSet currentMatchupSet;
        private MatchupCollection matchupCollection;
    }
}
