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
import java.util.List;

@SuppressWarnings("unchecked")
public class MmdWriterConfig {

    private static final String ROOT_ELEMENT_TAG = "mmd-writer-config";
    private static final String OVERWRITE_TAG = "overwrite";
    private static final String CACHE_SIZE_TAG = "cache-size";
    private static final String NETCDF_FORMAT_TAG = "netcdf-format";
    private static final String VARIABLES_CONFIGURATION_TAG = "variables-configuration";
    private static final String SENSORS_TAG = "sensors";
    private static final String RENAME_TAG = "rename";
    private static final String EXCLUDE_TAG = "exclude";
    private static final String NAMES_ATTRIBUTE = "names";
    private static final String SOURCE_NAME_ATTRIBUTE = "source-name";
    private static final String TARGET_NAME_ATTRIBUTE = "target-name";

    private boolean overwrite;
    private int cacheSize;
    private NetcdfType netcdfFormat;
    private VariablesConfiguration variablesConfiguration;

    public static MmdWriterConfig load(InputStream inputStream) {
        final SAXBuilder saxBuilder = new SAXBuilder();
        try {
            final Document document = saxBuilder.build(inputStream);
            return new MmdWriterConfig(document);
        } catch (JDOMException | IOException | RuntimeException e) {
            throw new RuntimeException("Unable to initialize use case configuration: " + e.getMessage(), e);
        }
    }

    MmdWriterConfig() {
        cacheSize = 2048;
        netcdfFormat = NetcdfType.N4;
        variablesConfiguration = new VariablesConfiguration();
    }

    void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    int getCacheSize() {
        return cacheSize;
    }

    void setNetcdfFormat(String netcdfFormat) {
        this.netcdfFormat = NetcdfType.valueOf(netcdfFormat);
    }

    NetcdfType getNetcdfFormat() {
        return netcdfFormat;
    }

    public VariablesConfiguration getVariablesConfiguration() {
        return variablesConfiguration;
    }

    private MmdWriterConfig(Document document) {
        this();
        init(document);
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
            final List<Element> sensorElements = variablesConfigurationElement.getChildren(SENSORS_TAG);
            for (final Element sensorElement : sensorElements) {
                final String sensorKeys = getAttributeString(NAMES_ATTRIBUTE, sensorElement);

                addVariableRenames(sensorElement, sensorKeys);
                addVariableExcludes(sensorElement, sensorKeys);
            }
        }
    }

    private void addVariableExcludes(Element sensorElement, String sensorKeys) {
        final ArrayList<VariableExclude> variableExcludes = new ArrayList<>();
        final List<Element> excludeElements = sensorElement.getChildren(EXCLUDE_TAG);
        for (final Element excludeElement : excludeElements) {
            final String sourceName = getAttributeString(SOURCE_NAME_ATTRIBUTE, excludeElement);
            variableExcludes.add(new VariableExclude(sourceName));
        }
        variablesConfiguration.addExcludes(sensorKeys, variableExcludes);
    }

    private void addVariableRenames(Element sensorElement, String sensorKeys) {
        final ArrayList<VariableRename> variableRenames = new ArrayList<>();
        final List<Element> renameElements = sensorElement.getChildren(RENAME_TAG);
        for (final Element renameElement : renameElements) {
            final String sourceName = getAttributeString(SOURCE_NAME_ATTRIBUTE, renameElement);
            final String targetName = getAttributeString(TARGET_NAME_ATTRIBUTE, renameElement);
            variableRenames.add(new VariableRename(sourceName, targetName));
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
