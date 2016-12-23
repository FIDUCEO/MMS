/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.post.plugin.nwp;


import java.io.File;

class Configuration {

    private boolean deleteOnExit;
    private String CDOHome;
    private int analysisSteps;
    private int forecastSteps;
    private String NWPAuxDir;
    private String timeVariableName;
    private String anSeaIceFractionName;
    private String anSSTName;
    private String anEastWindName;
    private String anNorthWindName;
    private String fcSSTName;

    Configuration() {
        deleteOnExit = true;
        analysisSteps = 17;
        forecastSteps = 33;

        anSeaIceFractionName = "matchup.nwp.an.sea_ice_fraction";
        anSSTName = "matchup.nwp.an.sea_surface_temperature";
        anEastWindName = "matchup.nwp.an.10m_east_wind_component";
        anNorthWindName = "matchup.nwp.an.10m_north_wind_component";
        fcSSTName = "matchup.nwp.fc.sea_surface_temperature";
    }

    void setDeleteOnExit(boolean deleteOnExit) {
        this.deleteOnExit = deleteOnExit;
    }

    boolean isDeleteOnExit() {
        return deleteOnExit;
    }

    void setCDOHome(String CDOHome) {
        this.CDOHome = CDOHome;
    }

    String getCDOHome() {
        return CDOHome;
    }

    void setAnalysisSteps(int analysisSteps) {
        this.analysisSteps = analysisSteps;
    }

    int getAnalysisSteps() {
        return analysisSteps;
    }

    void setForecastSteps(int forecastSteps) {
        this.forecastSteps = forecastSteps;
    }

    int getForecastSteps() {
        return forecastSteps;
    }

    void setNWPAuxDir(String NWPAuxDir) {
        this.NWPAuxDir = NWPAuxDir;
    }

    String getNWPAuxDir() {
        return NWPAuxDir;
    }

    void setTimeVariableName(String timeVariableName) {
        this.timeVariableName = timeVariableName;
    }

    String getTimeVariableName() {
        return timeVariableName;
    }

    void setAnSeaIceFractionName(String anSeaIceFractionName) {
        this.anSeaIceFractionName = anSeaIceFractionName;
    }

    String getAnSeaIceFractionName() {
        return anSeaIceFractionName;
    }

    void setAnSSTName(String anSSTName) {
        this.anSSTName = anSSTName;
    }

    String getAnSSTName() {
        return anSSTName;
    }

    void setAnEastWindName(String anEastWindName) {
        this.anEastWindName = anEastWindName;
    }

    String getAnEastWindName() {
        return anEastWindName;
    }

    void setAnNorthWindName(String anNorthWindName) {
        this.anNorthWindName = anNorthWindName;
    }

    String getAnNorthWindName() {
        return anNorthWindName;
    }

    void setFcSSTName(String fcSSTName) {
        this.fcSSTName = fcSSTName;
    }

    String getFcSSTName() {
        return fcSSTName;
    }

    boolean verify() {
        final File cdoDir = new File(CDOHome);
        if (!cdoDir.isDirectory()) {
            throw new RuntimeException("cdo executable directory does not exist");
        }

        final File nwpDir = new File(NWPAuxDir);
        if (!nwpDir.isDirectory()) {
            throw new RuntimeException("era interim aux data directory does not exist");
        }
        return true;
    }

}
