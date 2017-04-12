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
 */

package com.bc.fiduceo.post;

import com.bc.ceres.core.ServiceRegistry;
import com.bc.ceres.core.ServiceRegistryManager;
import org.esa.snap.SnapCoreActivator;
import org.jdom.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class PostProcessingFactory {

    private static PostProcessingFactory postProcessingFactory;
    private final HashMap<String, PostProcessingPlugin> postProcessingPluginMap = new HashMap<>();

    private PostProcessingFactory() {
        final ServiceRegistryManager serviceRegistryManager = ServiceRegistryManager.getInstance();
        final ServiceRegistry<PostProcessingPlugin> postProcessingPlugins = serviceRegistryManager.getServiceRegistry(PostProcessingPlugin.class);
        SnapCoreActivator.loadServices(postProcessingPlugins);

        for (PostProcessingPlugin plugin : postProcessingPlugins.getServices()) {
            final String key = plugin.getPostProcessingName();
            postProcessingPluginMap.put(key, plugin);
        }
    }

    public static PostProcessingFactory get() {
        if (postProcessingFactory == null) {
            postProcessingFactory = new PostProcessingFactory();
        }
        return postProcessingFactory;
    }

    public PostProcessing getPostProcessing(Element element) {
        final String name = element.getName();

        final PostProcessingPlugin plugin = postProcessingPluginMap.get(name);
        if (plugin == null) {
            throw new IllegalArgumentException("PostProcessing for name '" + name + "' not available.");
        }
        return plugin.createPostProcessing(element);
    }

    // package access for testing only se 2016-11-28
    Map<String, PostProcessingPlugin> getPlugins() {
        return Collections.unmodifiableMap(postProcessingPluginMap);
    }
}
