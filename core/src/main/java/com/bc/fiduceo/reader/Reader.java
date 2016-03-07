/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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

import com.bc.fiduceo.location.PixelLocator;
import org.esa.snap.core.datamodel.GeoCoding;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public interface Reader {

    void open(File file) throws IOException;

    void close() throws IOException;

    @Deprecated
    boolean checkSensorTypeName(String sensorType);

    String[] getSupportedSensorKeys();

    @Deprecated
    HashMap<String, String> getSensorTypes();

    AcquisitionInfo read() throws IOException;

    PixelLocator getPixelLocator() throws IOException;

    String getRegEx();
}
