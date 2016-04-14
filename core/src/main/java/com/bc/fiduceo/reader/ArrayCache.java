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
 *
 */

package com.bc.fiduceo.reader;

import org.esa.snap.core.util.StringUtils;
import ucar.ma2.Array;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayCache {

    private final NetcdfFile netcdfFile;
    private final HashMap<String, ArrayContainer> cache;
    private final HashMap<String, ArrayContainer> scaledCache;

    public ArrayCache(NetcdfFile netcdfFile) {
        this.netcdfFile = netcdfFile;

        cache = new HashMap<>();
        scaledCache = new HashMap<>();
    }

    public Array get(String variableName) throws IOException {
        ArrayContainer container = cache.get(variableName);
        if (container == null) {
            synchronized (netcdfFile) {
                container = readArrayAndAttributes(variableName, null);
            }
            cache.put(variableName, container);
        }

        return container.array;
    }

    public Array get(String groupName, String variableName) throws IOException {
        final String groupedVariableName = createGroupedName(groupName, variableName);
        ArrayContainer container = cache.get(groupedVariableName);
        if (container == null) {
            synchronized (netcdfFile) {
                container = readArrayAndAttributesFromGroup(variableName, groupName);
            }
            cache.put(groupedVariableName, container);
        }
        return container.array;
    }

    public Array getScaled(String variableName, String groupName, String scaleAttributeName, String offsetAttributeName) throws IOException {
        final String groupedVariableName = createGroupedName(groupName, variableName);
        ArrayContainer arrayContainer = scaledCache.get(groupedVariableName);
        if (arrayContainer == null) {
            arrayContainer = cache.get(groupedVariableName);
            if (arrayContainer == null) {
                synchronized (netcdfFile) {
                    arrayContainer = readArrayAndAttributesFromGroup(variableName, groupName);
                }
                cache.put(groupedVariableName, arrayContainer);
            }

            float scale = 1.f;
            float offset = 0.f;

            if (StringUtils.isNotNullAndNotEmpty(scaleAttributeName)) {
                final Attribute scaleAttribute = arrayContainer.get(scaleAttributeName);
                if (scaleAttribute != null) {
                    scale = scaleAttribute.getNumericValue().floatValue();
                }
            }

            if (StringUtils.isNotNullAndNotEmpty(offsetAttributeName)) {
                final Attribute offsetAttribute = arrayContainer.get(offsetAttributeName);
                if (offsetAttribute != null) {
                    offset = offsetAttribute.getNumericValue().floatValue();
                }
            }

            scaleIfNecessary(arrayContainer, scale, offset);

            scaledCache.put(groupedVariableName, arrayContainer);

        }
        return arrayContainer.array;
    }

    public Array getScaled(String variableName, String scaleAttributeName, String offsetAttributeName) throws IOException {
        ArrayContainer arrayContainer = scaledCache.get(variableName);
        if (arrayContainer == null) {
            arrayContainer = cache.get(variableName);
            if (arrayContainer == null) {
                synchronized (netcdfFile) {
                    arrayContainer = readArrayAndAttributes(variableName, null);
                }
                cache.put(variableName, arrayContainer);
            }

            float scale = 1.f;
            float offset = 0.f;

            if (StringUtils.isNotNullAndNotEmpty(scaleAttributeName)) {
                final Attribute scaleAttribute = arrayContainer.get(scaleAttributeName);
                if (scaleAttribute != null) {
                    scale = scaleAttribute.getNumericValue().floatValue();
                }
            }

            if (StringUtils.isNotNullAndNotEmpty(offsetAttributeName)) {
                final Attribute offsetAttribute = arrayContainer.get(offsetAttributeName);
                if (offsetAttribute != null) {
                    offset = offsetAttribute.getNumericValue().floatValue();
                }
            }

            scaleIfNecessary(arrayContainer, scale, offset);

            scaledCache.put(variableName, arrayContainer);

        }
        return arrayContainer.array;
    }

    // package access for testing only tb 2016-04-14
    static String createGroupedName(String groupName, String variableName) {
        return groupName + "_" + variableName;
    }

    private ArrayContainer readArrayAndAttributes(String variableName, Group group) throws IOException {
        ArrayContainer container;
        final Variable variable = netcdfFile.findVariable(group, variableName);
        if (variable == null) {
            throw new IOException("requested variable '" + variableName + "' not present in file");
        }
        container = new ArrayContainer();
        container.array = variable.read();

        final List<Attribute> attributes = variable.getAttributes();
        for (final Attribute attribute : attributes) {
            container.attributes.put(attribute.getFullName(), attribute);
        }
        return container;
    }

    private ArrayContainer readArrayAndAttributesFromGroup(String variableName, String groupName) throws IOException {
        ArrayContainer container;
        final Group group = netcdfFile.findGroup(groupName);
        if (group == null) {
            throw new IOException("requested group '" + groupName + "' not present in file");
        }
        container = readArrayAndAttributes(variableName, group);
        return container;
    }

    private void scaleIfNecessary(ArrayContainer arrayContainer, float scale, float offset) {
        if (scale != 1.f || offset != 0.f) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scale, offset);
            arrayContainer.array = MAMath.convert2Unpacked(arrayContainer.array, scaleOffset);
            final Array floatArray= Array.factory(Float.class, arrayContainer.array.getShape());
            MAMath.copyFloat(floatArray, arrayContainer.array);
            arrayContainer.array = floatArray;
        }
    }

    private class ArrayContainer {
        Array array;
        Map<String, Attribute> attributes;

        ArrayContainer() {
            attributes = new HashMap<>();
        }

        Attribute get(String name) {
            return attributes.get(name);
        }
    }
}
