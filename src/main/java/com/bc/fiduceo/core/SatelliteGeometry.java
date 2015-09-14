package com.bc.fiduceo.core;


import com.bc.fiduceo.math.TimeAxis;
import com.vividsolutions.jts.geom.Geometry;

public class SatelliteGeometry {

    private final Geometry geometry;
    private final TimeAxis[] timeAxes;
    private int timeAxisStartIndex;
    private int timeAxisEndIndex;

    public SatelliteGeometry(Geometry geometry, TimeAxis[] timeAxes) {
        this.geometry = geometry;
        this.timeAxes = timeAxes;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public TimeAxis[] getTimeAxes() {
        return timeAxes;
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
