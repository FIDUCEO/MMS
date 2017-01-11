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


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class NwpUtils {

    // @todo 2 tb/tb write tests for this method 2017-01-10
    static String composeFilesString(final String dirPath, final List<String> subDirPaths, final String pattern, int skip) {
        final StringBuilder sb = new StringBuilder();
        final List<File> allFiles = new ArrayList<>();
        final FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(pattern);
            }
        };
        for (final String subDirPath : subDirPaths) {
            final File subDir = new File(dirPath, subDirPath);
            final File[] files = subDir.listFiles(filter);
            if (files == null) {
                throw new RuntimeException(String.format("%s directory does not exist", subDir.getPath()));
            }
            Arrays.sort(files);
            Collections.addAll(allFiles, files);
        }
        final int m;
        final int n;
        if (skip >= 0) {
            m = skip;
            n = allFiles.size();
        } else {
            m = 0;
            n = allFiles.size() + skip;
        }
        for (int i = m; i < n; i++) {
            final File file = allFiles.get(i);
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(file.getPath());
        }
        return sb.toString();
    }

    static File createTempFile(String prefix, String suffix, boolean deleteOnExit) throws IOException {
        final File tempFile = File.createTempFile(prefix, suffix);
        if (deleteOnExit) {
            tempFile.deleteOnExit();
        }
        return tempFile;
    }
}
