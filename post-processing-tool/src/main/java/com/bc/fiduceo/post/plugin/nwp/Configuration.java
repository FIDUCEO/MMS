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


class Configuration {

    private boolean deleteOnExit;
    private String CDOHome;
    private int analysisSteps;
    private int forecastSteps;
    private String NWPAuxDir;

    Configuration() {
        deleteOnExit = true;
        analysisSteps = 17;
        forecastSteps = 33;
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
}
