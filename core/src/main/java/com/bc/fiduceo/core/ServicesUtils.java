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

import com.bc.ceres.core.ServiceRegistry;
import com.bc.ceres.core.ServiceRegistryManager;
import com.bc.fiduceo.db.Driver;
import com.bc.fiduceo.reader.Reader;
import org.esa.snap.SnapCoreActivator;

import java.util.Set;

/**
 * @author muhammad.bc
 */
public class ServicesUtils<T> {


    public T getServices(Class<T> pass, String searchTerm) {
        boolean isReaderSensorType = false;
        String content = " ";
        final ServiceRegistryManager serviceRegistryManager = ServiceRegistryManager.getInstance();
        ServiceRegistry<T> readerRegistry = serviceRegistryManager.getServiceRegistry(pass);
        SnapCoreActivator.loadServices(readerRegistry);
        final Set<T> services = readerRegistry.getServices();
        for (T service : services) {
            if (pass.getName().contains("Driver")) {
                content = ((Driver) service).getUrlPattern().toLowerCase();
            } else {
                isReaderSensorType = ((Reader) service).checkSensorTypeName(searchTerm);
            }

            if (searchTerm.contains(content)) {
                return service;
            }

            if (isReaderSensorType) {
                return service;
            }
        }
        return null;
    }
}
