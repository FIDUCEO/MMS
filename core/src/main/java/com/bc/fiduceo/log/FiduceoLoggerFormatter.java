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

        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
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
