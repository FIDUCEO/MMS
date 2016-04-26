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

package com.bc.fiduceo.core;

import org.esa.snap.core.util.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.util.JDomUtils.mandatory_getAttribute;
import static com.bc.fiduceo.util.JDomUtils.mandatory_getChild;
import static com.bc.fiduceo.util.JDomUtils.mandatory_getChildTextTrim;
import static com.bc.fiduceo.util.JDomUtils.mandatory_getRootElement;

public class UseCaseConfig {

    public static final String TAG_NAME_ROOT = "use-case-config";
    // @todo se move the following two tag names
    public static final String TAG_NAME_TIME_DELTA_SECONDS = "time-delta-seconds";
    public static final String TAG_NAME_MAX_PIXEL_DISTANCE_KM = "max-pixel-distance-km";
    public static final String TAG_NAME_OUTPUT_PATH = "output-path";
    public static final String TAG_NAME_SENSORS = "sensors";
    public static final String TAG_NAME_SENSOR = "sensor";
    public static final String TAG_NAME_PRIMARY = "primary";
    public static final String TAG_NAME_DIMENSIONS = "dimensions";
    public static final String TAG_NAME_DIMENSION = "dimension";
    public static final String TAG_NAME_NX = "nx";
    public static final String TAG_NAME_NY = "ny";
    public static final String TAG_NAME_NAME = "name";
    public static final String ATTRIBUTE_NAME_NAME = "name";

    transient private final Document document;
    private String name;
    private List<Sensor> sensors;
    private List<Dimension> dimensions;
    private int timeDeltaSeconds;
    private String outputPath;

    private UseCaseConfig(Document document) {
        sensors = new ArrayList<>();
        dimensions = new ArrayList<>();
        timeDeltaSeconds = -1;
        this.document = document;
        init();
    }

    public static UseCaseConfig load(InputStream inputStream) {
        final SAXBuilder saxBuilder = new SAXBuilder();
        try {
            final Document document = saxBuilder.build(inputStream);
            return new UseCaseConfig(document);
        } catch (JDOMException | IOException | RuntimeException e) {
            throw new RuntimeException("Unable to initialize use case configuration.", e);
        }
    }

    public void store(OutputStream outputStream) throws IOException {
        new XMLOutputter(Format.getPrettyFormat()).output(document, outputStream);
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public Element getDomElement(String elemName) {
        return document.getRootElement().getChild(elemName);
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    /**
     * Retrieves the primary Sensor for this use-case.
     *
     * @return the primary Sensor or null if none has been configured
     */
    public Sensor getPrimarySensor() {
        for (final Sensor sensor : sensors) {
            if (sensor.isPrimary()) {
                return sensor;
            }
        }
        return null;
    }

    public List<Sensor> getAdditionalSensors() {
        final ArrayList<Sensor> additionalSensorList = new ArrayList<>();
        for (final Sensor sensor : sensors) {
            if (!sensor.isPrimary()) {
                additionalSensorList.add(sensor);
            }
        }
        return additionalSensorList;
    }

    public int getTimeDeltaSeconds() {
        return timeDeltaSeconds;
    }

    void setTimeDeltaSeconds(int timeDeltaSeconds) {
        this.timeDeltaSeconds = timeDeltaSeconds;
    }

    public String getOutputPath() {
        return outputPath;
    }

    void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    void setDimensions(List<Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    public Dimension getDimensionFor(String sensorName) {
        for (Dimension dimension : dimensions) {
            if (dimension.getName().equals(sensorName)) {
                return dimension;
            }
        }
        throw new IllegalStateException("Dimensions for Sensor '" + sensorName + "' not available");
    }

    public ValidationResult checkValid() {
        final ValidationResult validationResult = new ValidationResult();
        if (StringUtils.isNullOrEmpty(name)) {
            setInvalidWithMessage("Use case name not configured.", validationResult);
        }
        if (timeDeltaSeconds < 0) {
            setInvalidWithMessage("Matchup time delta not configured.", validationResult);
        }
        if (getPrimarySensor() == null) {
            setInvalidWithMessage("Primary sensor not configured.", validationResult);
        }
        if (getAdditionalSensors().size() == 0) {
            setInvalidWithMessage("No additional sensor configured.", validationResult);
        }
        final List<Sensor> sensors = getSensors();
        for (final Sensor sensor : sensors) {
            // @todo 3 tb/** no good style, improve this 2016-03-17
            try {
                getDimensionFor(sensor.getName());
            } catch (IllegalStateException e) {
                setInvalidWithMessage("No dimensions for sensor '" + sensor.getName() + "' configured.", validationResult);
            }
        }
        if (StringUtils.isNullOrEmpty(outputPath)) {
            setInvalidWithMessage("Output path not configured.", validationResult);
        }
        return validationResult;
    }

    private void init() {
        final Element rootElement = mandatory_getRootElement(document);
        setName(mandatory_getAttribute(rootElement, ATTRIBUTE_NAME_NAME).getValue());
        final Element conditions = rootElement.getChild("conditions");
        if (conditions != null) {
            final Element timeDelta = conditions.getChild("time-delta");
            if (timeDelta != null) {
                final String trimmed = mandatory_getChildTextTrim(timeDelta, TAG_NAME_TIME_DELTA_SECONDS);
                setTimeDeltaSeconds(Integer.valueOf(trimmed));
            }
        }
        final Element outputPath = rootElement.getChild(TAG_NAME_OUTPUT_PATH);
        if (outputPath != null) {
            setOutputPath(outputPath.getValue());
        }
        final Element sensors = rootElement.getChild(TAG_NAME_SENSORS);
        if (sensors != null) {
            final List<Element> sensorList = sensors.getChildren(TAG_NAME_SENSOR);
            for (Element sensorElem : sensorList) {
                final Element name = mandatory_getChild(sensorElem, TAG_NAME_NAME);
                final Sensor sensor = new Sensor(name.getValue());
                final Element primary = sensorElem.getChild(TAG_NAME_PRIMARY);
                if (primary != null) {
                    sensor.setPrimary(Boolean.valueOf(primary.getValue()));
                }
                getSensors().add(sensor);
            }
        }
        final Element dimensions = rootElement.getChild(TAG_NAME_DIMENSIONS);
        if (dimensions != null) {
            final List<Element> dimensionList = dimensions.getChildren(TAG_NAME_DIMENSION);
            for (Element dimensionElem : dimensionList) {
                final String name = mandatory_getAttribute(dimensionElem, ATTRIBUTE_NAME_NAME).getValue();
                final int nx = Integer.valueOf(mandatory_getChild(dimensionElem, TAG_NAME_NX).getValue());
                final int ny = Integer.valueOf(mandatory_getChild(dimensionElem, TAG_NAME_NY).getValue());
                getDimensions().add(new Dimension(name, nx, ny));
            }
        }
    }

    private void setInvalidWithMessage(String message, ValidationResult validationResult) {
        validationResult.setValid(false);
        validationResult.addMessage(message);
    }
}
