/*
 * Copyright (C) 2018 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.util;

import com.bc.fiduceo.log.FiduceoLogger;
import org.esa.snap.core.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TempFileUtils {

    private final File tempDir;
    private final List<File> tempFileList;
    private boolean keepAfterCleanup;

    public TempFileUtils() {
        this(System.getProperty("java.io.tmpdir"));
    }

    public TempFileUtils(String tempDir) {
        this.tempDir = new File(tempDir);
        validateTempDir();
        tempFileList = new ArrayList<>();
        keepAfterCleanup = false;
    }

    public File getTempDir() {
        return tempDir;
    }

    public File create(String prefix, String extension) throws IOException {
        final File tempFile = createFile(prefix, extension);

        tempFileList.add(tempFile);

        return tempFile;
    }

    public File createDir(String directoryName) throws IOException {
        final File dir = new File(tempDir, directoryName);
        if (!dir.mkdirs()) {
            throw new IOException("unable to create temp directory: " + dir.getAbsolutePath());
        }

        tempFileList.add(dir);

        return dir;
    }

    public void delete(File tempFile) {
        final boolean deleted = deleteFileIfExists(tempFile);

        if (deleted) {
            tempFileList.remove(tempFile);
        }
    }

    public void cleanup() {
        if (keepAfterCleanup) {
            return;
        }

        for (final File tempFile : tempFileList) {
            deleteFileIfExists(tempFile);
        }
    }

    private boolean deleteFileIfExists(File tempFile) {
        boolean success = true;
        if (tempFile.isFile()) {
            if (!tempFile.delete()) {
                FiduceoLogger.getLogger().warning("Unable to delete file: " + tempFile.getAbsolutePath());
                success = false;
            }
        }
        if (tempFile.isDirectory()) {
            if (!FileUtils.deleteTree(tempFile)) {
                FiduceoLogger.getLogger().warning("Unable to delete directory: " + tempFile.getAbsolutePath());
                success = false;
            }
        }
        return success;
    }

    public void keepAfterCleanup(boolean keep) {
        keepAfterCleanup = keep;
    }

    private void validateTempDir() {
        if (!tempDir.isDirectory()) {
            throw new RuntimeException("configured tempDir '" + tempDir.getAbsolutePath() + "' does not exist");
        }
        if (!tempDir.canWrite()) {
            throw new RuntimeException("configured tempDir '" + tempDir.getAbsolutePath() + "' is not writeable");
        }
    }

    private File createFile(String prefix, String extension) throws IOException {
        if (tempDir == null) {
            return File.createTempFile(prefix, "." + extension);
        }

        final long nanoTime = System.nanoTime();
        final long time = new Date().getTime();
        final String fileName = prefix + time + "_" + Math.abs(nanoTime) + "." + extension;
        final File tempFile = new File(tempDir, fileName);
        if (!tempFile.createNewFile()) {
            throw new RuntimeException("Unable to create temp file");
        }

        return tempFile;
    }
}
