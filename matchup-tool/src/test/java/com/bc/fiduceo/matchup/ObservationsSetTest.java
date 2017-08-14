package com.bc.fiduceo.matchup;

import com.bc.fiduceo.core.SatelliteObservation;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ObservationsSetTest {

    private ObservationsSet observationsSet;

    @Before
    public void setUp() {
        observationsSet = new ObservationsSet();
    }

    @Test
    public void testAddAndGet() {
        final ArrayList<SatelliteObservation> observations = new ArrayList<>();

        observationsSet.add("firlefanz", observations);
        final List<SatelliteObservation> result = observationsSet.get("firlefanz");
        assertSame(observations, result);
    }

    @Test
    public void testGet_notExisting() {
        assertNull(observationsSet.get("ham_wa_nich"));
    }

    @Test
    public void testGetSensorKeys_empty() {
        final String[] sensorKeys = observationsSet.getSensorKeys();
        assertEquals(0, sensorKeys.length);
    }

    @Test
    public void testGetSensorKeys() {
        final ArrayList<SatelliteObservation> observations = new ArrayList<>();

        observationsSet.add("klimbim", observations);
        observationsSet.add("nasenmann", observations);

        final String[] sensorKeys = observationsSet.getSensorKeys();
        assertEquals(2, sensorKeys.length);
        assertEquals("klimbim", sensorKeys[0]);
        assertEquals("nasenmann", sensorKeys[1]);
    }
}
