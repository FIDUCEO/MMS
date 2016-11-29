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

package com.bc.fiduceo.db;


import com.bc.fiduceo.util.TimeUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class AbstractDriverTest {

    @Test
    public void testCreateSql_noParameter() {
        final String sql = AbstractDriver.createSql(null);

        assertEquals("SELECT * FROM SATELLITE_OBSERVATION obs INNER JOIN SENSOR sen ON obs.SensorId = sen.ID INNER JOIN TIMEAXIS axis ON obs.ID = axis.ObservationId", sql);
    }

    @Test
    public void testCreateSql_startTime() {
        final QueryParameter parameter = new QueryParameter();
        final Date startDate = TimeUtils.create(1300000000000L);
        parameter.setStartTime(startDate);

        final String sql = AbstractDriver.createSql(parameter);

        assertEquals("SELECT * FROM SATELLITE_OBSERVATION obs INNER JOIN SENSOR sen ON obs.SensorId = sen.ID INNER JOIN TIMEAXIS axis ON obs.ID = axis.ObservationId WHERE obs.stopDate >= '2011-03-13 07:06:40.0'", sql);
    }

    @Test
    public void testCreateSql_stopTime() {
        final QueryParameter parameter = new QueryParameter();
        parameter.setStopTime(TimeUtils.create(1210000000000L));

        final String sql = AbstractDriver.createSql(parameter);

        assertEquals("SELECT * FROM SATELLITE_OBSERVATION obs INNER JOIN SENSOR sen ON obs.SensorId = sen.ID INNER JOIN TIMEAXIS axis ON obs.ID = axis.ObservationId WHERE obs.startDate <= '2008-05-05 15:06:40.0'", sql);
    }

    @Test
    public void testCreateSql_startAndStopTime() {
        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(TimeUtils.create(1320000000000L));
        parameter.setStopTime(TimeUtils.create(1330000000000L));

        final String sql = AbstractDriver.createSql(parameter);

        assertEquals("SELECT * FROM SATELLITE_OBSERVATION obs INNER JOIN SENSOR sen ON obs.SensorId = sen.ID INNER JOIN TIMEAXIS axis ON obs.ID = axis.ObservationId WHERE obs.stopDate >= '2011-10-30 18:40:00.0' AND obs.startDate <= '2012-02-23 12:26:40.0'", sql);
    }

    @Test
    public void testCreateSql_sensorName() {
        final QueryParameter parameter = new QueryParameter();
        parameter.setSensorName("fieberthermometer");

        final String sql = AbstractDriver.createSql(parameter);

        assertEquals("SELECT * FROM SATELLITE_OBSERVATION obs INNER JOIN SENSOR sen ON obs.SensorId = sen.ID INNER JOIN TIMEAXIS axis ON obs.ID = axis.ObservationId WHERE sen.Name = 'fieberthermometer'", sql);
    }

    @Test
    public void testCreateSql_sensorNameAndStartTime() {
        final QueryParameter parameter = new QueryParameter();
        parameter.setSensorName("sensing");
        parameter.setStartTime(TimeUtils.create(1250000000000L));

        final String sql = AbstractDriver.createSql(parameter);

        assertEquals("SELECT * FROM SATELLITE_OBSERVATION obs INNER JOIN SENSOR sen ON obs.SensorId = sen.ID INNER JOIN TIMEAXIS axis ON obs.ID = axis.ObservationId WHERE obs.stopDate >= '2009-08-11 14:13:20.0' AND sen.Name = 'sensing'", sql);
    }

    @Test
    public void testCreateSql_productPath() {
        final QueryParameter parameter = new QueryParameter();
        parameter.setPath("/whereever/i/lay/my/hat");

        final String sql = AbstractDriver.createSql(parameter);

        assertEquals("SELECT * FROM SATELLITE_OBSERVATION obs INNER JOIN SENSOR sen ON obs.SensorId = sen.ID INNER JOIN TIMEAXIS axis ON obs.ID = axis.ObservationId WHERE obs.DataFile = '/whereever/i/lay/my/hat'", sql);
    }

    @Test
    public void testCreateSql_version() {
        final QueryParameter parameter = new QueryParameter();
        parameter.setVersion("v2.0");

        final String sql = AbstractDriver.createSql(parameter);

        assertEquals("SELECT * FROM SATELLITE_OBSERVATION obs INNER JOIN SENSOR sen ON obs.SensorId = sen.ID INNER JOIN TIMEAXIS axis ON obs.ID = axis.ObservationId WHERE obs.Version = 'v2.0'", sql);
    }
}
