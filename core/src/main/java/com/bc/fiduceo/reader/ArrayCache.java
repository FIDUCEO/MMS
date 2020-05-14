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
import ucar.nc2.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayCache {

    private final NetcdfFile netcdfFile;
    private final HashMap<String, Variable> injectedVariables;
    private final HashMap<String, ArrayContainer> cache;
    private final HashMap<String, ArrayContainer> scaledCache;
    private VariableFinder variableFinder;

    public ArrayCache(NetcdfFile netcdfFile) {
        this.netcdfFile = netcdfFile;

        cache = new HashMap<>();
        scaledCache = new HashMap<>();
        injectedVariables = new HashMap<>();
        variableFinder = netcdfFile::findVariable;
    }

    // package access for testing only tb 2016-04-14
    static String createGroupedName(String groupName, String variableName) {
        return groupName + "_" + variableName;
    }

    public Array get(String variableName) throws IOException {
        ArrayContainer container = cache.get(variableName);
        if (container == null) {
            container = readArrayAndAttributes(variableName, null);
            cache.put(variableName, container);
        }

        return container.array;
    }

    public Array get(String groupName, String variableName) throws IOException {
        final String groupedVariableName = createGroupedName(groupName, variableName);
        ArrayContainer container = cache.get(groupedVariableName);
        if (container == null) {
            container = readArrayAndAttributesFromGroup(variableName, groupName);
            cache.put(groupedVariableName, container);
        }
        return container.array;
    }

    public Array getScaled(String groupName, String variableName, String scaleAttributeName, String offsetAttributeName) throws IOException {
        final String groupedVariableName = createGroupedName(groupName, variableName);
        ArrayContainer arrayContainer = scaledCache.get(groupedVariableName);
        if (arrayContainer == null) {
            arrayContainer = cache.get(groupedVariableName);
            if (arrayContainer == null) {
                arrayContainer = readArrayAndAttributesFromGroup(variableName, groupName);
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
                arrayContainer = readArrayAndAttributes(variableName, null);
                cache.put(variableName, arrayContainer);
            }

            float scale = 1.f;
            float offset = 0.f;

            if (StringUtils.isNotNullAndNotEmpty(scaleAttributeName)) {
                final Attribute scaleAttribute = arrayContainer.get(scaleAttributeName);
                if (scaleAttribute == null) {
                    throw new RuntimeException("Scale attribute with name '" + scaleAttributeName + "' is not available.");
                } else {
                    scale = scaleAttribute.getNumericValue().floatValue();
                }
            }

            if (StringUtils.isNotNullAndNotEmpty(offsetAttributeName)) {
                final Attribute offsetAttribute = arrayContainer.get(offsetAttributeName);
                if (offsetAttribute == null) {
                    throw new RuntimeException("Offset attribute with name '" + offsetAttributeName + "' is not available.");
                } else {
                    offset = offsetAttribute.getNumericValue().floatValue();
                }
            }

            scaleIfNecessary(arrayContainer, scale, offset);

            scaledCache.put(variableName, arrayContainer);

        }
        return arrayContainer.array;
    }

    /**
     * Retrieves the string representation of the attribute. Returns null if attribute is not present.
     *
     * @param attributeName the attribute name
     * @param groupName     the name of the group containing the variable
     * @param variableName  the variable name
     * @return the string value or null
     * @throws IOException on disk access failures
     */
    public String getStringAttributeValue(String attributeName, String groupName, String variableName) throws IOException {
        final Array array = get(groupName, variableName);
        if (array != null) {
            final String groupedName = createGroupedName(groupName, variableName);
            return getAttributeStringValue(attributeName, groupedName);
        }

        return null;
    }

    /**
     * Replaces the default variable finder with the given.
     *
     * @param variableFinder a variable selection strategy
     */
    public ArrayCache withVariableFinder(VariableFinder variableFinder) {
        this.variableFinder = variableFinder;
        return this;
    }

    /**
     * Retrieves the number representation of the attribute. Returns null if attribute is not present.
     *
     * @param attributeName the attribute name
     * @param variableName  the variable name
     * @return the number value or null
     * @throws IOException on disk access failures
     */
    public Number getNumberAttributeValue(String attributeName, String variableName) throws IOException {
        final Array array = get(variableName);
        if (array != null) {
            return getAttributeNumberValue(attributeName, variableName);
        }

        return null;
    }

    /**
     * Retrieves the number representation of the attribute. Returns null if attribute is not present.
     *
     * @param attributeName the attribute name
     * @param groupName     the name of the group containing the variable
     * @param variableName  the variable name
     * @return the number value or null
     * @throws IOException on disk access failures
     */
    public Number getNumberAttributeValue(String attributeName, String groupName, String variableName) throws IOException {
        final Array array = get(groupName, variableName);
        if (array != null) {
            final String groupedName = createGroupedName(groupName, variableName);
            return getAttributeNumberValue(attributeName, groupedName);
        }

        return null;
    }

    public void inject(Variable variable) {
        injectedVariables.put(variable.getShortName(), variable);
    }

    public List<Variable> getInjectedVariables() {
        final HashMap<String, Variable> variableHashMap = this.injectedVariables;
        final ArrayList<Variable> resultList = new ArrayList<>(variableHashMap.size());
        resultList.addAll(variableHashMap.values());
        return resultList;
    }

    /**
     * Retrieves the string representation of the attribute. Returns null if attribute is not present.
     *
     * @param attributeName the attribute name
     * @param variableName  the variable name
     * @return the string value or null
     * @throws IOException on disk access failures
     */
    String getStringAttributeValue(String attributeName, String variableName) throws IOException {
        final Array array = get(variableName);
        if (array != null) {
            return getAttributeStringValue(attributeName, variableName);
        }

        return null;
    }

    private String getAttributeStringValue(String attributeName, String variableKey) {
        final ArrayContainer arrayContainer = cache.get(variableKey);
        final Attribute attribute = arrayContainer.get(attributeName);
        if (attribute != null) {
            if (attribute.isString()) {
                return attribute.getStringValue();
            } else {
                return attribute.getNumericValue().toString();
            }
        }
        return null;
    }

    private Number getAttributeNumberValue(String attributeName, String variableKey) {
        final ArrayContainer arrayContainer = cache.get(variableKey);
        final Attribute attribute = arrayContainer.get(attributeName);
        if (attribute != null) {
            if (!attribute.isString()) {
                return attribute.getNumericValue();
            }
        }
        return null;
    }

    private ArrayContainer readArrayAndAttributes(String variableName, Group group) throws IOException {
        ArrayContainer container;
        Variable variable = injectedVariables.get(variableName);
        if (variable == null) {
            synchronized (netcdfFile) {
                variable = variableFinder.findVariable(group, variableName);
            }
            if (variable == null) {
                throw new IOException("requested variable '" + variableName + "' not present in file: " + netcdfFile.getLocation());
            }
        }
        container = new ArrayContainer();
        synchronized (netcdfFile) {
            container.array = variable.read();
        }

        final AttributeContainer attributes = variable.attributes();
        if (attributes != null) {
            for (final Attribute attribute : attributes) {
                container.attributes.put(attribute.getFullName(), attribute);
            }
        }

        return container;
    }

    private ArrayContainer readArrayAndAttributesFromGroup(String variableName, String groupName) throws IOException {
        ArrayContainer container;
        final Group group;
        synchronized (netcdfFile) {
            group = netcdfFile.findGroup(groupName);
        }
        if (group == null) {
            throw new IOException("requested group '" + groupName + "' not present in file: " + netcdfFile.getLocation());
        }
        container = readArrayAndAttributes(variableName, group);
        return container;
    }

    private void scaleIfNecessary(ArrayContainer arrayContainer, float scale, float offset) {
        if (scale != 1.f || offset != 0.f) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scale, offset);
            arrayContainer.array = MAMath.convert2Unpacked(arrayContainer.array, scaleOffset);
        }
    }

    /**
     * To find a variable in a netcdf file
     */
    public interface VariableFinder {

        Variable findVariable(Group group, String variableName) throws IOException;
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
