package com.bc.fiduceo.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SatelliteObservationTest {

    @Test
    public void testConstructor() {
        final SatelliteObservation observation = new SatelliteObservation();

        assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        assertEquals(-1, observation.getTimeAxisStartIndex());
        assertEquals(-1, observation.getTimeAxisEndIndex());
    }
}
