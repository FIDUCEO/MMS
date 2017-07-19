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
 *
 */

package com.bc.fiduceo.reader;

import com.bc.fiduceo.log.FiduceoLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class ReaderCache extends LinkedHashMap<Path, Reader> {

    // The default load factor  used when none specified in constructor.
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    // The default initial capacity - MUST be a power of two.
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
    private final int cacheSize;
    private final ReaderFactory readerFactory;

    public ReaderCache(int cacheSize, ReaderFactory readerFactory) {
        super(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, true);
        this.cacheSize = cacheSize;
        this.readerFactory = readerFactory;
    }

    public void add(Reader reader, Path filePath) {
        put(filePath, reader);
    }

    public void close() throws IOException {
        for (Map.Entry<Path, Reader> next : entrySet()) {
            next.getValue().close();
        }
    }

    public Reader getReaderFor(String sensorName, Path observationPath) throws IOException {
        if (containsKey(observationPath)) {
            return get(observationPath);
        } else {
            final Reader reader = readerFactory.getReader(sensorName);
            reader.open(observationPath.toFile());
            add(reader, observationPath);
            return reader;
        }
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Path, Reader> eldest) {
        final boolean remove = size() > cacheSize;
        if (remove) {
            try {
                final Reader reader = eldest.getValue();
                reader.close();
            } catch (IOException e) {
                final Path key = eldest.getKey();
                FiduceoLogger.getLogger().log(Level.WARNING, "Unable to close reader for file \"" + key.toString() + "\"", e);
            }
        }
        return remove;
    }
}
