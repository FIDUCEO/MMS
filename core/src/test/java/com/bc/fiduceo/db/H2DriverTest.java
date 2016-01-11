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

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class H2DriverTest {

    @Test
    public void testCreateSql_noParameter() {
        final String sql = H2Driver.createSql(null);

        assertEquals("SELECT * FROM SATELLITE_OBSERVATION", sql);
    }

    @Test
    public void testCreateSql_startTime() {
        final QueryParameter parameter = new QueryParameter();
        final Date startDate = new Date(1200000000000L);
        parameter.setStartTime(startDate);

        final String sql = H2Driver.createSql(parameter);

        assertEquals("SELECT * FROM SATELLITE_OBSERVATION WHERE stopDate >= '2008-01-10 21:20:00.0'", sql);
    }

    @Test
    public void testCreateSql_stopTime() {
        final QueryParameter parameter = new QueryParameter();
        parameter.setStopTime(new Date(1210000000000L));

        final String sql = H2Driver.createSql(parameter);

        assertEquals("SELECT * FROM SATELLITE_OBSERVATION WHERE startDate <= '2008-05-05 15:06:40.0'", sql);
    }

    @Test
    public void testCreateSql_startAndStopTime() {
        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(new Date(1220000000000L));
        parameter.setStopTime(new Date(1230000000000L));

        final String sql = H2Driver.createSql(parameter);

        assertEquals("SELECT * FROM SATELLITE_OBSERVATION WHERE stopDate >= '2008-08-29 08:53:20.0' AND startDate <= '2008-12-23 02:40:00.0'", sql);
    }
}
