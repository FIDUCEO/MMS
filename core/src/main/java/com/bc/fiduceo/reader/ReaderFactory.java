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
import com.bc.fiduceo.geometry.GeometryFactory;
import org.esa.snap.SnapCoreActivator;
import org.esa.snap.core.util.StringUtils;

import java.util.HashMap;

/**
 * @author muhammad.bc
 */
public class ReaderFactory {

    private static ReaderFactory readerFactory;

    private final HashMap<String, ReaderPlugin> readerPluginHashMap = new HashMap<>();
    private final GeometryFactory geometryFactory;

    public static ReaderFactory get(GeometryFactory geometryFactory) {
        if (readerFactory == null) {
            readerFactory = new ReaderFactory(geometryFactory);
        }
        return readerFactory;
    }

    public Reader getReader(String sensorPlatformKey) {
        if (StringUtils.isNullOrEmpty(sensorPlatformKey)) {
            throw new IllegalArgumentException("No sensor key supplied to select data reader.");
        }

        final ReaderPlugin readerPlugin = getReaderPluginSafe(sensorPlatformKey);
        return readerPlugin.createReader(geometryFactory);
    }

    public DataType getDataType(String sensorPlatformKey) {
        final ReaderPlugin readerPlugin = getReaderPluginSafe(sensorPlatformKey);
        return readerPlugin.getDataType();
    }

    private ReaderPlugin getReaderPluginSafe(String sensorPlatformKey) {
        final ReaderPlugin readerPlugin = readerPluginHashMap.get(sensorPlatformKey);
        if (readerPlugin == null) {
            throw new IllegalArgumentException("No reader available for data of type: '" + sensorPlatformKey + "'");
        }
        return readerPlugin;
    }

    private ReaderFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;

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
}
