/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.airs.AIRS_L1B_Reader;
import com.bc.fiduceo.reader.amsu_mhs.AMSUB_MHS_L1C_Reader;
import com.bc.fiduceo.reader.avhrr_gac.AVHRR_GAC_Reader;
import com.bc.fiduceo.reader.hirs.HIRS_L1C_Reader;
import com.bc.fiduceo.reader.iasi.EumetsatIASIReader;
import com.bc.fiduceo.reader.insitu.SSTInsituReader;
import com.bc.fiduceo.reader.insitu.SSTInsituReaderTest;
import org.junit.*;

import static org.junit.Assert.*;


public class ReaderFactoryTest {

    private ReaderFactory readerFactory;

    @Before
    public void setUp() throws Exception {
        readerFactory = ReaderFactory.get(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testGetAVHHRReader() throws Exception {
        final Reader reader = readerFactory.getReader("avhrr-n06");

        assertNotNull(reader);
        assertTrue(reader instanceof AVHRR_GAC_Reader);
    }

    @Test
    public void testGetAMSUReader() throws Exception {
        final Reader reader = readerFactory.getReader("amsub-n17");

        assertNotNull(reader);
        assertTrue(reader instanceof AMSUB_MHS_L1C_Reader);
    }

    @Test
    public void testGetAIRSReader() throws Exception {
        final Reader reader = readerFactory.getReader("airs-aq");

        assertNotNull(reader);
        assertTrue(reader instanceof AIRS_L1B_Reader);
    }

    @Test
    public void testGetEumetsatIASIReader() throws Exception {
        final Reader reader = readerFactory.getReader("iasi-mb");

        assertNotNull(reader);
        assertTrue(reader instanceof EumetsatIASIReader);
    }

    @Test
    public void testGetHirsReader() throws Exception {
        final Reader reader = readerFactory.getReader("hirs-n11");

        assertNotNull(reader);
        assertTrue(reader instanceof HIRS_L1C_Reader);
    }

    @Test
    public void testGetSstInsituReader() throws Exception {
        final Reader reader = readerFactory.getReader("xbt-sst");

        assertNotNull(reader);
        assertTrue(reader instanceof SSTInsituReader);
    }

    @Test
    public void testGetReaderNullKey() throws Exception {
        try {
            readerFactory.getReader(null);
            fail("The key is null");
        } catch (IllegalArgumentException expect) {
        }
    }

    @Test
    public void testGetReaderEmptyKey() throws Exception {
        try {
            readerFactory.getReader("");
            fail("The key is empty");
        } catch (IllegalArgumentException expect) {
        }
    }

    @Test
    public void testGetReaderKeyNonExist() throws Exception {
        try {
            readerFactory.getReader("uztierter");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testSingletonBehaviour(){
        final ReaderFactory factory = ReaderFactory.get(new GeometryFactory(GeometryFactory.Type.S2));
        assertNotNull(factory);

        final ReaderFactory secondCallFactory = ReaderFactory.get(new GeometryFactory(GeometryFactory.Type.S2));
        assertNotNull(secondCallFactory);

        assertSame(factory, secondCallFactory);
    }
}