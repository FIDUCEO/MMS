
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


import com.bc.ceres.core.ServiceRegistry;
import com.bc.ceres.core.ServiceRegistryManager;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import org.apache.commons.dbcp.BasicDataSource;
import org.esa.snap.SnapCoreActivator;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class Storage {

    // @todo 4 tb/tb introduce hashmap with url as key - make singleton per DB-Url 2015-08-05
    private static Storage storage;

    private Driver driver;


    public static Storage create(BasicDataSource dataSource) throws SQLException {
        if (storage == null) {
            storage = new Storage(dataSource);
        }
        return storage;
    }

    public void close() throws SQLException {
        if (storage == null) {
            return;
        }

        if (driver != null) {
            driver.close();
            driver = null;
        }

        storage = null;
    }

    public void initialize() throws SQLException {
        driver.initialize();
    }

    public void clear() throws SQLException {
        driver.clear();
    }

    public void insert(SatelliteObservation satelliteObservation) throws SQLException {
        driver.insert(satelliteObservation);
    }

    public List<SatelliteObservation> get() throws SQLException {
        return driver.get();
    }

    public int insert(Sensor sensor) throws SQLException {
        return driver.insert(sensor);
    }

    Storage(BasicDataSource dataSource) throws SQLException {
        driver = createDriver(dataSource);
        if (driver == null) {
            throw new IllegalArgumentException("No database driver registered for URL `" + dataSource.getUrl() + "`");
        }

        driver.open(dataSource);
    }

    private Driver createDriver(BasicDataSource dataSource) {
        final ServiceRegistryManager serviceRegistryManager = ServiceRegistryManager.getInstance();
        final ServiceRegistry<Driver> driverRegistry = serviceRegistryManager.getServiceRegistry(Driver.class);

        SnapCoreActivator.loadServices(driverRegistry);
        final Set<Driver> services = driverRegistry.getServices();
        final String dbUrl = dataSource.getUrl().toLowerCase();
        for (final Driver driver : services) {
            final String urlPattern = driver.getUrlPattern().toLowerCase();
            if (dbUrl.startsWith(urlPattern)) {
                return driver;
            }
        }

        return null;
    }
}
