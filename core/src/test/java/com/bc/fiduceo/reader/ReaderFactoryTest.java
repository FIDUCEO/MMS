/*
 * Copyright (C) 2018 Brockmann Consult GmbH
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
import com.bc.fiduceo.reader.airs.ForReaderFactoryTest_AIRS_L1B_Reader;
import com.bc.fiduceo.reader.amsr.amsr2.AMSR2_Reader;
import com.bc.fiduceo.reader.amsu_mhs.ForReaderFactoryTest_AMSUB_MHS_L1C_Reader;
import com.bc.fiduceo.reader.avhrr_gac.AVHRR_GAC_Reader;
import com.bc.fiduceo.reader.hirs.ForReaderFactoryTest_HIRS_L1C_Reader;
import com.bc.fiduceo.reader.iasi.IASI_Reader;
import com.bc.fiduceo.reader.insitu.sirds_sst.SirdsInsituReader;
import com.bc.fiduceo.reader.insitu.sst_cci.SSTInsituReader;
import com.bc.fiduceo.reader.modis.MxD06_Reader;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;


public class ReaderFactoryTest {

    private ReaderFactory readerFactory;

    @Before
    public void setUp() throws Exception {
        readerFactory = ReaderFactory.create(new GeometryFactory(GeometryFactory.Type.S2), null, null); // we don't need temp file support here tb 2018-01-23
    }

    @Test
    public void testGetAVHHRReader() {
        final Reader reader = readerFactory.getReader("avhrr-n06");

        assertNotNull(reader);
        assertTrue(reader instanceof AVHRR_GAC_Reader);
    }

    @Test
    public void testGetAMSUReader() {
        final Reader reader = readerFactory.getReader("amsub-n17");

        assertNotNull(reader);
        ForReaderFactoryTest_AMSUB_MHS_L1C_Reader.checkInstance(reader);
    }

    @Test
    public void testGetAIRSReader() {
        final Reader reader = readerFactory.getReader("airs-aq");

        assertNotNull(reader);
        ForReaderFactoryTest_AIRS_L1B_Reader.checkInstance(reader);
    }

    @Test
    public void testGetIASIReader() {
        final Reader reader = readerFactory.getReader("iasi-mb");

        assertNotNull(reader);
        assertThat(reader, is(instanceOf(IASI_Reader.class)));
    }

    @Test
    public void testGetHirsReader() {
        final Reader reader = readerFactory.getReader("hirs-n11");

        assertNotNull(reader);
        ForReaderFactoryTest_HIRS_L1C_Reader.checkInstance(reader);
    }

    @Test
    public void testGetSstInsituReader() {
        final Reader reader = readerFactory.getReader("xbt-sst");

        assertNotNull(reader);
        assertTrue(reader instanceof SSTInsituReader);
    }

    @Test
    public void testGetSirdsInsituReader() {
        final Reader reader = readerFactory.getReader("bottle-sirds");

        assertNotNull(reader);
        assertTrue(reader instanceof SirdsInsituReader);
    }

    @Test
    public void testGetModisCloudReader() {
        final Reader reader = readerFactory.getReader("myd06-aq");

        assertNotNull(reader);
        assertTrue(reader instanceof MxD06_Reader);
    }

    @Test
    public void testGetAMSR2Reader() {
        final Reader reader = readerFactory.getReader("amsr2-gcw1");

        assertNotNull(reader);
        assertTrue(reader instanceof AMSR2_Reader);
    }

    @Test
    public void testGetReaderNullKey() {
        try {
            readerFactory.getReader(null);
            fail("The key is null");
        } catch (IllegalArgumentException expect) {
        }
    }

    @Test
    public void testGetReaderEmptyKey() {
        try {
            readerFactory.getReader("");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testGetReaderKeyNonExist() {
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
    public void testGetDataType_invalidSensor() {
        try {
            readerFactory.getDataType("grepolysium");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testSingletonBehaviour() {
        final ReaderFactory factory = ReaderFactory.create(new GeometryFactory(GeometryFactory.Type.S2), null, null); // we don't need temp file support here tb 2018-01-23
        assertNotNull(factory);

        final ReaderFactory secondCallFactory = ReaderFactory.create(new GeometryFactory(GeometryFactory.Type.S2), null, null); // we don't need temp file support here tb 2018-01-23
        assertNotNull(secondCallFactory);

        assertSame(factory, secondCallFactory);
    }

    @Test
    public void testGet() {
        final ReaderFactory factory = ReaderFactory.create(new GeometryFactory(GeometryFactory.Type.S2), null, null); // we don't need temp file support here tb 2018-01-23

        final ReaderFactory factoryFromGet = ReaderFactory.get();

        assertSame(factory, factoryFromGet);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testGet_throwsWhenNotCreated() {
        ReaderFactory.clear();

        try {
            ReaderFactory.get();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}