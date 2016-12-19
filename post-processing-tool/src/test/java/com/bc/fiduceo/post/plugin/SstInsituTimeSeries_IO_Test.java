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

package com.bc.fiduceo.post.plugin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.post.PostProcessingConfig;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.*;
import org.junit.runner.*;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

@RunWith(IOTestRunner.class)
public class SstInsituTimeSeries_IO_Test {

    @Test
    public void getInsituFileOpened() throws Exception {
        final String root = TestUtil.getTestDataDirectory().getAbsolutePath();
        final String useCaseXml = "<system-config>" +
                                  "    <archive>" +
                                  "        <root-path>" +
                                  "            " + root +
                                  "        </root-path>" +
                                  "        <rule sensors = \"animal-sst\">" +
                                  "            insitu/SENSOR/VERSION" +
                                  "        </rule>" +
                                  "    </archive>" +
                                  "</system-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        final SystemConfig systemConfig = SystemConfig.load(inputStream);

        // action
        final Reader insituFileOpened = SstInsituTimeSeries
                    .getInsituFileOpened("insitu_12_WMOID_11835_20040110_20040127.nc", "animal-sst", "v03.3", systemConfig);

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
    public void prepare() throws Exception {
        final String s = File.separator;
        final String testDataDir = TestUtil.getTestDataDirectory().getAbsolutePath();

        final String input = testDataDir + s + "mmd06c" + s + "animal-sst_amsre-aq" + s + "mmd6c_sst_animal-sst_amsre-aq_2004-008_2004-014.nc";
        final NetcdfFile reader = NetCDFUtils.openReadOnly(input);

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        final Attribute targetAttrib = new Attribute("use-case-configuration", "");
        when(writer.findGlobalAttribute("use-case-configuration")).thenReturn(targetAttrib);

        final SstInsituTimeSeries insituTimeSeries = new SstInsituTimeSeries("v123", 124, 2);
        final PostProcessingContext context = new PostProcessingContext();
        context.setSystemConfig(SystemConfig.load(new ByteArrayInputStream(
                    ("<system-config>" +
                     "<geometry-library name = \"S2\" />" +
                     "    <archive>" +
                     "        <root-path>" +
                     "            " + testDataDir +
                     "        </root-path>" +
                     "        <rule sensors = \"animal-sst\">" +
                     "            insitu/SENSOR/VERSION" +
                     "        </rule>" +
                     "    </archive>" +
                     "</system-config>").getBytes())));
        context.setProcessingConfig(PostProcessingConfig.load(new ByteArrayInputStream(
                    ("<"+ PostProcessingConfig.TAG_NAME_ROOT + ">" +
                     "<create-new-files>" +
                     "<output-directory>outDir</output-directory>" +
                     "</create-new-files>" +
                     "<post-processings>" +
                     "<dummy-post-processing>ABC</dummy-post-processing>" +
                     "</post-processings>" +
                     "</"+ PostProcessingConfig.TAG_NAME_ROOT + ">").getBytes())));
        insituTimeSeries.setContext(context);

        insituTimeSeries.prepare(reader, writer);

//        verify(writer, times(1)).findGlobalAttribute("use-case-configuration");
//        assertEquals("", targetAttrib.getStringValue());
    }
}
