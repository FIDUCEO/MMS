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

import com.bc.ceres.core.ServiceRegistry;
import com.bc.ceres.core.ServiceRegistryManager;
import org.esa.snap.SnapCoreActivator;
import org.esa.snap.core.util.StringUtils;

import java.util.HashMap;

/**
 * @author muhammad.bc
 */
public class ReaderFactory {

    // @todo 3 tb/** make this class a singleton so that we do not have to scan the services each time some component needs a factory 2016-03-14

    final HashMap<String, ReaderPlugin> readerPluginHashMap = new HashMap<>();

    public ReaderFactory() {
        final ServiceRegistryManager serviceRegistryManager = ServiceRegistryManager.getInstance();
        final ServiceRegistry<ReaderPlugin> readerRegistry = serviceRegistryManager.getServiceRegistry(ReaderPlugin.class);
        SnapCoreActivator.loadServices(readerRegistry);

        for (ReaderPlugin plugin : readerRegistry.getServices()) {
            String[] supportedSensorKeys = plugin.getSupportedSensorKeys();
            for (String key : supportedSensorKeys) {
                readerPluginHashMap.put(key, plugin);
            }
        }
    }

    public Reader getReader(String sensorPlatformKey) {
        if (StringUtils.isNullOrEmpty(sensorPlatformKey)) {
            throw new IllegalArgumentException("The reader support sensor key most be well define");
        }

        final ReaderPlugin readerPlugin = readerPluginHashMap.get(sensorPlatformKey);
        if (readerPlugin == null) {
            throw new NullPointerException("No support sensor with such :" + sensorPlatformKey + " key");
        }
        return readerPlugin.createReader();
    }
}
