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
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class InsituPolarOrbitingMatchupStrategyTest {

    @Test
    public void testGetCandidatesByTime_emptyList() {
        final List<SatelliteObservation> satelliteObservations = new ArrayList<>();

        final List<SatelliteObservation> resultList = InsituPolarOrbitingMatchupStrategy.getCandidatesByTime(satelliteObservations, new Date(20000));
        assertEquals(0, resultList.size());
    }

    @Test
    public void testGetCandidatesByTime_before() {
        final List<SatelliteObservation> satelliteObservations = new ArrayList<>();
        final SatelliteObservation observation = createSatelliteObservation(30000L, 40000L);
        satelliteObservations.add(observation);

        final List<SatelliteObservation> resultList = InsituPolarOrbitingMatchupStrategy.getCandidatesByTime(satelliteObservations, new Date(20000));
        assertEquals(0, resultList.size());
    }

    @Test
    public void testGetCandidatesByTime_after() {
        final List<SatelliteObservation> satelliteObservations = new ArrayList<>();
        final SatelliteObservation observation = createSatelliteObservation(30000L, 40000L);
        satelliteObservations.add(observation);

        final List<SatelliteObservation> resultList = InsituPolarOrbitingMatchupStrategy.getCandidatesByTime(satelliteObservations, new Date(41000));
        assertEquals(0, resultList.size());
    }

    @Test
    public void testGetCandidatesByTime_pickOne() {
        final List<SatelliteObservation> satelliteObservations = new ArrayList<>();
        satelliteObservations.add(createSatelliteObservation(30000L, 40000L));
        satelliteObservations.add(createSatelliteObservation(35000L, 45000L));
        satelliteObservations.add(createSatelliteObservation(37000L, 47000L));

        final List<SatelliteObservation> resultList = InsituPolarOrbitingMatchupStrategy.getCandidatesByTime(satelliteObservations, new Date(46000));
        assertEquals(1, resultList.size());
        assertEquals(37000L, resultList.get(0).getStartTime().getTime());
    }

    @Test
    public void testGetCandidatesByTime_picktwo() {
        final List<SatelliteObservation> satelliteObservations = new ArrayList<>();
        satelliteObservations.add(createSatelliteObservation(40000L, 50000L));
        satelliteObservations.add(createSatelliteObservation(45000L, 55000L));
        satelliteObservations.add(createSatelliteObservation(47000L, 57000L));

        final List<SatelliteObservation> resultList = InsituPolarOrbitingMatchupStrategy.getCandidatesByTime(satelliteObservations, new Date(52000));
        assertEquals(2, resultList.size());
        assertEquals(45000L, resultList.get(0).getStartTime().getTime());
        assertEquals(47000L, resultList.get(1).getStartTime().getTime());
    }

    private SatelliteObservation createSatelliteObservation(long startTime, long stopTime) {
        final SatelliteObservation observation = new SatelliteObservation();
        observation.setStartTime(new Date(startTime));
        observation.setStopTime(new Date(stopTime));
        return observation;
    }
}
