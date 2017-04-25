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
 */

package com.bc.fiduceo.post;

import static org.junit.Assert.*;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.reader.Reader;
import org.junit.*;
import org.junit.runner.*;
import ucar.nc2.Variable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

/**
 * Created by Sabine on 04.01.2017.
 */
@RunWith(IOTestRunner.class)
public class ReaderCacheTest {

    private File testDataDirectory;

    @Before
    public void setUp() throws Exception {
        testDataDirectory = TestUtil.getTestDataDirectory();
    }

    @Test
    public void getInsituFileOpened() throws Exception {
        final String root = testDataDirectory.getAbsolutePath();
        final String systemConfigXml = "<system-config>" +
                                       "    <archive>" +
                                       "        <root-path>" +
                                       "            " + root +
                                       "        </root-path>" +
                                       "        <rule sensors = \"animal-sst\">" +
                                       "            insitu/SENSOR/VERSION" +
                                       "        </rule>" +
                                       "    </archive>" +
                                       "</system-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(systemConfigXml.getBytes());

        final String processingVersion = "v03.3";
        final PostProcessingContext context = new PostProcessingContext();
        context.setSystemConfig(SystemConfig.load(inputStream));
        final ReaderCache readerCache = new ReaderCache(context) {
            @Override
            protected int[] extractYearMonthDayFromFilename(String fileName) {
                return new int[]{1970, 1, 1};
            }
        };

        // action
        final Reader insituFileOpened = readerCache
                    .getFileOpened("insitu_12_WMOID_11835_20040110_20040127.nc", "animal-sst", processingVersion);

        //validation
        assertNotNull(insituFileOpened);
        final List<Variable> variables = insituFileOpened.getVariables();
        assertNotNull(variables);
        final String[] expectedNames = {
                    "insitu.time",
                    "insitu.lat",
                    "insitu.lon",
                    "insitu.sea_surface_temperature",
                    "insitu.sst_uncertainty",
                    "insitu.sst_depth",
                    "insitu.sst_qc_flag",
                    "insitu.sst_track_flag",
                    "insitu.mohc_id",
                    "insitu.id"
        };
        assertEquals(expectedNames.length, variables.size());
        for (int i = 0; i < variables.size(); i++) {
            Variable variable = variables.get(i);
            assertEquals(i + ": " + expectedNames[i], i + ": " + variable.getShortName());
        }
    }
}