/*
 * Copyright (C) 2017 Brockmann Consult GmbH
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

package com.bc.fiduceo.post.plugin.nwp;


import com.bc.fiduceo.log.FiduceoLogger;

import java.io.*;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

class ProcessRunner {

    private final Logger logger;

    static File writeExecutableScript(String shellScript, String prefix, String suffix, boolean deleteOnExit) throws IOException {
        final File scriptFile = NwpUtils.createTempFile(prefix, suffix, deleteOnExit);

        final boolean executable = scriptFile.setExecutable(true);
        if (!executable) {
            throw new RuntimeException("Cannot set script to executable: " + scriptFile.getAbsolutePath());
        }

        final Writer writer = new FileWriter(scriptFile);
        try {
            writer.write(shellScript);
        } finally {
            try {
                writer.close();
            } catch (IOException ignored) {
            }
        }
        return scriptFile;
    }

    ProcessRunner() {
        logger = FiduceoLogger.getLogger();
    }

    void execute(final String command) throws IOException, InterruptedException {
        try {
            if (logger.isLoggable(Level.FINER)) {
                logger.entering(ProcessRunner.class.getName(), "execute");
            }
            if (command == null) {
                return;
            }
            if (command.isEmpty()) {
                return;
            }
            if (logger.isLoggable(Level.INFO)) {
                logger.info(MessageFormat.format("executing process <code>{0}</code>", command));
            }

            final Process process = Runtime.getRuntime().exec(command);

            final LoggingThread err = new LoggingThread(process.getErrorStream());
            final LoggingThread out = new LoggingThread(process.getInputStream());
            err.start();
            out.start();

            if (process.waitFor() != 0) {
                throw new RuntimeException(MessageFormat.format("Command <code>{0}</code> terminated with exit value {1}",
                                command, process.exitValue()));
            }

        } finally {
            if (logger.isLoggable(Level.FINER)) {
                logger.exiting(ProcessRunner.class.getName(), "execute");
            }
        }
    }

    private class LoggingThread extends Thread {

        private final InputStream inputStream;

        private LoggingThread(final InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                while (true) {
                    final String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    logger.info(line);
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
