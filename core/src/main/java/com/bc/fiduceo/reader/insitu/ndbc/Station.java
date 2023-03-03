package com.bc.fiduceo.reader.insitu.ndbc;

class Station {

    private final String id;
    private final MeasurementType measurementType;
    private float lon;
    private float lat;
    private float anHeight;
    private float airTempHeight;
    private float barHeight;
    private float sstDepth;
    private StationType type;

    public Station(String id, MeasurementType measurementType) {
        this.id = id;
        this.measurementType = measurementType;

        lon = Float.NaN;
        lat = Float.NaN;
        anHeight = Float.NaN;
        airTempHeight = Float.NaN;
        barHeight = Float.NaN;
        sstDepth = Float.NaN;
    }

    String getId() {
        return id;
    }

    float getLon() {
        return lon;
    }

    void setLon(float lon) {
        this.lon = lon;
    }

    float getLat() {
        return lat;
    }

    void setLat(float lat) {
        this.lat = lat;
    }

    StationType getType() {
        return type;
    }

    void setType(StationType stationType) {
        this.type = stationType;
    }

    float getAnemometerHeight() {
        return anHeight;
    }

    void setAnemometerHeight(float anHeight) {
        this.anHeight = anHeight;
    }

    float getAirTemperatureHeight() {
        return airTempHeight;
    }

    void setAirTemperatureHeight(float airTempHeight) {
        this.airTempHeight = airTempHeight;
    }

    float getBarometerHeight() {
        return barHeight;
    }

    void setBarometerHeight(float barHeight) {
        this.barHeight = barHeight;
    }

    float getSSTDepth() {
        return sstDepth;
    }

    void setSSTDepth(float sstDepth) {
        this.sstDepth = sstDepth;
    }

    MeasurementType getMeasurementType() {
        return measurementType;
    }
}
