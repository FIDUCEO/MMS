package com.bc.fiduceo.reader;


import com.vividsolutions.jts.geom.Coordinate;

import java.util.Date;
import java.util.List;

class AcquisitionInfo {

    private List<Coordinate> coordinates;
    private int timeAxisStartIndex;
    private int timeAxisEndIndex;
    private Date sensingStart;
    private Date sensingStop;

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
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
}
