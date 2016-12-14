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
 */
package com.bc.fiduceo.post;

import com.bc.fiduceo.log.FiduceoLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class SourceTargetManager {

    private final boolean overwrite;
    private final String outputDirectory;

    public SourceTargetManager(PostProcessingConfig config) {
        overwrite = config.isOverwrite();
        outputDirectory = config.getOutputDirectory();
    }

    public Path getSource(Path src) throws IOException {
        if (overwrite) {
            final Path temp = createTempFile(src);
            Files.move(src, temp);
            return temp;
        } else {
            return src;
        }
    }

    public Path getTargetPath(Path src) {
        if (overwrite) {
            return src;
        } else {
            return Paths.get(outputDirectory).resolve(src.getFileName());
        }
    }

    public void processingDone(Path src, Exception exception) {
        if (!overwrite) {
            return;
        }
        final Path tempFile = createTempFile(src);
        if (exception == null) {
            if (!tempFile.toFile().delete()) {
                tempFile.toFile().deleteOnExit();
            }
        } else {
            if (src.toFile().delete()) {
                try {
                    Files.move(tempFile, src);
                } catch (IOException e) {
                    loggErrorMessage(src, tempFile);
                }
            } else {
                loggErrorMessage(src, tempFile);
            }
        }
    }

    private void loggErrorMessage(Path src, Path tempFile) {
        final Logger logger = FiduceoLogger.getLogger();
        logger.severe("Unable to restore the source file. After computation please remove the source file "
                      + src.toAbsolutePath().toString() + " and replace it by removing the '.temp' extension from" +
                      " the temporary file " + tempFile.toAbsolutePath().toString() + ".");
    }

    private Path createTempFile(Path src) {
        return src.getParent().resolve(src.getFileName().toString() + ".temp");
    }
}
