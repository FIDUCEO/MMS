package com.bc.fiduceo.core;

import com.vividsolutions.jts.geom.Geometry;
import org.jdom2.Element;
import ucar.ma2.Array;

import java.util.Date;

public class SatelliteObservation {

    private Date startTime;
    private Date stopTime;

    private Geometry geoSatLon;
    private Geometry geoSatLat;
    private Sensor sensor;

    public Geometry getGeoSatLon() {
        return geoSatLon;
    }

    public void setGeoSatLon(Geometry geoSatLon) {
        this.geoSatLon = geoSatLon;
    }

    public Geometry getGeoSatLat() {
        return geoSatLat;
    }

    public void setGeoSatLat(Array arrayLat) {

        this.geoSatLat = (Geometry) arrayLat.copyTo1DJavaArray();
    }

    public SatelliteObservation() {
        // Set all the settings here After the meta data have be read

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
}
