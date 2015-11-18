package com.bc.fiduceo.reader;


import com.bc.fiduceo.core.NodeType;
import com.vividsolutions.jts.geom.Coordinate;

import java.util.Date;
import java.util.List;

class AcquisitionInfo {

    private List<Coordinate> coordinates;
    private int[] timeAxisStartIndices;
    private int[] timeAxisEndIndices;
    private Date sensingStart;
    private Date sensingStop;
    private NodeType nodeType;

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public int[] getTimeAxisStartIndices() {
        return timeAxisStartIndices;
    }

    public void setTimeAxisStartIndices(int[] timeAxisStartIndices) {
        this.timeAxisStartIndices = timeAxisStartIndices;
    }

    public int[] getTimeAxisEndIndices() {
        return timeAxisEndIndices;
    }

    public void setTimeAxisEndIndices(int[] timeAxisEndIndices) {
        this.timeAxisEndIndices = timeAxisEndIndices;
    }

    public Date getSensingStart() {
        return sensingStart;
    }

    public void setSensingStart(Date sensingStart) {
        this.sensingStart = sensingStart;
    }

    public Date getSensingStop() {
        return sensingStop;
    }

    public void setSensingStop(Date sensingStop) {
        this.sensingStop = sensingStop;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }
}
