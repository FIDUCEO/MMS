/*
 * Copyright (c) 2014 Brockmann Consult GmbH (info@brockmann-consult.de)
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
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package com.bc.fiduceo.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class FiduceoLogger {
    private static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.INFO;

    private static Logger logger;
    private static String logMessagesForTestOnly ;

    public static LogLevel getDefaultLevel() {
        return DEFAULT_LOG_LEVEL;
    }

    public static Logger getLogger() {
        return getLogger(DEFAULT_LOG_LEVEL);
    }

    public static Logger getLogger(LogLevel logLevel) {
        if (logger == null) {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final StringBuilder logMessage = new StringBuilder();
            final Formatter formatter = new Formatter() {
                @Override
                public String format(LogRecord record) {

                    logMessage.append(dateFormat.format(new Date(record.getMillis())));
                    logMessage.append(" - ");

                    logMessage.append(record.getLevel().getName());
                    logMessage.append(": ");

                    logMessage.append(record.getSourceClassName());
                    logMessage.append(" - ");

                    logMessage.append(record.getSourceMethodName());
                    logMessage.append(" - ");
                    if (logger.getLevel() == Level.SEVERE) {
                        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                        for (StackTraceElement stackTraceElement : stackTrace) {
                            if (stackTraceElement.getClassName() == record.getSourceClassName()) {
                                logMessage.append(stackTraceElement.getLineNumber());
                                break;
                            }
                        }
                        logMessage.append(" - ");
                    }
                    logMessage.append(record.getMessage());
                    logMessage.append("\n");

                    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
                    final Throwable thrown = record.getThrown();
                    if (thrown != null) {
                        logMessage.append(thrown.toString());
                        logMessage.append("\n");
                    }
                    logMessagesForTestOnly = logMessage.toString();
                    return logMessage.toString();
                }
            };

            final ConsoleHandler handler = new ConsoleHandler();
            handler.setFormatter(formatter);
            handler.setLevel(Level.ALL);

            logger = Logger.getLogger("com.bc.fiduceo.log");

            logger.setUseParentHandlers(false);
            logger.addHandler(handler);
        }
        logger.setLevel(logLevel.getValue());

        return logger;
    }

    public static void setLevelDebug() {
        if (logger != null) {
            logger.setLevel(Level.ALL);
        }
    }

    public static void setLevelSilent() {
        if (logger != null) {
            logger.setLevel(Level.WARNING);
        }
    }

    //Only for testing purpose
    final static String getLogMessage() {
        return logMessagesForTestOnly.toString();
    }

}
