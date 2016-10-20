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

package com.bc.fiduceo.matchup.writer;


import static java.io.File.separator;

import com.bc.fiduceo.matchup.writer.MmdWriterFactory.NetcdfType;
import org.esa.snap.core.util.StringUtils;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class MmdWriterConfig {

    private static final String ROOT_ELEMENT_TAG = "mmd-writer-config";
    private static final String OVERWRITE_TAG = "overwrite";
    private static final String CACHE_SIZE_TAG = "cache-size";
    private static final String NETCDF_FORMAT_TAG = "netcdf-format";
    private static final String VARIABLES_CONFIGURATION_TAG = "variables-configuration";
    private static final String SENSOR_RENAME_TAG = "sensor-rename";
    private static final String SEPARATOR = "separator";
    private static final String SENSORS_TAG = "sensors";
    private static final String RENAME_TAG = "rename";
    private static final String EXCLUDE_TAG = "exclude";

    private static final String SEPARATOR_ATTRIBUTE = "separator";
    private static final String SENSOR_NAME_ATTRIBUTE = "sensor-name";
    private static final String NAMES_ATTRIBUTE = "names";
    private static final String SOURCE_NAME_ATTRIBUTE = "source-name";
    private static final String TARGET_NAME_ATTRIBUTE = "target-name";

    private boolean overwrite;
    private int cacheSize;
    private NetcdfType netcdfFormat;
    private VariablesConfiguration variablesConfiguration;

    MmdWriterConfig() {
        cacheSize = 2048;
        netcdfFormat = NetcdfType.N4;
        variablesConfiguration = new VariablesConfiguration();
    }

    private MmdWriterConfig(Document document) {
        this();
        init(document);
    }

    public static MmdWriterConfig load(InputStream inputStream) {
        final SAXBuilder saxBuilder = new SAXBuilder();
        try {
            final Document document = saxBuilder.build(inputStream);
            return new MmdWriterConfig(document);
        } catch (JDOMException | IOException | RuntimeException e) {
            throw new RuntimeException("Unable to initialize use case configuration: " + e.getMessage(), e);
        }
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public VariablesConfiguration getVariablesConfiguration() {
        return variablesConfiguration;
    }

    int getCacheSize() {
        return cacheSize;
    }

    void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    NetcdfType getNetcdfFormat() {
        return netcdfFormat;
    }

    void setNetcdfFormat(String netcdfFormat) {
        this.netcdfFormat = NetcdfType.valueOf(netcdfFormat);
    }

    private void init(Document document) {
        final Element rootElement = document.getRootElement();
        final String name = rootElement.getName();
        if (!ROOT_ELEMENT_TAG.equals(name)) {
            throw new RuntimeException("Root tag name '" + ROOT_ELEMENT_TAG + "' expected");
        }

        final Element overwriteElement = rootElement.getChild(OVERWRITE_TAG);
        if (overwriteElement != null) {
            final String overwriteValue = overwriteElement.getValue();
            overwrite = Boolean.valueOf(overwriteValue);
        }

        final Element cacheSizeElement = rootElement.getChild(CACHE_SIZE_TAG);
        if (cacheSizeElement != null) {
            final String cacheSizeValue = cacheSizeElement.getValue();
            cacheSize = Integer.valueOf(cacheSizeValue);
        }

        final Element netcdfFormatElement = rootElement.getChild(NETCDF_FORMAT_TAG);
        if (netcdfFormatElement != null) {
            final String netcdfFormatValue = netcdfFormatElement.getValue();
            setNetcdfFormat(netcdfFormatValue);
        }

        final Element variablesConfigurationElement = rootElement.getChild(VARIABLES_CONFIGURATION_TAG);
        if (variablesConfigurationElement != null) {
            addSensorRenames(variablesConfigurationElement);
            configureSeparator(variablesConfigurationElement);
            final List<Element> sensorElements = variablesConfigurationElement.getChildren(SENSORS_TAG);
            for (final Element sensorElement : sensorElements) {
                addVariableRenames(sensorElement);
                addVariableExcludes(sensorElement);
            }
        }
    }

    private void configureSeparator(Element variablesConfigurationElement) {
        final List<Element> separatorElems = variablesConfigurationElement.getChildren(SEPARATOR);
        if (separatorElems == null || separatorElems.size() == 0) {
            return;
        }
        final String defaultSeparator = VariablesConfiguration.DEFAULT_SEPARATOR;
        for (Element separatorElem : separatorElems) {
            final String sensorName = getAttributeString(SENSOR_NAME_ATTRIBUTE, separatorElem);
            final String separator = getAttributeString(SEPARATOR_ATTRIBUTE, separatorElem);
            if (defaultSeparator.equals(variablesConfiguration.getSeparator(sensorName))) {
                variablesConfiguration.setSeparator(sensorName, separator);
            } else {
                throw new RuntimeException("Separator for sensor '" + sensorName + "' is already set.");
            }
        }
    }

    private void addSensorRenames(Element variablesConfigurationElement) {
        final List<Element> sensorRenames = variablesConfigurationElement.getChildren(SENSOR_RENAME_TAG);
        for (Element element : sensorRenames) {
            final String sourceName = getAttributeString(SOURCE_NAME_ATTRIBUTE, element);
            final String targetName = getAttributeString(TARGET_NAME_ATTRIBUTE, element);
            variablesConfiguration.addSensorRename(sourceName, targetName);
        }
    }

    private void addVariableExcludes(Element sensorElement) {
        final String sensorKeys = getAttributeString(NAMES_ATTRIBUTE, sensorElement);

        final ArrayList<String> variableExcludes = new ArrayList<>();
        final List<Element> excludeElements = sensorElement.getChildren(EXCLUDE_TAG);
        for (final Element excludeElement : excludeElements) {
            final String sourceName = getAttributeString(SOURCE_NAME_ATTRIBUTE, excludeElement);
            variableExcludes.add(sourceName);
        }
        variablesConfiguration.addExcludes(sensorKeys, variableExcludes);
    }

    private void addVariableRenames(Element sensorElement) {
        final String sensorKeys = getAttributeString(NAMES_ATTRIBUTE, sensorElement);

        final Map<String, String> variableRenames = new HashMap<>();
        final List<Element> renameElements = sensorElement.getChildren(RENAME_TAG);
        for (final Element renameElement : renameElements) {
            final String sourceName = getAttributeString(SOURCE_NAME_ATTRIBUTE, renameElement);
            final String targetName = getAttributeString(TARGET_NAME_ATTRIBUTE, renameElement);
            variableRenames.put(sourceName, targetName);
        }

        variablesConfiguration.addRenames(sensorKeys, variableRenames);
    }

    private String getAttributeString(String attributeName, Element sensorElement) {
        final Attribute namesAttribute = sensorElement.getAttribute(attributeName);
        if (namesAttribute == null) {
            throw new RuntimeException("Missing attribute: " + attributeName);
        }
        final String sensorKeys = namesAttribute.getValue();
        if (StringUtils.isNullOrEmpty(sensorKeys)) {
            throw new RuntimeException("Empty attribute: " + attributeName);
        }
        return sensorKeys;
    }
}
