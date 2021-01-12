package com.bc.fiduceo.post.plugin.era5;

class Configuration {

    private String nwpAuxDir;
    private String era5Collection;

    private SatelliteFieldsConfiguration satelliteFields;
    private MatchupFieldsConfiguration matchupFields;

    String getNWPAuxDir() {
        return nwpAuxDir;
    }

    void setNWPAuxDir(String nwpAuxDir) {
        this.nwpAuxDir = nwpAuxDir;
    }

    String getEra5Collection() {
        return era5Collection;
    }

    void setEra5Collection(String era5Collection) {
        this.era5Collection = era5Collection;
    }

    SatelliteFieldsConfiguration getSatelliteFields() {
        return satelliteFields;
    }

    void setSatelliteFields(SatelliteFieldsConfiguration satelliteFields) {
        this.satelliteFields = satelliteFields;
    }

    MatchupFieldsConfiguration getMatchupFields() {
        return matchupFields;
    }

    void setMatchupFields(MatchupFieldsConfiguration matchupFields) {
        this.matchupFields = matchupFields;
    }
}
