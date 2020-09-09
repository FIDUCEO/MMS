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
package com.bc.fiduceo.core;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.bc.fiduceo.core.UseCaseConfig.*;
import static com.bc.fiduceo.util.JDomUtils.setNameAttribute;

// @todo 2 tb/** write tests for this class 2016-09-20
public class UseCaseConfigBuilder {

    private Document document;

    public UseCaseConfigBuilder(String name) {
        final Element rootElement = new Element(TAG_NAME_ROOT);
        setNameAttribute(rootElement, name != null ? name : "testName");
        document = new Document(rootElement);
    }

    public static UseCaseConfigBuilder build(String name) {
        return new UseCaseConfigBuilder(name);
    }

    public InputStream getStream() {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            new XMLOutputter().output(document, out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
        }
        return null;
    }

    public UseCaseConfigBuilder withDimensions(List<Dimension> dimensionsList) {
        if (dimensionsList != null && dimensionsList.size() != 0) {
            final Element dimensions = addChild(getRootElement(), TAG_NAME_DIMENSIONS);
            for (Dimension dimension : dimensionsList) {
                final Element dimensionElem = addChild(dimensions, TAG_NAME_DIMENSION);
                setNameAttribute(dimensionElem, dimension.getName());
                addChild(dimensionElem, TAG_NAME_NX, dimension.getNx());
                addChild(dimensionElem, TAG_NAME_NY, dimension.getNy());
            }
        }
        return this;
    }

    public UseCaseConfigBuilder withSensors(List<Sensor> sensorList) {
        if (sensorList != null && sensorList.size() != 0) {
            final Element sensors = addChild(getRootElement(), TAG_NAME_SENSORS);
            for (Sensor sensor : sensorList) {
                final Element sensorElem = addChild(sensors, TAG_NAME_SENSOR);
                addChild(sensorElem, TAG_NAME_NAME, sensor.getName());
                addChild(sensorElem, TAG_NAME_PRIMARY, sensor.isPrimary());
                final String dataVersion = sensor.getDataVersion();
                if (dataVersion != null) {
                    addChild(sensorElem, TAG_NAME_DATA_VERSION, dataVersion);
                }
            }
        }
        return this;
    }

    public UseCaseConfigBuilder withOutputPath(String path) {
        addChild(getRootElement(), TAG_NAME_OUTPUT_PATH, path);
        return this;
    }

    public UseCaseConfigBuilder withSphericalDistanceVariable() {
        addChild(getRootElement(), TAG_NAME_WRITE_DISTANCE, "true");
        return this;
    }

    public UseCaseConfigBuilder withRandomPointsPerDay(int numRandomSeedPoints) {
        addChild(getRootElement(), TAG_NAME_RANDOM_POINTS_PER_DAY, numRandomSeedPoints);
        return this;
    }

    public UseCaseConfigBuilder withRandomPointsPerDay(int numRandomSeedPoints, String distribution) {
        final Element randomSamplingElement = addChild(getRootElement(), TAG_NAME_RANDOM_SAMPLING);
        addChild(randomSamplingElement, TAG_NAME_POINTS_PER_DAY, numRandomSeedPoints);

        if (!("FLAT".equals(distribution) || "COSINE_LAT".equals(distribution) || "INV_TRUNC_COSINE_LAT".equals(distribution))) {
            throw new IllegalArgumentException("Unsupported random distribution");
        }
        addChild(randomSamplingElement, TAG_NAME_DISTRIBUTION, distribution);
        return this;
    }

    public UseCaseConfigBuilder withTestRun() {
        addChild(getRootElement(), TAG_NAME_TEST_RUN, true);
        return this;
    }

    public UseCaseConfig createConfig() {
        return load(getStream());
    }

    protected Element ensureChild(Element element, String tagName) {
        Element child = element.getChild(tagName);
        if (child != null) {
            return child;
        }
        child = new Element(tagName);
        element.addContent(child);
        return child;
    }

    protected Element getRootElement() {
        return document.getRootElement();
    }

    protected Element addChild(Element element, String name, long value) {
        return addChild(element, name, Long.toString(value));
    }

    protected Element addChild(Element element, String name, float value) {
        return addChild(element, name, Float.toString(value));
    }

    protected void addAttribute(Element element, String attributeName, String attributeValue) {
        element.setAttribute(attributeName, attributeValue);
    }

    private Element addChild(Element element, String name, boolean value) {
        return addChild(element, name, Boolean.toString(value));
    }

    private Element addChild(Element element, String name, int value) {
        return addChild(element, name, Integer.toString(value));
    }

    protected Element addChild(Element element, String name, String value) {
        final Element child = new Element(name);
        child.setText(value);
        element.addContent(child);
        return child;
    }

    protected Element addChild(Element element, String name) {
        return addChild(element, name, "");
    }
}
