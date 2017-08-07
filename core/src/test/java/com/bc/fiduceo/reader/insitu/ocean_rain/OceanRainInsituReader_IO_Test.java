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

package com.bc.fiduceo.reader.insitu.ocean_rain;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.reader.AcquisitionInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(IOTestRunner.class)
public class OceanRainInsituReader_IO_Test {

    private OceanRainInsituReader reader;
    private File testFile;

    @Before
    public void setUp() throws IOException {
        reader = new OceanRainInsituReader();

        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "ocean-rain-sst", "v1.0", "OceanRAIN_allships_2010-2017_SST.ascii"}, false);
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        testFile = new File(testDataDirectory, relativePath);
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        reader.open(testFile);

        try {
            final AcquisitionInfo info = reader.read();
            assertNotNull(info);

            TestUtil.assertCorrectUTCDate(2010, 6, 10, 21, 0, 0, 0, info.getSensingStart());
            TestUtil.assertCorrectUTCDate(2016, 9, 16, 22, 59, 0, 0, info.getSensingStop());

            assertEquals(NodeType.UNDEFINED, info.getNodeType());

            assertNull(info.getBoundingGeometry());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables() throws Exception {
        reader.open(testFile);

        final List<Variable> variables = reader.getVariables();

        assertNotNull(variables);
        assertEquals(4, variables.size());
        assertEquals("lon", variables.get(0).getShortName());
        assertEquals("lat", variables.get(1).getShortName());
        assertEquals("time", variables.get(2).getShortName());
        assertEquals("sst", variables.get(3).getShortName());
    }
}
