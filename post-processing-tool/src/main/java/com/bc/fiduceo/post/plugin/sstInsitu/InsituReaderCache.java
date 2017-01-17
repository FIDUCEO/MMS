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
package com.bc.fiduceo.post.plugin.sstInsitu;

import com.bc.fiduceo.archive.Archive;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.util.TimeUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

class InsituReaderCache {

    private final Archive archive;
    private final PostProcessingContext context;
    private final HashMap<String, Reader> cacheMap;
    private final TreeMap<Long, String> oldestMap;

    InsituReaderCache(PostProcessingContext context) {
        this.context = context;
        archive = new Archive(context.getSystemConfig().getArchiveConfig());
        cacheMap = new HashMap<>();
        oldestMap = new TreeMap<>();
    }

    Reader getInsituFileOpened(String insituFileName, String sensorType, String processingVersion) throws IOException {
        if (cacheMap.containsKey(insituFileName)) {
            renewTimestamp(insituFileName);
            return cacheMap.get(insituFileName);
        }

        final Reader insituReader = openFile(insituFileName, sensorType, processingVersion);

        if (insituReader != null) {
            addReaderToCache(insituFileName, insituReader);
            removeOldestReader();
        }
        return insituReader;
    }

    static Date[] extractStartEndDateFromInsituFilename(String insituFileName) {
        final String[] strings = insituFileName.split("_");
        final String start = strings[strings.length - 2];
        final String end = strings[strings.length - 1].substring(0, 8);
        final String pattern = "yyyyMMdd";
        final Date[] startEnd = new Date[2];
        startEnd[0] = TimeUtils.parse(start, pattern);
        startEnd[1] = TimeUtils.parse(end, pattern);
        return startEnd;
    }

    private Reader openFile(String insituFileName, String sensorType, String processingVersion) throws IOException {
        final SystemConfig systemConfig = context.getSystemConfig();
        final String geomType = systemConfig.getGeometryLibraryType();
        final ReaderFactory readerFactory = ReaderFactory.get(new GeometryFactory(geomType));
        final Reader insituReader = readerFactory.getReader(sensorType);

        final Path insituProductsDir = archive.createValidProductPath(processingVersion, sensorType, 1970,1,1);
        insituReader.open(insituProductsDir.resolve(insituFileName).toFile());
        return insituReader;
    }

    private void removeOldestReader() throws IOException {
        // todo se/** 3 put maxCacheSize to configuration file
        // todo se/** 3 an other solution to eliminate caching problems can be sorting of insitu file indexes
        final int maxCacheSize = 70;
        if (oldestMap.size() > maxCacheSize) {
            final Map.Entry<Long, String> oldest = oldestMap.firstEntry();
            cacheMap.remove(oldest.getValue()).close();
            oldestMap.remove(oldest.getKey());
        }
    }

    private void renewTimestamp(String insituFileName) {
        for (Map.Entry<Long, String> oldest : oldestMap.entrySet()) {
            if (oldest.getValue().equals(insituFileName)) {
                oldestMap.remove(oldest.getKey());
                break;
            }
        }
        registerNameWithTimestamp(insituFileName);
    }

    private void addReaderToCache(String insituFileName, Reader insituReader) {
        cacheMap.put(insituFileName, insituReader);
        registerNameWithTimestamp(insituFileName);
    }

    private String registerNameWithTimestamp(String insituFileName) {
        return oldestMap.put(getTime(), insituFileName);
    }

    private long getTime() {
        return System.nanoTime();
    }
}
