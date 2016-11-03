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

package com.bc.fiduceo.archive;


import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom2.Parent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class ArchiveConfig {
    // @todo 2 tb/tb use JDOMUtils here 2016-11-02

    private static final String ROOT_ELEMENT_TAG = "archive";
    private static final String ROOT_PATH_TAG = "root-path";
    private static final String RULE_TAG = "rule";

    private Path rootPath;
    private Map<String, String[]> rules;

    public Path getRootPath() {
        return rootPath;
    }

    public Map<String, String[]> getRules() {
        return rules;
    }

    public static ArchiveConfig parse(String xml) {
        final SAXBuilder saxBuilder = new SAXBuilder();
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());

        try {
            final Document document = saxBuilder.build(inputStream);
            return new ArchiveConfig(document);
        } catch (JDOMException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    ArchiveConfig() {
        rules = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    ArchiveConfig(Document document) {
      this(document.getRootElement());
    }

    public ArchiveConfig(Element element) {
        this();
        final String name = element.getName();
        if (!ROOT_ELEMENT_TAG.equals(name)) {
            throw new RuntimeException("Root tag name '" + ROOT_ELEMENT_TAG + "' expected");
        }

        parseRootPath(element);
        parseRules(element);
    }

    void setRootPath(Path rootPath) {
        this.rootPath = rootPath;
    }

    void setRules(Map<String, String[]> rules) {
        this.rules = rules;
    }

    @SuppressWarnings("unchecked")
    private void parseRules(Element rootElement) {
        final List<Element> ruleElementsList = rootElement.getChildren(RULE_TAG);
        for (final Element ruleElement : ruleElementsList) {
            final Attribute sensorsAttribute = ruleElement.getAttribute("sensors");
            if (sensorsAttribute == null) {
                throw new RuntimeException("Sensors not configured for archive rule");
            }

            final String[] pathElements = parsePathElements(ruleElement);

            assignRules(pathElements, sensorsAttribute);
        }
    }

    private void assignRules(String[] pathElements, Attribute sensorsAttribute) {
        final String sensorsAttributeValue = sensorsAttribute.getValue();
        final StringTokenizer stringTokenizer = new StringTokenizer(sensorsAttributeValue.trim(), ",", false);
        while (stringTokenizer.hasMoreTokens()) {
            final String sensorName = stringTokenizer.nextToken().trim();

            rules.put(sensorName, pathElements);
        }
    }

    private String[] parsePathElements(Element ruleElement) {
        final String ruleElementValue = ruleElement.getValue();
        if (ruleElementValue == null || ruleElementValue.trim().isEmpty()) {
            throw new RuntimeException("Archive root path not configured, element '" + ROOT_PATH_TAG + "' is empty");
        }

        final StringTokenizer stringTokenizer = new StringTokenizer(ruleElementValue.trim(), "/", false);
        final ArrayList<String> elementsList = new ArrayList<>();
        while (stringTokenizer.hasMoreTokens()) {
            final String token = stringTokenizer.nextToken().trim();
            elementsList.add(token);
        }

        return elementsList.toArray(new String[elementsList.size()]);
    }

    private void parseRootPath(Element rootElement) {
        final Element rootPathElement = rootElement.getChild(ROOT_PATH_TAG);
        if (rootPathElement != null) {
            final String rootPathValue = rootPathElement.getValue();
            if (rootPathValue == null || rootPathValue.trim().isEmpty()) {
                throw new RuntimeException("Archive root path not configured, element '" + ROOT_PATH_TAG + "' is empty");
            }
            rootPath = Paths.get(rootPathValue.trim());
        } else {
            throw new RuntimeException("Archive root path not configured, missing element '" + ROOT_PATH_TAG + "'");
        }
    }
}
