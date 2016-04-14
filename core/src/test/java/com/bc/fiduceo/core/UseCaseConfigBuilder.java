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

import static com.bc.fiduceo.core.UseCaseConfig.ATTRIBUTE_NAME_NAME;
import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_DIMENSION;
import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_DIMENSIONS;
import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_MAX_PIXEL_DISTANCE_KM;
import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_NAME;
import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_NX;
import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_NY;
import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_OUTPUT_PATH;
import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_PRIMARY;
import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_ROOT;
import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_SENSOR;
import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_SENSORS;
import static com.bc.fiduceo.core.UseCaseConfig.TAG_NAME_TIME_DELTA_SECONDS;
import static com.bc.fiduceo.core.UseCaseConfig.load;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class UseCaseConfigBuilder {

    private Document document;

    public static UseCaseConfigBuilder build(String name) {
        final UseCaseConfigBuilder configBuilder = new UseCaseConfigBuilder();
        final Element rootElement = new Element(TAG_NAME_ROOT);
        rootElement.setAttribute(new Attribute(ATTRIBUTE_NAME_NAME, name != null ? name : "testName"));
        configBuilder.document = new Document(rootElement);
        return configBuilder;
    }

    public UseCaseConfigBuilder withTimeDeltaSeconds(int seconds) {
        addChild(getRootElement(), TAG_NAME_TIME_DELTA_SECONDS, seconds);
        return this;
    }

    public UseCaseConfigBuilder withMaxPixelDistanceKm(float distance) {
        addChild(getRootElement(), TAG_NAME_MAX_PIXEL_DISTANCE_KM, distance);
        return this;
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
                dimensionElem.setAttribute(new Attribute(ATTRIBUTE_NAME_NAME, dimension.getName()));
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
            }
        }
        return this;
    }

    public UseCaseConfigBuilder withOutputPath(String path) {
        addChild(getRootElement(), TAG_NAME_OUTPUT_PATH, path);
        return this;
    }

    public UseCaseConfig createConfig() {
        return load(getStream());
    }

    private Element getRootElement() {
        return document.getRootElement();
    }

    private Element addChild(Element element, String name, boolean value) {
        return addChild(element, name, Boolean.toString(value));
    }

    private Element addChild(Element element, String name, int value) {
        return addChild(element, name, Integer.toString(value));
    }

    private Element addChild(Element element, String name, float value) {
        return addChild(element, name, Float.toString(value));
    }

    private Element addChild(Element element, String name) {
        return addChild(element, name, "");
    }

    private Element addChild(Element element, String name, String value) {
        final Element child = new Element(name);
        child.setText(value);
        element.addContent(child);
        return child;
    }
}
