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

package com.bc.fiduceo.post.plugin.sstInsitu;

import static org.junit.Assert.*;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.util.TimeUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.*;
import ucar.nc2.Variable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Sabine on 04.01.2017.
 */
public class InsituReaderCacheTest {

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
        final InsituReaderCache insituReaderCache = new InsituReaderCache(context);

        // action
        final Reader insituFileOpened = insituReaderCache
                    .getInsituFileOpened("insitu_12_WMOID_11835_20040110_20040127.nc", "animal-sst", processingVersion);

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

    @Test
    public void extractStartEndDateFromInsituFilename() throws Exception {
        final String begin = "19700325";
        final String end = "19760625";

        final Date[] dates = InsituReaderCache.extractStartEndDateFromInsituFilename(
                    "anyNameWith_yyyyMMdd_atTheLastTwoPositions_" + begin + "_" + end + ".anyExtension");

        assertEquals(2, dates.length);
        assertEquals("25-Mar-1970 00:00:00", TimeUtils.format(dates[0]));
        assertEquals("25-Jun-1976 00:00:00", TimeUtils.format(dates[1]));
    }
}