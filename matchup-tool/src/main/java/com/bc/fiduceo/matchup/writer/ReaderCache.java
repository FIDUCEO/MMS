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

package com.bc.fiduceo.matchup.writer;

import com.bc.fiduceo.reader.Reader;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class ReaderCache {

    private final HashMap<String, Container> readerMap;
    private final int cacheSize;

    ReaderCache(int cacheSize) {
        readerMap = new HashMap<>();
        this.cacheSize = cacheSize;
    }

    public Reader get(String filePath) {
        final Container container = readerMap.get(filePath);
        if (container != null) {
            return container.reader;
        }

        return null;
    }

    void add(Reader reader, String filePath) throws IOException {
        final int mapSize = readerMap.size();

        if (mapSize >= cacheSize) {
            long oldest = Long.MAX_VALUE;
            String key = null;

            for (Map.Entry<String, Container> next : readerMap.entrySet()) {
                final Container container = next.getValue();
                if (container.timeStamp < oldest) {
                    oldest = container.timeStamp;
                    key = next.getKey();
                }
            }

            final Container container = readerMap.get(key);
            container.reader.close();
            readerMap.remove(key);
        }

        final Container container = new Container();
        container.reader = reader;
        container.timeStamp = new Date().getTime();

        readerMap.put(filePath, container);
    }

    void close() throws IOException {
        for (Map.Entry<String, Container> next : readerMap.entrySet()) {
            final Container container = next.getValue();
            container.reader.close();
        }
    }

    private static class Container {
        Reader reader;
        long timeStamp;
    }
}
