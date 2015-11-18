package com.bc.fiduceo.core;

import com.vividsolutions.jts.geom.Geometry;

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

    public SatelliteObservation() {
        nodeType = NodeType.UNDEFINED;
        timeAxisStartIndex = -1;
        timeAxisEndIndex = -1;
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
