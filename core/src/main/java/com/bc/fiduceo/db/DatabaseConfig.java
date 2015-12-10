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

package com.bc.fiduceo.db;

import org.apache.commons.dbcp.BasicDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConfig {

    private final Properties properties;

    public DatabaseConfig() {
        properties = new Properties();
    }

    public void loadFrom(File configDirectory) throws IOException {
        final File databasePropertiesFile = new File(configDirectory, "database.properties");
        if (!databasePropertiesFile.isFile()) {
            throw new RuntimeException("Configuration file not found: " + databasePropertiesFile.getAbsolutePath());
        }

        try (FileInputStream fileInputStream = new FileInputStream(databasePropertiesFile)) {
            properties.load(fileInputStream);
        }
    }

    public BasicDataSource getDataSource() {
        if (properties.isEmpty()) {
            throw new RuntimeException("database.properties not loaded");
        }

        final BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName(properties.getProperty("driverClassName"));
        dataSource.setUrl(properties.getProperty("url"));
        dataSource.setUsername(properties.getProperty("username"));
        dataSource.setPassword(properties.getProperty("password"));

        return dataSource;
    }
}
