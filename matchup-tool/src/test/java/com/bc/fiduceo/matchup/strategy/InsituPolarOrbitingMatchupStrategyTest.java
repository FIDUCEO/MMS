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

import static org.junit.Assert.*;

import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.core.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.junit.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InsituPolarOrbitingMatchupStrategyTest {

    private static GeometryFactory geometryFactory;

    @BeforeClass
    public static void beforeClass() {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
    }

    @Test
    public void testGetCandidatesByTime_emptyList() {
        final List<SatelliteObservation> satelliteObservations = new ArrayList<>();

        final List<SatelliteObservation> resultList = InsituPolarOrbitingMatchupStrategy.getCandidatesByTime(satelliteObservations, new Date(20000), 20);
        assertEquals(0, resultList.size());
    }

    @Test
    public void testGetCandidatesByTime_before() {
        final List<SatelliteObservation> satelliteObservations = new ArrayList<>();
        final SatelliteObservation observation = createSatelliteObservation(30000L, 40000L);
        satelliteObservations.add(observation);

        final List<SatelliteObservation> resultList = InsituPolarOrbitingMatchupStrategy.getCandidatesByTime(satelliteObservations, new Date(20000), 500);
        assertEquals(0, resultList.size());
    }

    @Test
    public void testGetCandidatesByTime_after() {
        final List<SatelliteObservation> satelliteObservations = new ArrayList<>();
        final SatelliteObservation observation = createSatelliteObservation(30000L, 40000L);
        satelliteObservations.add(observation);

        final List<SatelliteObservation> resultList = InsituPolarOrbitingMatchupStrategy.getCandidatesByTime(satelliteObservations, new Date(41000), 500);
        assertEquals(0, resultList.size());
    }

    @Test
    public void testGetCandidatesByTime_pickOne() {
        final List<SatelliteObservation> satelliteObservations = new ArrayList<>();
        satelliteObservations.add(createSatelliteObservation(30000L, 40000L));
        satelliteObservations.add(createSatelliteObservation(35000L, 45000L));
        satelliteObservations.add(createSatelliteObservation(37000L, 47000L));

        final List<SatelliteObservation> resultList = InsituPolarOrbitingMatchupStrategy.getCandidatesByTime(satelliteObservations, new Date(46000), 500);
        assertEquals(1, resultList.size());
        assertEquals(37000L, resultList.get(0).getStartTime().getTime());
    }

    @Test
    public void testGetCandidatesByTime_pickOne_useTimeDelta_after() {
        final List<SatelliteObservation> satelliteObservations = new ArrayList<>();
        satelliteObservations.add(createSatelliteObservation(30000L, 40000L));
        satelliteObservations.add(createSatelliteObservation(35000L, 45000L));
        satelliteObservations.add(createSatelliteObservation(37000L, 47000L));

        final List<SatelliteObservation> resultList = InsituPolarOrbitingMatchupStrategy.getCandidatesByTime(satelliteObservations, new Date(47500), 1000);
        assertEquals(1, resultList.size());
        assertEquals(37000L, resultList.get(0).getStartTime().getTime());
    }

    @Test
    public void testGetCandidatesByTime_pickOne_useTimeDelta_before() {
        final List<SatelliteObservation> satelliteObservations = new ArrayList<>();
        satelliteObservations.add(createSatelliteObservation(30000L, 40000L));
        satelliteObservations.add(createSatelliteObservation(35000L, 45000L));
        satelliteObservations.add(createSatelliteObservation(37000L, 47000L));

        final List<SatelliteObservation> resultList = InsituPolarOrbitingMatchupStrategy.getCandidatesByTime(satelliteObservations, new Date(30500), 1000);
        assertEquals(1, resultList.size());
        assertEquals(30000L, resultList.get(0).getStartTime().getTime());
    }

    @Test
    public void testGetCandidatesByTime_picktwo() {
        final List<SatelliteObservation> satelliteObservations = new ArrayList<>();
        satelliteObservations.add(createSatelliteObservation(40000L, 50000L));
        satelliteObservations.add(createSatelliteObservation(45000L, 55000L));
        satelliteObservations.add(createSatelliteObservation(47000L, 57000L));

        final List<SatelliteObservation> resultList = InsituPolarOrbitingMatchupStrategy.getCandidatesByTime(satelliteObservations, new Date(52000), 500);
        assertEquals(2, resultList.size());
        assertEquals(45000L, resultList.get(0).getStartTime().getTime());
        assertEquals(47000L, resultList.get(1).getStartTime().getTime());
    }

    @Test
    public void testGetCandidatesByGeometry_emptyList() {
        final List<SatelliteObservation> satelliteObservations = new ArrayList<>();

        final List<SatelliteObservation> resultList = InsituPolarOrbitingMatchupStrategy.getCandidatesByGeometry(satelliteObservations, geometryFactory.createPoint(12.0, 14.8));
        assertEquals(0, resultList.size());
    }

    @Test
    public void testGetCandidatesByGeometry_outside() {
        final List<SatelliteObservation> satelliteObservations = new ArrayList<>();
        satelliteObservations.add(createSatelliteObservation("POLYGON((0 0, 0 2, 2 2, 2 0, 0 0))"));
        satelliteObservations.add(createSatelliteObservation("POLYGON((2 0, 2 1, 3 1, 3 0, 2 0))"));
        satelliteObservations.add(createSatelliteObservation("POLYGON((2.5 0, 2.5 1, 3.5 1, 3.5 0, 2.5 0))"));

        final List<SatelliteObservation> resultList = InsituPolarOrbitingMatchupStrategy.getCandidatesByGeometry(satelliteObservations, geometryFactory.createPoint(12.0, 14.8));
        assertEquals(0, resultList.size());
    }

    @Test
    public void testGetCandidatesByGeometry_pickOne() {
        final List<SatelliteObservation> satelliteObservations = new ArrayList<>();
        satelliteObservations.add(createSatelliteObservation("POLYGON((0 0, 0 2, 2 2, 2 0, 0 0))"));
        satelliteObservations.add(createSatelliteObservation("POLYGON((2 0, 2 1, 3 1, 3 0, 2 0))"));
        satelliteObservations.add(createSatelliteObservation("POLYGON((2.5 0, 2.5 1, 3.5 1, 3.5 0, 2.5 0))"));

        final List<SatelliteObservation> resultList = InsituPolarOrbitingMatchupStrategy.getCandidatesByGeometry(satelliteObservations, geometryFactory.createPoint(2.2, 0.7));
        assertEquals(1, resultList.size());
        assertEquals("POLYGON((3.0000000000000004 0.0,3.0000000000000004 1.0,1.9999999999999996 1.0,2.0 0.0,3.0000000000000004 0.0))", geometryFactory.format(resultList.get(0).getGeoBounds()));
    }

    @Test
    public void testGetCandidatesByGeometry_pickTwo() {
        final List<SatelliteObservation> satelliteObservations = new ArrayList<>();
        satelliteObservations.add(createSatelliteObservation("POLYGON((0 0, 0 2, 2 2, 2 0, 0 0))"));
        satelliteObservations.add(createSatelliteObservation("POLYGON((2 0, 2 1, 3 1, 3 0, 2 0))"));
        satelliteObservations.add(createSatelliteObservation("POLYGON((2.5 0, 2.5 1, 3.5 1, 3.5 0, 2.5 0))"));

        final List<SatelliteObservation> resultList = InsituPolarOrbitingMatchupStrategy.getCandidatesByGeometry(satelliteObservations, geometryFactory.createPoint(2.7, 0.4));
        assertEquals(2, resultList.size());
        assertEquals("POLYGON((3.0000000000000004 0.0,3.0000000000000004 1.0,1.9999999999999996 1.0,2.0 0.0,3.0000000000000004 0.0))", geometryFactory.format(resultList.get(0).getGeoBounds()));
        assertEquals("POLYGON((3.5000000000000004 0.0,3.5 1.0,2.5000000000000004 1.0,2.5 0.0,3.5000000000000004 0.0))", geometryFactory.format(resultList.get(1).getGeoBounds()));
    }

    @Test
    public void test_getValidMatchupSet() {
        //preparation
        final MatchupSet currentMatchupSet = new MatchupSet();
        currentMatchupSet.addPrimary(new Sample(1, 2, 3, 4, 5));

        final Path[] paths = {Paths.get("p1"), Paths.get("p2"), Paths.get("p3"), Paths.get("p4")};
        final String[] versions = {"v1", "v2", "v3", "v4",};
        final String[] secSensorNames = {"sec1", "sec2", "sec3"};
        final MatchupCollection collection = new MatchupCollection();

        //execution
        final MatchupSet matchupSet = InsituPolarOrbitingMatchupStrategy.getValidMatchupSet(currentMatchupSet, paths, versions, secSensorNames, collection);

        //verification
        assertNotNull(matchupSet);
        assertEquals(Paths.get("p1"), matchupSet.getPrimaryObservationPath());
        assertEquals(Paths.get("p2"), matchupSet.getSecondaryObservationPath("sec1"));
        assertEquals(Paths.get("p3"), matchupSet.getSecondaryObservationPath("sec2"));
        assertEquals(Paths.get("p4"), matchupSet.getSecondaryObservationPath("sec3"));

        assertEquals("v1", matchupSet.getPrimaryProcessingVersion());
        assertEquals("v2", matchupSet.getSecondaryProcessingVersion("sec1"));
        assertEquals("v3", matchupSet.getSecondaryProcessingVersion("sec2"));
        assertEquals("v4", matchupSet.getSecondaryProcessingVersion("sec3"));

        final List<MatchupSet> matchupSets = collection.getSets();
        assertEquals(1, matchupSets.size());
        assertSame(currentMatchupSet, matchupSets.get(0));
    }

    @Test
    public void test_createValidSampleSet() {
        //preparation
        final String[] secSensorNames = {"sec2", "sec1"};
        final Sample sample1 = new Sample(1, 2, 3, 4, 5);
        final Sample sample2 = new Sample(2, 3, 4, 5, 6);
        final Sample sample3 = new Sample(3, 4, 5, 6, 7);
        final Sample[] samples = new Sample[]{sample1, sample2, sample3};

        //execution
        final SampleSet sampleSet = InsituPolarOrbitingMatchupStrategy.createValidSampleSet(samples, secSensorNames);

        //verification
        assertNotNull(sampleSet);
        assertSame(sample1, sampleSet.getPrimary());
        assertSame(sample3, sampleSet.getSecondary("sec1"));
        assertSame(sample2, sampleSet.getSecondary("sec2"));
    }

    @Test
    public void test_createValidSampleSet_OneOfTheSamplesIsEmpty() {
        //preparation
        final String[] secSensorNames = {"sec2", "sec1"};
        final Sample sample1 = new Sample(1, 2, 3, 4, 5);
        final Sample sample2 = null;
        final Sample sample3 = new Sample(3, 4, 5, 6, 7);
        final Sample[] samples = new Sample[]{sample1, sample2, sample3};

        //execution
        final SampleSet sampleSet = InsituPolarOrbitingMatchupStrategy
                    .createValidSampleSet(samples, secSensorNames);

        //verification
        assertNull(sampleSet);
    }

    @Test
    public void test_matchupsetIsValid_true() {
        boolean isValid;

        //preparation
        final Path primaryPath = Paths.get("prime");
        final Path firstSecPath = Paths.get("sec1");
        final Path secondSecPath = Paths.get("sec2");
        final Path[] paths = new Path[]{primaryPath, firstSecPath, secondSecPath};
        final String[] secSensorNames = new String[]{"sec1", "sec2"};
        final MatchupSet currentMatchupSet = new MatchupSet();
        currentMatchupSet.setPrimaryObservationPath(primaryPath);
        currentMatchupSet.setSecondaryObservationPath("sec1", firstSecPath);
        currentMatchupSet.setSecondaryObservationPath("sec2", secondSecPath);

        //execution
        isValid = InsituPolarOrbitingMatchupStrategy.matchupsetIsValid(currentMatchupSet, paths, secSensorNames);

        //verification
        assertTrue(isValid);
    }

    @Test
    public void test_matchupsetIsValid_false_firstSecondaryObservationPathIsNull() {
        boolean isValid;

        //preparation
        final Path primaryPath = Paths.get("prime");
        final Path firstSecPath = Paths.get("sec1");
        final Path secondSecPath = Paths.get("sec2");
        final Path[] paths = new Path[]{primaryPath, firstSecPath, secondSecPath};
        final String[] secSensorNames = new String[]{"sec1", "sec2"};
        final MatchupSet currentMatchupSet = new MatchupSet();
        currentMatchupSet.setPrimaryObservationPath(primaryPath);
        currentMatchupSet.setSecondaryObservationPath("sec1", null);
        currentMatchupSet.setSecondaryObservationPath("sec2", secondSecPath);

        //execution
        isValid = InsituPolarOrbitingMatchupStrategy.matchupsetIsValid(currentMatchupSet, paths, secSensorNames);

        //verification
        assertFalse(isValid);
    }

    @Test
    public void test_matchupsetIsValid_false_primaryObservationPathIsNull() {
        boolean isValid;

        //preparation
        final Path primaryPath = Paths.get("prime");
        final Path firstSecPath = Paths.get("sec1");
        final Path secondSecPath = Paths.get("sec2");
        final Path[] paths = new Path[]{primaryPath, firstSecPath, secondSecPath};
        final String[] secSensorNames = new String[]{"sec1", "sec2"};
        final MatchupSet currentMatchupSet = new MatchupSet();
        currentMatchupSet.setPrimaryObservationPath(null);
        currentMatchupSet.setSecondaryObservationPath("sec1", firstSecPath);
        currentMatchupSet.setSecondaryObservationPath("sec2", secondSecPath);

        //execution
        isValid = InsituPolarOrbitingMatchupStrategy.matchupsetIsValid(currentMatchupSet, paths, secSensorNames);

        //verification
        assertFalse(isValid);
    }

    @Test
    public void test_matchupsetIsValid_false_secondSecondaryObservationPathIsNotEqualToThePathInPathsArray() {
        final Path wrongSecondaryPath;
        boolean isValid;

        //preparation
        final Path primaryPath = Paths.get("prime");
        wrongSecondaryPath = primaryPath;
        final Path firstSecPath = Paths.get("sec1");
        final Path secondSecPath = Paths.get("sec2");
        final Path[] paths = new Path[]{primaryPath, firstSecPath, secondSecPath};
        final String[] secSensorNames = new String[]{"sec1", "sec2"};
        final MatchupSet currentMatchupSet = new MatchupSet();
        currentMatchupSet.setPrimaryObservationPath(primaryPath);
        currentMatchupSet.setSecondaryObservationPath("sec1", firstSecPath);
        currentMatchupSet.setSecondaryObservationPath("sec2", wrongSecondaryPath);

        //execution
        isValid = InsituPolarOrbitingMatchupStrategy.matchupsetIsValid(currentMatchupSet, paths, secSensorNames);

        //verification
        assertFalse(isValid);
    }

    private SatelliteObservation createSatelliteObservation(long startTime, long stopTime) {
        final SatelliteObservation observation = new SatelliteObservation();
        observation.setStartTime(new Date(startTime));
        observation.setStopTime(new Date(stopTime));
        return observation;
    }

    private SatelliteObservation createSatelliteObservation(String boundaryWKT) {
        final SatelliteObservation observation = new SatelliteObservation();
        final Geometry geometry = geometryFactory.parse(boundaryWKT);
        observation.setGeoBounds(geometry);
        return observation;
    }
}
