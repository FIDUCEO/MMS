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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchupSet {

    private List<SampleSet> sampleSets;

    private Path primaryObservationPath;
    private String primaryProcessingVersion;
    private final Map<String, Path> secondaryObservationPath;
    private final Map<String, String> secondaryProcessingVersion;

    public MatchupSet() {
        sampleSets = new ArrayList<>();
        secondaryObservationPath = new HashMap<>();
        secondaryProcessingVersion = new HashMap<>();
    }

    public Path getPrimaryObservationPath() {
        return primaryObservationPath;
    }

    public void setPrimaryObservationPath(Path primaryObservationPath) {
        this.primaryObservationPath = primaryObservationPath;
    }

    public Path getSecondaryObservationPath(String sensorName) {
        return secondaryObservationPath.get(sensorName);
    }

    public void setSecondaryObservationPath(String sensorName, Path secondaryObservationPath) {
        this.secondaryObservationPath.put(sensorName, secondaryObservationPath);
    }

    public void addPrimary(Sample primarySample) {
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(primarySample);

        sampleSets.add(sampleSet);
    }

    public int getNumObservations() {
        return sampleSets.size();
    }

    public List<SampleSet> getSampleSets() {
        return sampleSets;
    }

    public void setSampleSets(List<SampleSet> sampleSets) {
        this.sampleSets = sampleSets;
    }

    public void setPrimaryProcessingVersion(String primaryProcessingVersion) {
        this.primaryProcessingVersion = primaryProcessingVersion;
    }

    public String getPrimaryProcessingVersion() {
        return primaryProcessingVersion;
    }

    public void setSecondaryProcessingVersion(String sensorName, String secondaryProcessingVersion) {
        this.secondaryProcessingVersion.put(sensorName, secondaryProcessingVersion);
    }

    public String getSecondaryProcessingVersion(String sensorName) {
        return secondaryProcessingVersion.get(sensorName);
    }
}
