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

package com.bc.fiduceo.core;

import org.esa.snap.core.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class SystemConfig {

    private final Properties properties;

    public SystemConfig() {
        properties = new Properties();
    }

    public void loadFrom(File configDirectory) throws IOException {
        final File systemPropertiesFile = new File(configDirectory, "system.properties");
        if (!systemPropertiesFile.isFile()) {
            throw new RuntimeException("Configuration file not found: " + systemPropertiesFile.getAbsolutePath());
        }

        try (FileInputStream fileInputStream = new FileInputStream(systemPropertiesFile)) {
            properties.load(fileInputStream);
        }
    }

    public String getArchiveRoot() {
        return properties.getProperty("archive-root").trim();
    }

    public String getGeometryLibraryType() {
        return properties.getProperty("geometry-library-type", "S2").trim();
    }
}
