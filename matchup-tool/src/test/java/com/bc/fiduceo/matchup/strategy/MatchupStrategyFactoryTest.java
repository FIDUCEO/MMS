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

import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.matchup.TestUseCaseConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

public class MatchupStrategyFactoryTest {

    private TestUseCaseConfig useCaseConfig;
    private ArrayList<Sensor> sensors;

    @Before
    public void setUp() {
        useCaseConfig = new TestUseCaseConfig();
        sensors = new ArrayList<>();
        useCaseConfig.setSensors(sensors);
    }

    @Test
    public void testCreatePolarOrbitingStrategy() {
        final Sensor primary = new Sensor("hirs-n10", "v1.8");
        primary.setPrimary(true);
        sensors.add(primary);

        sensors.add(new Sensor("mhs-n18", "1.1"));


        final AbstractMatchupStrategy strategy = MatchupStrategyFactory.get(useCaseConfig, Logger.getAnonymousLogger());
        assertTrue(strategy instanceof PolarOrbitingMatchupStrategy);
    }

    @Test
    public void testCreateInsituPolarOrbitingStrategy() {
        final Sensor primary = new Sensor("ship-sst", "v03.3");
        primary.setPrimary(true);
        sensors.add(primary);

        sensors.add(new Sensor("hirs-n14", "1.2"));

        final AbstractMatchupStrategy strategy = MatchupStrategyFactory.get(useCaseConfig, Logger.getAnonymousLogger());
        assertTrue(strategy instanceof InsituPolarOrbitingMatchupStrategy);
    }

    @Test
    public void testCreateSeedPointStrategy() {
        String configString = "<use-case-config name=\"use case 23\">" +
                "    <random-points-per-day>" +
                "        114" +
                "    </random-points-per-day>" +
                "</use-case-config>";

        final UseCaseConfig config = UseCaseConfig.load(new ByteArrayInputStream(configString.getBytes()));

        final AbstractMatchupStrategy strategy = MatchupStrategyFactory.get(config, Logger.getAnonymousLogger());
        assertTrue(strategy instanceof SeedPointMatchupStrategy);
    }

    @Test
    public void testCreatePointExtractionStrategy() {
        final Sensor primary = new Sensor("mod06-te", "v006");
        primary.setPrimary(true);
        sensors.add(primary);

        useCaseConfig.setLon(-2.56);
        useCaseConfig.setLat(11.6);

        final AbstractMatchupStrategy strategy = MatchupStrategyFactory.get(useCaseConfig, Logger.getAnonymousLogger());
        assertTrue(strategy instanceof PointExtractionStrategy);
    }
}
