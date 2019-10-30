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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author muhammad.bc
 */
class FiduceoLoggerFormatter extends Formatter {

    private static StringBuilder logMessage = null;

    @Override
    public String format(LogRecord record) {
        logMessage = new StringBuilder();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        logMessage.append(dateFormat.format(new Date(record.getMillis())));
        logMessage.append(" - ");

        logMessage.append(record.getLevel().getName());
        logMessage.append(": ");

        logMessage.append(record.getSourceClassName());
        logMessage.append(" - ");

        logMessage.append(record.getSourceMethodName());
        logMessage.append(" - ");
        if (record.getLevel() == Level.SEVERE) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if (stackTraceElement.getClassName().equals(record.getSourceClassName())) {
                    logMessage.append(stackTraceElement.getLineNumber());
                    break;
                }
            }
            logMessage.append(" - ");
        }
        logMessage.append(record.getMessage());
        logMessage.append("\n");

        final Throwable thrown = record.getThrown();
        if (thrown != null) {
            logMessage.append(thrown.toString());
            logMessage.append("\n");
        }
        return logMessage.toString();
    }

    //Only for testing purpose
    static String getLogMessage() {
        return logMessage.toString();
    }
}
