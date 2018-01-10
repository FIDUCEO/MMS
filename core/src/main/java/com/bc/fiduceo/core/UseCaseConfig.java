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

import static com.bc.fiduceo.util.JDomUtils.getMandatoryChild;
import static com.bc.fiduceo.util.JDomUtils.getMandatoryRootElement;
import static com.bc.fiduceo.util.JDomUtils.getMandatoryText;
import static com.bc.fiduceo.util.JDomUtils.getValueFromNameAttributeMandatory;

public class UseCaseConfig {

    public static final String TAG_NAME_ROOT = "use-case-config";
    static final String TAG_NAME_OUTPUT_PATH = "output-path";
    static final String TAG_NAME_WRITE_DISTANCE = "write-distance";
    public static final String TAG_NAME_SENSORS = "sensors";
    public static final String TAG_NAME_SENSOR = "sensor";
    static final String TAG_NAME_RANDOM_POINTS_PER_DAY = "random-points-per-day";
    public static final String TAG_NAME_PRIMARY = "primary";
    static final String TAG_NAME_DATA_VERSION = "data-version";
    static final String TAG_NAME_DIMENSIONS = "dimensions";
    static final String TAG_NAME_DIMENSION = "dimension";
    static final String TAG_NAME_TEST_RUN = "test-run";
    private static final String TAG_NAME_LOCATION = "location";
    private static final String TAG_NAME_LON = "lon";
    private static final String TAG_NAME_LAT = "lat";
    static final String TAG_NAME_NX = "nx";
    static final String TAG_NAME_NY = "ny";
    public static final String TAG_NAME_NAME = "name";

    transient private Document document;
    private String name;
    private List<Sensor> sensors;
    private List<Dimension> dimensions;
    private String outputPath;
    private boolean writeDistance;
    private int randomPointsPerDay;
    private boolean testRun;
    private double lon;
    private double lat;

    public UseCaseConfig() {
        sensors = new ArrayList<>();
        dimensions = new ArrayList<>();
        testRun = false;
        lon = Double.NaN;
        lat = Double.NaN;
    }

    public static UseCaseConfig load(InputStream inputStream) {
        final SAXBuilder saxBuilder = new SAXBuilder();
        try {
            final Document document = saxBuilder.build(inputStream);
            return new UseCaseConfig(document);
        } catch (JDOMException | IOException | RuntimeException e) {
            throw new RuntimeException("Unable to initialize use case configuration: " + e.getMessage(), e);
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

    public int getRandomPointsPerDay() {
        return randomPointsPerDay;
    }

    private void setRandomPointsPerDay(int randomPointsPerDay) {
        this.randomPointsPerDay = randomPointsPerDay;
    }

    protected List<Sensor> getSensors() {
        return sensors;
    }

    protected void setSensors(List<Sensor> sensors) {
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

    public List<Sensor> getSecondarySensors() {
        final ArrayList<Sensor> additionalSensorList = new ArrayList<>();
        for (final Sensor sensor : sensors) {
            if (!sensor.isPrimary()) {
                additionalSensorList.add(sensor);
            }
        }
        return additionalSensorList;
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

    public void setDimensions(List<Dimension> dimensions) {
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

    public boolean isWriteDistance() {
        return writeDistance;
    }

    void setWriteDistance(boolean writeDistance) {
        this.writeDistance = writeDistance;
    }

    public boolean isTestRun() {
        return testRun;
    }

    public void setTestRun(boolean testRun) {
        this.testRun = testRun;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public boolean hasLocation() {
        return !(Double.isNaN(lon) || Double.isNaN(lat));
    }

    public ValidationResult checkValid() {
        final ValidationResult validationResult = new ValidationResult();
        if (StringUtils.isNullOrEmpty(name)) {
            setInvalidWithMessage("Use case name not configured.", validationResult);
        }
        int primaryCount = 0;
        for (Sensor sensor : sensors) {
            if (sensor.isPrimary()) {
                primaryCount++;
            }
        }
        if (primaryCount > 1) {
            setInvalidWithMessage("More than one primary sensor configured.", validationResult);
        }
        if (primaryCount < 1) {
            setInvalidWithMessage("Primary sensor not configured.", validationResult);
        }
        if (getSecondarySensors().size() == 0) {
            if (!hasLocation()) {
                setInvalidWithMessage("No additional sensor configured.", validationResult);
            }
        }
        for (final Sensor sensor : sensors) {
            if (!hasDimensionFor(sensor.getName())) {
                setInvalidWithMessage("No dimensions for sensor '" + sensor.getName() + "' configured.", validationResult);
            }
        }
        if (StringUtils.isNullOrEmpty(outputPath)) {
            setInvalidWithMessage("Output path not configured.", validationResult);
        }
        return validationResult;
    }

    public Element getDomElement(String elemName) {
        return document.getRootElement().getChild(elemName);
    }

    boolean hasDimensionFor(String sensorName) {
        for (Dimension dimension : dimensions) {
            if (dimension.getName().equals(sensorName)) {
                return true;
            }
        }
        return false;
    }

    private UseCaseConfig(Document document) {
        this();
        this.document = document;
        init();
    }

    @SuppressWarnings("unchecked")
    private void init() {
        final Element rootElement = getMandatoryRootElement(UseCaseConfig.TAG_NAME_ROOT, document);
        setName(getValueFromNameAttributeMandatory(rootElement));

        final Element outputPath = rootElement.getChild(TAG_NAME_OUTPUT_PATH);
        if (outputPath != null) {
            setOutputPath(outputPath.getValue());
        }

        final Element sensorsElem = rootElement.getChild(TAG_NAME_SENSORS);
        if (sensorsElem != null) {
            final List<Element> sensorList = sensorsElem.getChildren(TAG_NAME_SENSOR);
            for (Element sensorElem : sensorList) {
                final Element name = getMandatoryChild(sensorElem, TAG_NAME_NAME);
                final Sensor sensor = new Sensor(name.getValue());
                final Element primary = sensorElem.getChild(TAG_NAME_PRIMARY);
                if (primary != null) {
                    sensor.setPrimary(Boolean.valueOf(primary.getValue()));
                }
                final Element dataVersionElement = sensorElem.getChild(TAG_NAME_DATA_VERSION);
                if (dataVersionElement != null) {
                    sensor.setDataVersion(dataVersionElement.getValue());
                }
                sensors.add(sensor);
            }
        }

        final Element dimensions = rootElement.getChild(TAG_NAME_DIMENSIONS);
        if (dimensions != null) {
            final List<Element> dimensionList = dimensions.getChildren(TAG_NAME_DIMENSION);
            for (Element dimensionElem : dimensionList) {
                final String name = getValueFromNameAttributeMandatory(dimensionElem);
                final int nx = Integer.valueOf(getMandatoryChild(dimensionElem, TAG_NAME_NX).getValue());
                final int ny = Integer.valueOf(getMandatoryChild(dimensionElem, TAG_NAME_NY).getValue());
                getDimensions().add(new Dimension(name, nx, ny));
            }
        }

        final Element writeDistanceElement = rootElement.getChild(TAG_NAME_WRITE_DISTANCE);
        if (writeDistanceElement != null) {
            final boolean writeDistance = Boolean.parseBoolean(writeDistanceElement.getValue());
            setWriteDistance(writeDistance);
        }

        final Element seedPointsElem = rootElement.getChild(TAG_NAME_RANDOM_POINTS_PER_DAY);
        if (seedPointsElem != null) {
            setRandomPointsPerDay(getMandatoryPositiveIntegerValue(seedPointsElem));
        }

        final Element testRunElem = rootElement.getChild(TAG_NAME_TEST_RUN);
        if (testRunElem != null) {
            testRun = true;
        }

        final Element locationElement = rootElement.getChild(TAG_NAME_LOCATION);
        if (locationElement != null) {
            final Element lonElement = locationElement.getChild(TAG_NAME_LON);
            if (lonElement != null) {
                final String lonText = getMandatoryText(lonElement);
                lon = Double.parseDouble(lonText);
            }

            final Element latElement = locationElement.getChild(TAG_NAME_LAT);
            if (latElement != null) {
                final String latText = getMandatoryText(latElement);
                lat = Double.parseDouble(latText);
            }
        }
    }

    private int getMandatoryPositiveIntegerValue(Element seedPointsElem) {
        final String text = getMandatoryText(seedPointsElem);
        final int i;
        try {
            i = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Integer value of element '" + seedPointsElem.getName() + "' >= 1 expected. But was '" + text + "'.", e);
        }
        if (i < 1) {
            throw new RuntimeException("Value of element '" + seedPointsElem.getName() + "' >= 1 expected. But was '" + text + "'.");
        }
        return i;
    }

    private void setInvalidWithMessage(String message, ValidationResult validationResult) {
        validationResult.setValid(false);
        validationResult.addMessage(message);
    }
}
