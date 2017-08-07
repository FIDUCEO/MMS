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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.airs.ForReaderFactoryTest_AIRS_L1B_Reader;
import com.bc.fiduceo.reader.amsu_mhs.ForReaderFactoryTest_AMSUB_MHS_L1C_Reader;
import com.bc.fiduceo.reader.avhrr_gac.AVHRR_GAC_Reader;
import com.bc.fiduceo.reader.hirs.ForReaderFactoryTest_HIRS_L1C_Reader;
import com.bc.fiduceo.reader.iasi.IASI_Reader;
import com.bc.fiduceo.reader.insitu.sst_cci.SSTInsituReader;
import com.bc.fiduceo.reader.modis.ForReaderFactoryTest_MxD06_Reader;
import org.junit.*;


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
        ForReaderFactoryTest_AMSUB_MHS_L1C_Reader.checkInstance(reader);
    }

    @Test
    public void testGetAIRSReader() throws Exception {
        final Reader reader = readerFactory.getReader("airs-aq");

        assertNotNull(reader);
        ForReaderFactoryTest_AIRS_L1B_Reader.checkInstance(reader);
    }

    @Test
    public void testGetIASIReader() throws Exception {
        final Reader reader = readerFactory.getReader("iasi-mb");

        assertNotNull(reader);
        assertThat(reader, is(instanceOf(IASI_Reader.class)));
    }

    @Test
    public void testGetHirsReader() throws Exception {
        final Reader reader = readerFactory.getReader("hirs-n11");

        assertNotNull(reader);
        ForReaderFactoryTest_HIRS_L1C_Reader.checkInstance(reader);
    }

    @Test
    public void testGetSstInsituReader() throws Exception {
        final Reader reader = readerFactory.getReader("xbt-sst");

        assertNotNull(reader);
        assertTrue(reader instanceof SSTInsituReader);
    }

    @Test
    public void testGetModisCloudReader() throws Exception {
        final Reader reader = readerFactory.getReader("myd06-aq");

        assertNotNull(reader);
        ForReaderFactoryTest_MxD06_Reader.checkInstance(reader);
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
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
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
    public void testGetDataType() {
        assertEquals(DataType.POLAR_ORBITING_SATELLITE, readerFactory.getDataType("amsre-aq"));
        assertEquals(DataType.POLAR_ORBITING_SATELLITE, readerFactory.getDataType("iasi-mb"));
        assertEquals(DataType.POLAR_ORBITING_SATELLITE, readerFactory.getDataType("ssmt2-f12"));

        assertEquals(DataType.INSITU, readerFactory.getDataType("radiometer-sst"));
        assertEquals(DataType.INSITU, readerFactory.getDataType("ctd-sst"));
    }

    @Test
    public void testGetDataType_invalidSensor() throws Exception {
        try {
            readerFactory.getDataType("grepolysium");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testSingletonBehaviour() {
        final ReaderFactory factory = ReaderFactory.get(new GeometryFactory(GeometryFactory.Type.S2));
        assertNotNull(factory);

        final ReaderFactory secondCallFactory = ReaderFactory.get(new GeometryFactory(GeometryFactory.Type.S2));
        assertNotNull(secondCallFactory);

        assertSame(factory, secondCallFactory);
    }
}