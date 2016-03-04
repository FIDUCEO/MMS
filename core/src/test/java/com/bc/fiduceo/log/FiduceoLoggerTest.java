package com.bc.fiduceo.log;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FiduceoLoggerTest {
    @Test
    public void testGetDefaultAndLevel() throws Exception {
        final Logger logger = FiduceoLogger.getLogger();
        assertNotNull(logger);
        assertEquals("com.bc.fiduceo.log", logger.getName());
        assertEquals(LogLevel.INFO, FiduceoLogger.getDefaultLevel());
    }

    @Test
    public void testDefault_DebugSilent_Level() throws Exception {
        final Logger logger = FiduceoLogger.getLogger(LogLevel.ERROR);
        FiduceoLogger.setLevelDebug();
        assertEquals(LogLevel.ALL.getValue(), logger.getLevel());

        FiduceoLogger.setLevelSilent();
        assertEquals(LogLevel.WARNING.getValue(), logger.getLevel());
    }

    @Test
    public void testLoggerMessageWithNumberLine() throws Exception {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Logger logger = FiduceoLogger.getLogger(LogLevel.ERROR);
        logger.severe("With Error message Line");
        String dateTimeNow = dateFormat.format(Calendar.getInstance().getTime()).toString() + " - SEVERE: com.bc.fiduceo.log.FiduceoLoggerTest - testLoggerMessageWithNumberLine - 35 - With Error message Line\n";
        assertEquals(dateTimeNow, FiduceoLogger.getLogMessage());

    }
}