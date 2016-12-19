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

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ReaderCacheTest {

    private ReaderCache readerCache;

    @Before
    public void setUp() {
        readerCache = new ReaderCache(2);
    }

    @Test
    public void testGetReader_emptyCache() {
        final Reader reader = readerCache.get("usr/local/data/file_1");
        assertNull(reader);
    }

    @Test
    public void testAddAndGet() throws IOException {
        final Reader reader = mock(Reader.class);

        readerCache.add(reader, "a/relative/path");
        final Reader retrievedReader = readerCache.get("a/relative/path");
        assertSame(reader, retrievedReader);
    }

    @Test
    public void testAddTwoAndGet() throws IOException {
        final Reader reader_1 = mock(Reader.class);
        final Reader reader_2 = mock(Reader.class);

        readerCache.add(reader_1, "a/relative/path/one");
        readerCache.add(reader_2, "a/relative/path/two");

        Reader retrievedReader = readerCache.get("a/relative/path/one");
        assertSame(reader_1, retrievedReader);

        retrievedReader = readerCache.get("a/relative/path/two");
        assertSame(reader_2, retrievedReader);
    }

    @Test
    public void testAddThreeAndGet_oldestRemoved() throws InterruptedException, IOException {
        final Reader reader_1 = mock(Reader.class);
        final Reader reader_2 = mock(Reader.class);
        final Reader reader_3 = mock(Reader.class);

        readerCache.add(reader_1, "a/relative/path/one");
        Thread.sleep(100);
        readerCache.add(reader_2, "a/relative/path/two");
        Thread.sleep(100);
        readerCache.add(reader_3, "a/relative/path/three");

        Reader retrievedReader = readerCache.get("a/relative/path/one");
        assertNull(retrievedReader);

        retrievedReader = readerCache.get("a/relative/path/two");
        assertSame(reader_2, retrievedReader);

        retrievedReader = readerCache.get("a/relative/path/three");
        assertSame(reader_3, retrievedReader);

        verify(reader_1, times(1)).close();
    }

    @Test
    public void testAddAndClose() throws IOException {
        final Reader reader_1 = mock(Reader.class);
        final Reader reader_2 = mock(Reader.class);

        readerCache.add(reader_1, "a/relative/path/one");
        readerCache.add(reader_2, "a/relative/path/two");

        readerCache.close();

        verify(reader_1, times(1)).close();
        verify(reader_2, times(1)).close();
    }
}
