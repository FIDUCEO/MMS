package com.bc.fiduceo.post.plugin.era5;

class Configuration {

    private String nwpAuxDir;

    private SatelliteFieldsConfiguration satelliteFields;

    String getNWPAuxDir() {
        return nwpAuxDir;
    }

    void setNWPAuxDir(String nwpAuxDir) {
        this.nwpAuxDir = nwpAuxDir;
    }

    SatelliteFieldsConfiguration getSatelliteFields() {
        return satelliteFields;
    }

    public void setSatelliteFields(SatelliteFieldsConfiguration satelliteFields) {
        this.satelliteFields = satelliteFields;
    }
}
