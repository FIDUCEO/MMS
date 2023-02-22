package com.bc.fiduceo.reader.insitu.ndbc;

class Station {

    private final String id;
    private float lon;
    private float lat;
    private float anHeight;
    private StationType type;

    Station(String id) {
        this.id = id;

        lon = Float.NaN;
        lat = Float.NaN;
        anHeight = Float.NaN;
    }

    String getId() {
        return id;
    }

     float getLon() {
        return lon;
    }

     float getLat() {
        return lat;
    }

    public StationType getType() {
        return type;
    }

    public float getAnemometerHeight() {
        return anHeight;
    }
}
