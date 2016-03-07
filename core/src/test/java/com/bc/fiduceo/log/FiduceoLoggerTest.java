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

package com.bc.fiduceo.log;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FiduceoLoggerTest {

    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Test
    public void testGetDefaultAndLevel() throws Exception {
        final Logger logger = FiduceoLogger.getLogger();
        assertNotNull(logger);
        assertEquals("com.bc.fiduceo.log", logger.getName());
        assertEquals(Level.INFO, FiduceoLogger.getDefaultLevel());
    }

    @Test
    public void testDefault_DebugSilent_Level() throws Exception {
        final Logger logger = FiduceoLogger.getLogger(Level.SEVERE);
        FiduceoLogger.setLevelDebug();
        assertEquals(Level.ALL, logger.getLevel());

        FiduceoLogger.setLevelSilent();
        assertEquals(Level.WARNING, logger.getLevel());
    }

    @Test
    public void testLoggerMessageWithLineNumber() throws Exception {
        Logger logger = FiduceoLogger.getLogger(Level.SEVERE);
        logger.severe("With Error message Line");
        String dateTimeNow = dateFormat.format(Calendar.getInstance().getTime()).toString() + " - SEVERE: com.bc.fiduceo.log.FiduceoLoggerTest - testLoggerMessageWithLineNumber - 58 - With Error message Line\n";

        assertEquals(dateTimeNow, FiduceoLoggerFormatter.getLogMessage());
    }

    @Test
    public void testLoggerMessageINFO() throws Exception {
        Logger logger = FiduceoLogger.getLogger(Level.INFO);
        logger.info("Info about Fiduceo");
        String dateTimeNow = dateFormat.format(Calendar.getInstance().getTime()).toString() + " - INFO: com.bc.fiduceo.log.FiduceoLoggerTest - testLoggerMessageINFO - Info about Fiduceo\n";

        assertEquals(dateTimeNow, FiduceoLoggerFormatter.getLogMessage());
    }
}