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

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FiduceoLogger {
    private static final Level DEFAULT_LOG_LEVEL = Level.INFO;

    private static Logger logger = null;

    public static Level getDefaultLevel() {
        return DEFAULT_LOG_LEVEL;
    }

    public static Logger getLogger() {
        return getLogger(DEFAULT_LOG_LEVEL);
    }

    public static Logger getLogger(Level logLevel) {
        FiduceoLoggerFormatter fiduceoLoggerFormatter = new FiduceoLoggerFormatter();
        if (logger == null) {
            final ConsoleHandler handler = new ConsoleHandler();
            handler.setFormatter(fiduceoLoggerFormatter);
            handler.setLevel(Level.ALL);
            logger = Logger.getLogger("com.bc.fiduceo.log");
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);
        }
        logger.setLevel(logLevel);
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
}
