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

import com.bc.fiduceo.archive.ArchiveConfig;
import com.bc.fiduceo.util.JDomUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SystemConfig {

    private String geometryLibraryType;
    private ArchiveConfig archiveConfig;

    public static SystemConfig loadFrom(File configDirectory) throws IOException {
        final File systemPropertiesFile = new File(configDirectory, "system-config.xml");
        if (!systemPropertiesFile.isFile()) {
            throw new RuntimeException("Configuration file not found: " + systemPropertiesFile.getAbsolutePath());
        }

        try (FileInputStream inputStream = new FileInputStream(systemPropertiesFile)) {
            return load(inputStream);
        }
    }

    public static SystemConfig load(InputStream inputStream) {
        final SAXBuilder saxBuilder = new SAXBuilder();
        try {
            final Document document = saxBuilder.build(inputStream);
            return new SystemConfig(document);
        } catch (JDOMException | IOException | RuntimeException e) {
            throw new RuntimeException("Unable to initialize use case configuration: " + e.getMessage(), e);
        }
    }

    public SystemConfig() {
        geometryLibraryType = "S2";
    }

    public String getGeometryLibraryType() {
        return geometryLibraryType;
    }

    public ArchiveConfig getArchiveConfig() {
        return archiveConfig;
    }

    private SystemConfig(Document document) {
        this();

        final Element rootElement = JDomUtils.getMandatoryRootElement("system-config", document);
        final Element geometryLibraryElement = rootElement.getChild("geometry-library");
        if (geometryLibraryElement != null) {
            geometryLibraryType = JDomUtils.getValueFromNameAttributeMandatory(geometryLibraryElement);
        }

        final Element archiveConfigElement = rootElement.getChild("archive");
        if (archiveConfigElement != null) {
            archiveConfig = new ArchiveConfig(archiveConfigElement);
        }
    }
}
