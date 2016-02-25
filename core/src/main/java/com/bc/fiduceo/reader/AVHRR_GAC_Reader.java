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

package com.bc.fiduceo.reader;


import org.esa.snap.core.datamodel.GeoCoding;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class AVHRR_GAC_Reader implements Reader {

    private static final String[] SENSOR_KEYS = {"avhrr-n06", "avhrr-n07", "avhrr-n08", "avhrr-n09","avhrr-n10","avhrr-n11", "avhrr-n12", "avhrr-n13", "avhrr-n14", "avhrr-n15", "avhrr-n16", "avhrr-n17", "avhrr-n18", "avhrr-n19", "avhrr-m01", "avhrr-m02"};

    @Override
    public void open(File file) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void close() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return SENSOR_KEYS;
    }

    @Override
    public boolean checkSensorTypeName(String sensorType) {
        // @todo 1 tb/tb refactor this whole pattern 2016-02-25
        // the reader shall return an array of "sensor-platform"
        // the factory shall NOT call into the reader at this pouínt, instead the factory shall contain a hashmap with
        // all "sensor-platform" as keys and the readers as values
        return sensorType.contains("avhrr");
    }

    @Override
    public HashMap<String, String> getSensorTypes() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public GeoCoding getGeoCoding() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getRegEx() {
        return "[0-9]{14}-ESACCI-L1C-AVHRR([0-9]{2}|MTA)_G-fv\\d\\d.\\d.nc";
    }
}
