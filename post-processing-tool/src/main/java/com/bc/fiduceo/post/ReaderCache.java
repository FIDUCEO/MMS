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

import com.bc.fiduceo.archive.Archive;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

// @todo 1 tb/** this class does not ensure that all readers are getting closed again. Clean this up! tb 2017-06-12
public abstract class ReaderCache {

    private final Archive archive;
    private final PostProcessingContext context;
    private final HashMap<String, Reader> cacheMap;
    private final TreeMap<Long, String> oldestMap;

    public ReaderCache(PostProcessingContext context) {
        this.context = context;
        archive = new Archive(context.getSystemConfig().getArchiveConfig());
        cacheMap = new HashMap<>();
        oldestMap = new TreeMap<>();
    }

    public Reader getFileOpened(String fileName, String sensorType, String processingVersion) throws IOException {
        if (cacheMap.containsKey(fileName)) {
            renewTimestamp(fileName);
            return cacheMap.get(fileName);
        }

        final Reader reader = openFile(fileName, sensorType, processingVersion);

        if (reader != null) {
            addReaderToCache(fileName, reader);
            removeOldestReader();
        }
        return reader;
    }

    protected abstract int[] extractYearMonthDayFromFilename(String fileName);

    private Reader openFile(String fileName, String sensorType, String processingVersion) throws IOException {
        final SystemConfig systemConfig = context.getSystemConfig();
        final String geomType = systemConfig.getGeometryLibraryType();
        final ReaderFactory readerFactory = ReaderFactory.get(new GeometryFactory(geomType));
        final Reader reader = readerFactory.getReader(sensorType);

        int[] ymd = extractYearMonthDayFromFilename(fileName);

        final Path productsDir = archive.createValidProductPath(processingVersion, sensorType, ymd[0], ymd[1], ymd[2]);
        reader.open(productsDir.resolve(fileName).toFile());
        return reader;
    }

    private void removeOldestReader() throws IOException {
        // todo se/** 3 put maxCacheSize to configuration file
        // todo se/** 3 an other solution to eliminate caching problems can be sorting of file indexes
        final int maxCacheSize = 70;
        if (oldestMap.size() > maxCacheSize) {
            final Map.Entry<Long, String> oldest = oldestMap.firstEntry();
            cacheMap.remove(oldest.getValue()).close();
            oldestMap.remove(oldest.getKey());
        }
    }

    private void renewTimestamp(String fileName) {
        for (Map.Entry<Long, String> oldest : oldestMap.entrySet()) {
            if (oldest.getValue().equals(fileName)) {
                oldestMap.remove(oldest.getKey());
                break;
            }
        }
        registerNameWithTimestamp(fileName);
    }

    private void addReaderToCache(String fileName, Reader reader) {
        cacheMap.put(fileName, reader);
        registerNameWithTimestamp(fileName);
    }

    private String registerNameWithTimestamp(String fileName) {
        return oldestMap.put(getTime(), fileName);
    }

    private long getTime() {
        return System.nanoTime();
    }
}
