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
import org.esa.snap.core.util.ServiceLoader;

import java.util.Set;

/**
 * @author muhammad.bc
 */
class DriverUtils {

    Driver getDriver(String driverUrl) {
        final ServiceRegistryManager serviceRegistryManager = ServiceRegistryManager.getInstance();
        final ServiceRegistry<Driver> readerRegistry = serviceRegistryManager.getServiceRegistry(Driver.class);

        ServiceLoader.loadServices(readerRegistry);

        final Set<Driver> driverSet = readerRegistry.getServices();
        if (driverSet.isEmpty()) {
            throw new RuntimeException("No database drivers available");
        }

        for (Driver driver : driverSet) {
            final String driverPattern = driver.getUrlPattern().toLowerCase();
            if (driverUrl.contains(driverPattern)) {
                return driver;
            }
        }
        throw new IllegalArgumentException("No database driver with the name: " + driverUrl);
    }
}
