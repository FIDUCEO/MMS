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

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReaderCache extends LinkedHashMap<Path, Reader> {

    // The default load factor  used when none specified in constructor.
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    // The default initial capacity - MUST be a power of two.
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
    private final int cacheSize;

    public ReaderCache(int cacheSize) {
        super(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, true);
        this.cacheSize = cacheSize;
    }

    public void add(Reader reader, Path filePath) throws IOException {
        try {
            put(filePath, reader);
        } catch (UnableToCloseException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public void close() throws IOException {
        for (Map.Entry<Path, Reader> next : entrySet()) {
            next.getValue().close();
        }
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Path, Reader> eldest) {
        final boolean remove = size() > cacheSize;
        if (remove) {
            try {
                eldest.getValue().close();
            } catch (IOException e) {
                final Path key = eldest.getKey();
                throw new UnableToCloseException("Unable to close reader for file \"" + key.toString() + "\"", e);
            }
        }
        return remove;
    }

    static class UnableToCloseException extends RuntimeException {

        public UnableToCloseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
