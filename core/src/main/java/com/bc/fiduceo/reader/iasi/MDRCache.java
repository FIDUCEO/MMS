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

package com.bc.fiduceo.reader.iasi;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

class MDRCache {

    static final int CAPACITY = 64;

    private final ImageInputStream iis;
    private final long firstMdrOffset;
    // @todo 1 tb/tb replace with abtract MDR1C 2017-06-14
    private final Cache<Integer, MDR_1C_v5> cache = new Cache<>(CAPACITY);

    MDRCache(ImageInputStream iis, long firstMdrOffset) {
        this.iis = iis;
        this.firstMdrOffset = firstMdrOffset;
    }

    MDR_1C_v5 getRecord(int line) throws IOException {
        MDR_1C_v5 mdr = cache.get(line);
        if (mdr != null) {
            return mdr;
        }

        final int mdrIndex = getMdrIndex(line);
        final int targetLine = 2 * mdrIndex;

        mdr = readMdr(line);

        cache.put(targetLine, mdr);
        cache.put(targetLine + 1, mdr);

        return mdr;
    }

    private MDR_1C_v5 readMdr(int line) throws IOException {
        final int mdrIndex = getMdrIndex(line);

        // @todo 1 tb/tb replace construction with call to factory 2017-06-14
        final MDR_1C_v5 mdr_1C = new MDR_1C_v5();

        iis.seek(firstMdrOffset + mdrIndex * mdr_1C.getMdrSize());
        iis.read(mdr_1C.getRaw_record());
        return mdr_1C;
    }

    // package access for testing only tb 2017-05-03
    static int getMdrIndex(int line) {
        return line / 2;
    }

    private class Cache<K, V> extends LinkedHashMap<K, V> {

        static final float DEFAULT_LOAD_FACTOR_FROM_HASH_MAP = 0.75f;
        private final int maxCapacity;

        Cache(int maxCapacity) {
            super(maxCapacity + 1, DEFAULT_LOAD_FACTOR_FROM_HASH_MAP, true);
            this.maxCapacity = maxCapacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > maxCapacity;
        }
    }
}
