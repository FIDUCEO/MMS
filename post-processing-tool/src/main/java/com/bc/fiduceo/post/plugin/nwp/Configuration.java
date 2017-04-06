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
    private String NWPAuxDir;

    private TimeSeriesConfiguration timeSeriesConfiguration;
    private SensorExtractConfiguration sensorExtractConfiguration;
    private String tempDir;

    Configuration() {
        deleteOnExit = true;
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

    void setNWPAuxDir(String NWPAuxDir) {
        this.NWPAuxDir = NWPAuxDir;
    }

    String getNWPAuxDir() {
        return NWPAuxDir;
    }

    void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    String getTempDir() {
        return tempDir;
    }

    boolean isTimeSeriesExtraction() {
        return timeSeriesConfiguration != null;
    }

    boolean isSensorExtraction() {
        return sensorExtractConfiguration != null;
    }

    void setTimeSeriesConfiguration(TimeSeriesConfiguration timeSeriesConfiguration) {
        this.timeSeriesConfiguration = timeSeriesConfiguration;
    }

    TimeSeriesConfiguration getTimeSeriesConfiguration() {
        return timeSeriesConfiguration;
    }

    void setSensorExtractConfiguration(SensorExtractConfiguration sensorExtractConfiguration) {
        this.sensorExtractConfiguration = sensorExtractConfiguration;
    }

    SensorExtractConfiguration getSensorExtractConfiguration() {
        return sensorExtractConfiguration;
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
