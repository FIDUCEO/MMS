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

package com.bc.fiduceo.reader.atsr;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderPlugin;

public class ATSR_L1B_ReaderPlugin implements ReaderPlugin {

    private static final String[] SENSOR_KEYS = new String[]{"atsr-e1", "atsr-e2", "aatsr-en"};

    @Override
    public Reader createReader(GeometryFactory geometryFactory) {
        return new ATSR_L1B_Reader();
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return SENSOR_KEYS;
    }
}
