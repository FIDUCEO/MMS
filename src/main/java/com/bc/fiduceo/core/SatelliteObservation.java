package com.bc.fiduceo.core;

import com.vividsolutions.jts.geom.Geometry;
import org.esa.snap.dataio.netcdf.metadata.profiles.hdfeos.HdfEosUtils;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.filter.Filter;

import java.util.Date;
import java.util.List;

public class SatelliteObservation {

    private Date startTime;
    private Date stopTime;
    private Geometry geometry;

    private Sensor sensor;
    private Element element;
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

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }


}
