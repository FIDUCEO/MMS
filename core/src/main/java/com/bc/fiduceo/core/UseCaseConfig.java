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

import com.thoughtworks.xstream.XStream;
import org.esa.snap.core.util.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class UseCaseConfig {

    private String name;
    private List<Sensor> sensors;
    private List<Dimension> dimensions;
    private int timeDelta;
    private String outputPath;
    private float maxPixelDistance;

    public static UseCaseConfig load(InputStream inputStream) {
        final XStream xStream = createXStream();
        return (UseCaseConfig) xStream.fromXML(inputStream);
    }

    public void store(OutputStream outputStream) {
        final XStream xStream = createXStream();
        xStream.toXML(this, outputStream);
    }

    public UseCaseConfig() {
        sensors = new ArrayList<>();
        dimensions = new ArrayList<>();
        timeDelta = -1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
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

    public int getTimeDelta() {
        return timeDelta;
    }

    public void setTimeDelta(int timeDelta) {
        this.timeDelta = timeDelta;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    public Dimension getDimensionFor(String sensorName) {
        for (Dimension dimension : dimensions) {
            if (dimension.getName().equals(sensorName)) {
                return dimension;
            }
        }
        throw new IllegalStateException("Dimensions for Sensor '" +sensorName + "' not available");
    }

    public void setDimensions(List<Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    public void setMaxPixelDistance(float maxPixelDistance) {
        this.maxPixelDistance = maxPixelDistance;
    }

    public float getMaxPixelDistance() {
        return maxPixelDistance;
    }

    public ValidationResult checkValid() {
        final ValidationResult validationResult = new ValidationResult();
        if (StringUtils.isNullOrEmpty(name)) {
            validationResult.setValid(false);
            validationResult.addMessage("Use case name not configured.");
        }
        if (timeDelta < 0) {
            validationResult.setValid(false);
            validationResult.addMessage("Matchup time delta not configured.");
        }
        return validationResult;
    }

    private static XStream createXStream() {
        final XStream xStream = new XStream();
        xStream.alias("use-case-config", UseCaseConfig.class);
        xStream.useAttributeFor(UseCaseConfig.class, "name");
        xStream.aliasField("time-delta", UseCaseConfig.class, "timeDelta");
        xStream.aliasField("output-path", UseCaseConfig.class, "outputPath");
        xStream.alias("sensor", Sensor.class);

        xStream.alias("dimension", Dimension.class);
        xStream.useAttributeFor(Dimension.class, "name");

        return xStream;
    }
}
