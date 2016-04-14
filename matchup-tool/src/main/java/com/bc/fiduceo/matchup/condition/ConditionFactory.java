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
package com.bc.fiduceo.matchup.condition;

import com.bc.ceres.core.ServiceRegistry;
import com.bc.ceres.core.ServiceRegistryManager;
import org.esa.snap.SnapCoreActivator;
import org.jdom.Element;

import java.util.HashMap;

public class ConditionFactory {

    private static ConditionFactory conditionFactory;
    private final HashMap<String, ConditionPlugin> conditionPluginMap = new HashMap<>();

    private ConditionFactory() {
        final ServiceRegistryManager serviceRegistryManager = ServiceRegistryManager.getInstance();
        final ServiceRegistry<ConditionPlugin> conditionPlugins = serviceRegistryManager.getServiceRegistry(ConditionPlugin.class);
        SnapCoreActivator.loadServices(conditionPlugins);

        for (ConditionPlugin plugin : conditionPlugins.getServices()) {
            final String key = plugin.getConditionName();
            conditionPluginMap.put(key, plugin);
        }
    }

    public static ConditionFactory get() {
        if (conditionFactory == null) {
            conditionFactory = new ConditionFactory();
        }
        return conditionFactory;
    }

    public Condition getCondition(Element element) {
        final String name = element.getName();

        final ConditionPlugin plugin = conditionPluginMap.get(name);
        if (plugin == null) {
            throw new IllegalArgumentException("Condition for name '" + name + "' not available.");
        }
        return plugin.createCondition(element);
    }

}
