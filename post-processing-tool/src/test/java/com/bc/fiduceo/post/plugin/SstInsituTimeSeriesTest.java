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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.post.PostProcessingConfig;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.*;
import org.junit.runner.*;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RunWith(IOTestRunner.class)
public class SstInsituTimeSeriesTest {


    @Before
    public void setUp() throws Exception {
    }


    @Test
    public void extractSensorType_Success() throws Exception {
        final Variable variable = mock(Variable.class);
        when(variable.getShortName()).thenReturn("sensor-name_insitu.sonstwas");

        final NetcdfFile reader = mock(NetcdfFile.class);
        when(reader.getVariables()).thenReturn(Collections.singletonList(variable));

        final String sensorType = SstInsituTimeSeries.extractSensorType(reader);

        assertEquals("sensor-name", sensorType);
    }

    @Test
    public void extractSensorType_DoesNotContainVariablesWithNameContainig_insituDot() throws Exception {
        final Variable v1 = mock(Variable.class);
        when(v1.getShortName()).thenReturn("DontContainInsituWithUnderscoreAndDot_1");

        final Variable v2 = mock(Variable.class);
        when(v2.getShortName()).thenReturn("DontContainInsituWithUnderscoreAndDot_2");

        final NetcdfFile reader = mock(NetcdfFile.class);
        when(reader.getVariables()).thenReturn(Arrays.asList(v1, v2));

        try {
            SstInsituTimeSeries.extractSensorType(reader);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Unable to extract sensor type.", expected.getMessage());
        }
    }

    @Test
    public void getFileNameVariable_Success() throws IOException {
        final NetcdfFile reader = mock(NetcdfFile.class);
        final Variable expectedVariable = mock(Variable.class);
        when(reader.findVariable("sensor-name_file_name")).thenReturn(expectedVariable);

        //action
        final Variable fileNameVariable = SstInsituTimeSeries.getFileNameVariable(reader, "sensor-name");

        assertSame(expectedVariable, fileNameVariable);
    }

    @Test
    public void getFileNameVariable_VariableDoesNotExist() throws IOException {
        final Variable variable = mock(Variable.class);
        when(variable.getShortName()).thenReturn("sensor-name_insitu.sonstwas");

        final ArrayList<Variable> variables = new ArrayList<>();
        variables.add(variable);

        final NetcdfFile reader = mock(NetcdfFile.class);
        when(reader.getVariables()).thenReturn(variables);

        try {
            SstInsituTimeSeries.getFileNameVariable(reader, SstInsituTimeSeries.extractSensorType(reader));
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), is(equalTo("Variable 'sensor-name_file_name' does not exist.")));
        }
    }

    @Test
    public void findDimensionMandatory_Success() throws Exception {
        final String dimName = "dimName";
        final NetcdfFile reader = mock(NetcdfFile.class);
        final Dimension expectedDim = new Dimension("dimName", 24);
        when(reader.findDimension(dimName)).thenReturn(expectedDim);

        final Dimension dimension = SstInsituTimeSeries.findDimensionMandatory(reader, dimName);

        assertSame(dimension, expectedDim);
    }

    @Test
    public void findDimensionMandatory_DimensionDoesNotExist() throws Exception {
        final String dimName = "dimName";
        final NetcdfFile reader = mock(NetcdfFile.class);
        when(reader.findDimension(dimName)).thenReturn(null);

        try {
            SstInsituTimeSeries.findDimensionMandatory(reader, dimName);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Dimension 'dimName' does not exist.", expected.getMessage());
        }
    }

    @Test
    public void extractStarEndDateFromInsituFilename() throws Exception {
        final String begin = "19700325";
        final String end = "19760625";

        final Date[] dates = SstInsituTimeSeries.extractStarEndDateFromInsituFilename(
                    "anyNameWith_yyyyMMdd_atTheLastTwoPositions_" + begin + "_" + end + ".anyExtension");

        assertEquals(2, dates.length);
        assertEquals("25-Mar-1970 00:00:00", TimeUtils.format(dates[0]));
        assertEquals("25-Jun-1976 00:00:00", TimeUtils.format(dates[1]));
    }

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
    public void getInsituFileName_Success() throws Exception {
        final Array array = mock(Array.class);

        final String validInsituFileName = "insitu_file_name_12345678_12345678.nc";
        when(array.getStorage()).thenReturn(Arrays.copyOf(validInsituFileName.toCharArray(), 180));

        final Variable fileNameVariable = mock(Variable.class);
        when(fileNameVariable.read(new int[]{0, 0}, new int[]{1, 180})).thenReturn(array);

        final String insituFileName = SstInsituTimeSeries.getInsituFileName(fileNameVariable, 0, 180);

        assertEquals(validInsituFileName, insituFileName);
    }

    @Test
    public void getInsituFileName_ThrowsRuntimeException_BecauseTheFileNameDoesNotMatchTheExpectedPattern() throws Exception {
        final String expression = SstInsituTimeSeries.D_8_D_8_NC;
        assertEquals(".*_\\d{8}_\\d{8}.nc", expression);
        final String invalidName = "invalid_insitu_file_name_12345678.nc";
        final String expectedErrorMessage =
                    "The insitu file name '" + invalidName + "' does not match the regular expression '" + expression + "'";

        final Array array = mock(Array.class);
        final Variable fileNameVariable = mock(Variable.class);
        when(array.getStorage()).thenReturn(Arrays.copyOf(invalidName.toCharArray(), 180));
        when(fileNameVariable.read(new int[]{0, 0}, new int[]{1, 180})).thenReturn(array);

        try {
            SstInsituTimeSeries.getInsituFileName(fileNameVariable, 0, 180);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals(expectedErrorMessage, expected.getMessage());
        }
    }

    @Test
    public void getInsituFileName_VariableThrowsInvalidRangeException_IsNotCatched() throws Exception {
        final Variable fileNameVariable = mock(Variable.class);
        when(fileNameVariable.read(new int[]{0, 0}, new int[]{1, 180})).thenThrow(new InvalidRangeException("mess"));

        try {
            SstInsituTimeSeries.getInsituFileName(fileNameVariable, 0, 180);
            fail("InvalidRangeException expected");
        } catch (InvalidRangeException expected) {
            assertEquals("mess", expected.getMessage());
        }
    }

    @Test
    public void getInsituFileName_VariableThrowsIOException_IsNotCatched() throws Exception {
        final Variable fileNameVariable = mock(Variable.class);
        when(fileNameVariable.read(new int[]{0, 0}, new int[]{1, 180})).thenThrow(new IOException("mess"));

        try {
            SstInsituTimeSeries.getInsituFileName(fileNameVariable, 0, 180);
            fail("IOException expected");
        } catch (IOException expected) {
            assertEquals("mess", expected.getMessage());
        }
    }

    @Test
    public void prepareImpl() throws Exception {
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
