package com.bc.fiduceo.matchup;

import com.bc.fiduceo.core.SatelliteObservation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObservationsSet {

    private Map<String, List<SatelliteObservation>> observationsSet;

    public ObservationsSet() {
        this.observationsSet = new HashMap<>();
    }

    public void add(String sensorKey, List<SatelliteObservation> observations) {
        observationsSet.put(sensorKey, observations);
    }

    public List<SatelliteObservation> get(String sensorKey) {
        return observationsSet.get(sensorKey);
    }

    public String[] getSensorKeys() {
        return observationsSet.keySet().toArray(new String[]{});
    }
}
