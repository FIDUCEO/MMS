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

import com.bc.fiduceo.IOTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class MDRCache_IO_Test {

    private ImageInputStream iis;

    @Before
    public void setUp() throws IOException {
        final File file = IASI_TestUtil.getIasiFile_MB();

        iis = new FileImageInputStream(file);
    }

    @After
    public void tearDown() throws IOException {
        iis.close();
    }

    @Test
    public void testReadOneRecord() throws IOException {
        final MDRCache mdrCache = new MDRCache(iis, IASI_TestUtil.MDR_OFFSET_MA);

        final MDR_1C_v5 mdr = mdrCache.getRecord(0);
        assertNotNull(mdr);
    }

    @Test
    public void testReadOneRecord_twiceReturnsTheSameObject() throws IOException {
        final MDRCache mdrCache = new MDRCache(iis, IASI_TestUtil.MDR_OFFSET_MA);

        final MDR_1C_v5 mdr_1 = mdrCache.getRecord(167);
        assertNotNull(mdr_1);

        final MDR_1C_v5 mdr_2 = mdrCache.getRecord(167);
        assertNotNull(mdr_2);

        assertSame(mdr_1, mdr_2);
    }

    @Test
    public void testReadOneRecord_coversTwoConsecutiveLines() throws IOException {
        final MDRCache mdrCache = new MDRCache(iis, IASI_TestUtil.MDR_OFFSET_MA);

        final MDR_1C_v5 mdr_216 = mdrCache.getRecord(216);
        assertNotNull(mdr_216);

        final MDR_1C_v5 mdr_217 = mdrCache.getRecord(217);
        assertNotNull(mdr_217);

        assertSame(mdr_216, mdr_217);
    }

    @Test
    public void testReadRecordsUntilCacheIsFull() throws IOException {
        final MDRCache mdrCache = new MDRCache(iis, IASI_TestUtil.MDR_OFFSET_MA);

        final MDR_1C_v5 mdr_first = mdrCache.getRecord(216);
        assertNotNull(mdr_first);

        for (int i = 0; i < MDRCache.CAPACITY + 2; i++) {
            final MDR_1C_v5 mdr_217 = mdrCache.getRecord(217 + i);
            assertNotNull(mdr_217);
        }

        final MDR_1C_v5 mdr_second = mdrCache.getRecord(216);
        assertNotNull(mdr_second);
        assertNotSame(mdr_first, mdr_second);
    }

    @Test
    public void testReadRecords_MoreThanMaxCapacityCalls_StillTheSameRecord() throws IOException {
        final MDRCache mdrCache = new MDRCache(iis, IASI_TestUtil.MDR_OFFSET_MA);

        final MDR_1C_v5 mdr_first = mdrCache.getRecord(150);
        assertNotNull(mdr_first);

        final int firstCalls = 20;
        for (int i = 0; i < firstCalls; i++) {
            final MDR_1C_v5 mdr_217 = mdrCache.getRecord(217 + i);
            assertNotNull(mdr_217);
        }

        assertSame(mdr_first, mdrCache.getRecord(150));

        // continue calls
        for (int i = firstCalls; i < MDRCache.CAPACITY + 2; i++) {
            final MDR_1C_v5 mdr_217 = mdrCache.getRecord(217 + i);
            assertNotNull(mdr_217);
        }

        assertSame(mdr_first, mdrCache.getRecord(150));
    }
}
