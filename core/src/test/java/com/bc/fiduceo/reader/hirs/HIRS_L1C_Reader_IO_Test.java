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

package com.bc.fiduceo.reader.hirs;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.AcquisitionInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class HIRS_L1C_Reader_IO_Test {

    private File dataDirectory;
    private HIRS_L1C_Reader reader;

    @Before
    public void setUp() throws IOException {
        dataDirectory = TestUtil.getTestDataDirectory();
        reader = new HIRS_L1C_Reader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testReadAcquisitionInfo_TIROSN() throws IOException {
        final File file = getTirosNFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1979, 10, 14, 16, 23, 59, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1979, 10, 14, 18, 7, 33, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_NOAA10() throws IOException {
        final File file = getNOAA10File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1989, 3, 17, 6, 8, 45, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1989, 3, 17, 8, 2, 2, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_METOPA() throws IOException {
        final File file = getMetopAFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2011, 8, 23, 16, 41, 20, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2011, 8, 23, 18, 22, 40, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);
        } finally {
            reader.close();
        }
    }

    private File getMetopAFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-ma", "1.0", "2011", "08", "23", "190583863.NSS.HIRX.M2.D11235.S1641.E1823.B2513233.SV.nc"}, false);
        return getFileAsserted(testFilePath);
    }

    private File getTirosNFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-tn", "1.0", "1979", "10", "14", "NSS.HIRX.TN.D79287.S1623.E1807.B0516566.GC.nc"}, false);
        return getFileAsserted(testFilePath);
    }

    private File getNOAA10File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-n10", "1.0", "1989", "03", "17", "NSS.HIRX.NG.D89076.S0608.E0802.B1296162.WI.nc"}, false);
        return getFileAsserted(testFilePath);
    }

    private File getFileAsserted(String testFilePath) {
        final File file = new File(dataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}
