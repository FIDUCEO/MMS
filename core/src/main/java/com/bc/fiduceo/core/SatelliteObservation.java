/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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

package com.bc.fiduceo.core;


import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.TimeAxis;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class SatelliteObservation {

    private Date startTime;
    private Date stopTime;
    private Geometry geoBounds;
    private TimeAxis[] timeAxes;
    private Sensor sensor;
    private NodeType nodeType;
    private Path dataFilePath;
    private String version;

    public SatelliteObservation() {
        nodeType = NodeType.UNDEFINED;
    }

    public Geometry getGeoBounds() {
        return geoBounds;
    }

    public void setGeoBounds(Geometry geoBounds) {
        this.geoBounds = geoBounds;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getStopTime() {
        return stopTime;
    }

    public void setStopTime(Date stopTime) {
        this.stopTime = stopTime;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public Path getDataFilePath() {
        return dataFilePath;
    }

    public void setDataFilePath(String path) {
        this.dataFilePath = Paths.get(path);
    }

    public TimeAxis[] getTimeAxes() {
        return timeAxes;
    }

    public void setTimeAxes(TimeAxis[] timeAxes) {
        this.timeAxes = timeAxes;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
