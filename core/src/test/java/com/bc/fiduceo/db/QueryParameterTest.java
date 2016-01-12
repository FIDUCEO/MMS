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

public class QueryParameterTest {

    @Test
    public void testSetGetStartTime(){
        final Date startTime = new Date(100000000L);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(startTime);
        assertEquals(startTime.getTime(), parameter.getStartTime().getTime());
    }


    @Test
    public void testSetGetStopTime(){
        final Date stopTime = new Date(100200000L);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStopTime(stopTime);
        assertEquals(stopTime.getTime(), parameter.getStopTime().getTime());
    }
}
