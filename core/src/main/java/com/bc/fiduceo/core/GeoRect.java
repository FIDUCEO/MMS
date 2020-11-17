package com.bc.fiduceo.core;

public class GeoRect {

    private final float lonMin;
    private final float lonMax;
    private final float latMin;
    private final float latMax;

    public GeoRect(float lonMin, float lonMax, float latMin, float latMax) {
        this.lonMin = lonMin;
        this.lonMax = lonMax;
        this.latMin = latMin;
        this.latMax = latMax;
    }

    public float getLonMin() {
        return lonMin;
    }

    public float getLonMax() {
        return lonMax;
    }

    public float getLatMin() {
        return latMin;
    }

    public float getLatMax() {
        return latMax;
    }
}
