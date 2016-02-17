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

import java.io.File;
import java.util.Date;

public class SatelliteObservation {

    private Date startTime;
    private Date stopTime;
    private Geometry geoBounds;
    private Sensor sensor;
    private NodeType nodeType;
    private File dataFile;
    private int timeAxisStartIndex;
    private int timeAxisEndIndex;
    private String wkt;

    public SatelliteObservation() {
        nodeType = NodeType.UNDEFINED;
        timeAxisStartIndex = -1;
        timeAxisEndIndex = -1;
    }

    public String getWellknowText() {
        return wkt;
    }

    public void setWellknowText(String wkt) {
        this.wkt = wkt;
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

    public File getDataFile() {
        return dataFile;
    }

    public void setDataFile(File dataFile) {
        this.dataFile = dataFile;
    }

    public int getTimeAxisStartIndex() {
        return timeAxisStartIndex;
    }

    public void setTimeAxisStartIndex(int timeAxisStartIndex) {
        this.timeAxisStartIndex = timeAxisStartIndex;
    }

    public int getTimeAxisEndIndex() {
        return timeAxisEndIndex;
    }

    public void setTimeAxisEndIndex(int timeAxisEndIndex) {
        this.timeAxisEndIndex = timeAxisEndIndex;
    }
}
