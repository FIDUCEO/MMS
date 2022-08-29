/*
 * Copyright (C) 2018 Brockmann Consult GmbH
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

package com.bc.fiduceo.reader;

import com.bc.fiduceo.archive.Archive;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.util.TempFileUtils;

import java.io.File;
import java.io.IOException;

public class ReaderContext {
    private GeometryFactory geometryFactory;
    private TempFileUtils tempFileUtils;
    private Archive archive;
    private String configDir;

    public ReaderContext() {
        configDir = "./config";
    }

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    public void setTempFileUtils(TempFileUtils tempFileUtils) {
        this.tempFileUtils = tempFileUtils;
    }

    public File createTempFile(String prefix, String extension) throws IOException {
        return tempFileUtils.create(prefix, extension);
    }

    public File createDirInTempDir(String directoryName) throws IOException {
        return tempFileUtils.createDir(directoryName);
    }

    public void deleteTempFile(File tempFile) {
        tempFileUtils.delete(tempFile);
    }

    public Archive getArchive() {
        return archive;
    }

    public void setArchive(Archive archive) {
        this.archive = archive;
    }

    public void setConfigDir(String configDir) {
        this.configDir = configDir;
    }

    public String getConfigDir() {
        return configDir;
    }
}
