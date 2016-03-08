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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class UseCaseConfig {

    private String name;
    private List<Sensor> sensors;
    private int timeDelta;

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

    public Sensor getPrimarySensor() {
        for (final Sensor sensor : sensors) {
            if (sensor.isPrimary()) {
                return sensor;
            }
        }
        return null;
    }

    public int getTimeDelta() {
        return timeDelta;
    }

    public void setTimeDelta(int timeDelta) {
        this.timeDelta = timeDelta;
    }

    private static XStream createXStream() {
        final XStream xStream = new XStream();
        xStream.alias("use-case-config", UseCaseConfig.class);
        xStream.useAttributeFor(UseCaseConfig.class, "name");
        xStream.aliasField("time-delta", UseCaseConfig.class, "timeDelta");
        xStream.alias("sensor", Sensor.class);
        return xStream;
    }


}
