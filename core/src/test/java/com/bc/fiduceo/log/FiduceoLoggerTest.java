package com.bc.fiduceo.log;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FiduceoLoggerTest {
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
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Logger logger = FiduceoLogger.getLogger(Level.SEVERE);
        logger.severe("With Error message Line");
        String dateTimeNow = dateFormat.format(Calendar.getInstance().getTime()).toString() + " - SEVERE: com.bc.fiduceo.log.FiduceoLoggerTest - testLoggerMessageWithLineNumber - 36 - With Error message Line\n";
        assertEquals(dateTimeNow, FiduceoLoggerFormatter.getLogMessage());

    }

    @Test
    public void testLoggerMessageINFO() throws Exception {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Logger logger = FiduceoLogger.getLogger(Level.INFO);
        logger.info("Info about Fiduceo");
        String dateTimeNow = dateFormat.format(Calendar.getInstance().getTime()).toString() + " - INFO: com.bc.fiduceo.log.FiduceoLoggerTest - testLoggerMessageINFO - Info about Fiduceo\n";
        assertEquals(dateTimeNow, FiduceoLoggerFormatter.getLogMessage());
    }
}