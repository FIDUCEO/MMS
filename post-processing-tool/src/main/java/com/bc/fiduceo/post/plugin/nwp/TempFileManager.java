/*
 * Copyright (C) 2017 Brockmann Consult GmbH
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

package com.bc.fiduceo.post.plugin.nwp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class TempFileManager {

    private final List<File> tempFileList;

    TempFileManager() {
        tempFileList = new ArrayList<>();
    }

    File create(String prefix, String extension) throws IOException {
        final File tempFile = File.createTempFile(prefix, "." + extension);

        tempFileList.add(tempFile);

        return tempFile;
    }

    void cleanup() {
        for (final File tempFile : tempFileList) {
            if (!tempFile.delete()) {
                // @todo 2 tb/tb add logging here 2017-03-17
                System.out.println("Unable to delete file: " + tempFile.getAbsolutePath());
            }
        }
    }
}
