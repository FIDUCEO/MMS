package com.bc.fiduceo.post.plugin.era5;

class Configuration {

    private String nwpAuxDir;

    private SatelliteFieldsConfiguration satelliteFields;
    private MatchupFieldsConfiguration matchupFields;

    String getNWPAuxDir() {
        return nwpAuxDir;
    }

    void setNWPAuxDir(String nwpAuxDir) {
        this.nwpAuxDir = nwpAuxDir;
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
