/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.fiduceo.matchup.screening;

import com.bc.ceres.core.ServiceRegistry;
import com.bc.ceres.core.ServiceRegistryManager;
import org.esa.snap.SnapCoreActivator;
import org.jdom.Element;

import java.util.HashMap;

public class ScreeningFactory {

    private static ScreeningFactory screeningFactory;
    private final HashMap<String, ScreeningPlugin> screeningPluginMap = new HashMap<>();

    private ScreeningFactory() {
        final ServiceRegistryManager serviceRegistryManager = ServiceRegistryManager.getInstance();
        final ServiceRegistry<ScreeningPlugin> screeningPlugins = serviceRegistryManager.getServiceRegistry(ScreeningPlugin.class);
        SnapCoreActivator.loadServices(screeningPlugins);

        for (ScreeningPlugin plugin : screeningPlugins.getServices()) {
            final String key = plugin.getScreeningName();
            screeningPluginMap.put(key, plugin);
        }
    }

    public static ScreeningFactory get() {
        if (screeningFactory == null) {
            screeningFactory = new ScreeningFactory();
        }
        return screeningFactory;
    }

    public Screening getScreening(Element element) {
        final String name = element.getName();

        final ScreeningPlugin plugin = screeningPluginMap.get(name);
        if (plugin == null) {
            throw new IllegalArgumentException("Screening for name '" + name + "' not available.");
        }
        return plugin.createScreening(element);
    }

}
