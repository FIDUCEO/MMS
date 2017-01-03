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

import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_SST_INSITU_TIME_SERIES;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_TIME_RANGE_SECONDS;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_TIME_SERIES_SIZE;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_VERSION;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.post.PostProcessingConfig;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.post.PostProcessingToolMain;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.util.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.InOrder;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@RunWith(IOTestRunner.class)
public class SstInsituTimeSeries_IO_Test {

    private File configDir;
    private File dataDir;
    private File testDataDirectory;

    @Before
    public void setUp() throws Exception {
        testDataDirectory = TestUtil.getTestDataDirectory();
        File testDir = new File(TestUtil.getTestDir(), "PostProcessingToolTest");
        configDir = new File(testDir, "config");
        dataDir = new File(testDir, "data");
    }

    @After
    public void tearDown() throws Exception {
        final File testDir = TestUtil.getTestDir();
        if (testDir.isDirectory()) {
            FileUtils.deleteTree(testDir);
            assertFalse(testDir.exists());
        }
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

        final SstInsituTimeSeries sstInsituTimeSeries = new SstInsituTimeSeries("v03.3", 123, 12);
        final PostProcessingContext context = new PostProcessingContext();
        context.setSystemConfig(SystemConfig.load(inputStream));
        sstInsituTimeSeries.setContext(context);

        // action
        final Reader insituFileOpened = sstInsituTimeSeries
                    .getInsituFileOpened("insitu_12_WMOID_11835_20040110_20040127.nc", "animal-sst");

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

        // preparation
        final String input = testDataDirectory.toPath()
                    .resolve("post-processing")
                    .resolve("mmd06c")
                    .resolve("animal-sst_amsre-aq")
                    .resolve("mmd6c_sst_animal-sst_amsre-aq_2004-008_2004-014.nc").toAbsolutePath().toString();

        // open the file that way is needed because the standard open mechanism changes the file size
        final NetcdfFile reader = NetCDFUtils.openReadOnly(input);

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        final Attribute targetAttrib = new Attribute("use-case-configuration", "");
        when(writer.findGlobalAttribute("use-case-configuration")).thenReturn(targetAttrib);
        final Variable newVariable = mock(Variable.class);
        when(writer.addVariable(any(Group.class), anyString(), any(DataType.class), anyString())).thenReturn(newVariable);

        final PostProcessingContext context = new PostProcessingContext();
        context.setSystemConfig(SystemConfig.load(new ByteArrayInputStream(
                    ("<system-config>" +
                     "<geometry-library name = \"S2\" />" +
                     "    <archive>" +
                     "        <root-path>" +
                     "            " + testDataDirectory.getAbsolutePath() +
                     "        </root-path>" +
                     "        <rule sensors = \"animal-sst\">" +
                     "            insitu/SENSOR/VERSION" +
                     "        </rule>" +
                     "    </archive>" +
                     "</system-config>").getBytes())));
        context.setProcessingConfig(PostProcessingConfig.load(new ByteArrayInputStream(
                    ("<" + PostProcessingConfig.TAG_NAME_ROOT + ">" +
                     "<create-new-files>" +
                     "<output-directory>outDir</output-directory>" +
                     "</create-new-files>" +
                     "<post-processings>" +
                     "<dummy-post-processing>ABC</dummy-post-processing>" +
                     "</post-processings>" +
                     "</" + PostProcessingConfig.TAG_NAME_ROOT + ">").getBytes())));

        final SstInsituTimeSeries insituTimeSeries = new SstInsituTimeSeries("v03.3", 124, 16);
        insituTimeSeries.setContext(context);

        // method under test
        insituTimeSeries.prepare(reader, writer);

        // verification
        final String insituNtime = SstInsituTimeSeries.INSITU_NTIME;
        final String matchup = SstInsituTimeSeries.MATCHUP;
        final String dimString = matchup + " " + insituNtime;

        final InOrder inOrder = inOrder(writer, newVariable);
        inOrder.verify(writer, times(1)).addDimension(null, matchup, 9);
        inOrder.verify(writer, times(1)).addDimension(null, insituNtime, 16);

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.time", DataType.INT, dimString);
        inOrder.verify(newVariable, times(1)).addAll(any(List.class));

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.latitude", DataType.FLOAT, dimString);
        inOrder.verify(newVariable, times(1)).addAll(any(List.class));
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("valid_min", -90.0f));
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("valid_max", 90.0f));

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.longitude", DataType.FLOAT, dimString);
        inOrder.verify(newVariable, times(1)).addAll(any(List.class));
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("valid_min", -180.0f));
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("valid_max", 180.0f));

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.sea_surface_temperature", DataType.SHORT, dimString);
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("units", "K"));
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("add_offset", 293.15));
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("scale_factor", 0.001));
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("_FillValue", (short) -32768));
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("valid_min", (short) -22000));
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("valid_max", (short) 31850));
        inOrder.verify(newVariable, times(1)).addAttribute(any(Attribute.class));

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.sst_uncertainty", DataType.SHORT, dimString);
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("units", "K"));
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("add_offset", 0.0));
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("scale_factor", 0.001));
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("_FillValue", (short) -32768));
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("valid_min", (short) -22000));
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("valid_max", (short) 22000));
        inOrder.verify(newVariable, times(1)).addAttribute(any(Attribute.class));

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.sst_depth", DataType.FLOAT, dimString);
        inOrder.verify(newVariable, times(1)).addAll(any(List.class));

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.sst_qc_flag", DataType.SHORT, dimString);
        inOrder.verify(newVariable, times(1)).addAll(any(List.class));

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.sst_track_flag", DataType.SHORT, dimString);
        inOrder.verify(newVariable, times(1)).addAll(any(List.class));

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.mohc_id", DataType.INT, dimString);
        inOrder.verify(newVariable, times(1)).addAll(any(List.class));

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.id", DataType.LONG, dimString);
        inOrder.verify(newVariable, times(1)).addAll(any(List.class));

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.y", DataType.INT, dimString);

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.dtime", DataType.INT, dimString);
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("units", "seconds from matchup.time"));
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute("_FillValue", -2147483648));

        verifyNoMoreInteractions(writer);
        verifyNoMoreInteractions(newVariable);
    }

    @Test
    public void computeOneProduct() throws Exception {
        final Logger logger = FiduceoLogger.getLogger();
        FiduceoLogger.setLevelSilent();
        configDir.mkdirs();


        final File systemConfigFile = new File(configDir, "system-config.xml");
        try (OutputStream stream = Files.newOutputStream(systemConfigFile.toPath())) {
            final Document dom = new Document(
                        new Element("system-config").addContent(
                                    new Element("archive").addContent(Arrays.asList(
                                                new Element("root-path").addContent(testDataDirectory.getAbsolutePath()),
                                                new Element("rule").setAttribute("sensors", "animal-sst").addContent("insitu/SENSOR/VERSION")
                                    ))
                        )
            );
            new XMLOutputter(Format.getPrettyFormat()).output(dom, stream);
        }


        final File postProcessingConfigFile = new File(configDir, "processing-config.xml");
        try (OutputStream stream = Files.newOutputStream(postProcessingConfigFile.toPath())) {
            final Document dom = new Document(
                        new Element("post-processing-config").addContent(Arrays.asList(
                                    new Element("overwrite"),
                                    new Element("post-processings").addContent(
                                                new Element(TAG_NAME_SST_INSITU_TIME_SERIES).addContent(Arrays.asList(
                                                            new Element(TAG_NAME_VERSION).addContent("v03.3"),
                                                            new Element(TAG_NAME_TIME_RANGE_SECONDS).addContent("" + 36 * 60 * 60),
                                                            new Element(TAG_NAME_TIME_SERIES_SIZE).addContent("10")
                                                ))
                                    )
                        ))
            );
            new XMLOutputter(Format.getPrettyFormat()).output(dom, stream);
        }

        dataDir.mkdirs();
        final String filename = "mmd6c_sst_animal-sst_amsre-aq_2004-008_2004-014.nc";

        final Path src = testDataDirectory.toPath()
                    .resolve("post-processing")
                    .resolve("mmd06c")
                    .resolve("animal-sst_amsre-aq")
                    .resolve(filename);
        final Path target = dataDir.toPath().resolve(filename);
        Files.copy(src, target);

        final String[] args = {
                    "-c", configDir.getAbsolutePath(),
                    "-i", dataDir.getAbsolutePath(),
                    "-start", "2004-008",
                    "-end", "2004-014",
                    "-j", "processing-config.xml"
        };

        PostProcessingToolMain.main(args);


        NetcdfFile netcdfFile = null;
        try {
            netcdfFile = NetcdfFile.open(target.toAbsolutePath().toString());

            // **********  ID  ************
            final Variable insituId = netcdfFile.findVariable(escape("insitu.id"));
            assertEquals("matchup insitu.ntime", insituId.getDimensionsString());
            assertEquals(3, insituId.getAttributes().size());
            assertEquals(new Attribute("_FillValue", -32768L), insituId.findAttribute("_FillValue"));
            assertEquals(new Attribute("long_name", "unique matchup ID"), insituId.findAttribute("long_name"));
            assertEquals(new Attribute("comment", "this unique id is generated by combining YEAR, MONTH and mohc_id"), insituId.findAttribute("comment"));
            final Array idArr = insituId.read();
            assertEquals(DataType.LONG, idArr.getDataType());
            final int[] idShape = idArr.getShape();
            assertEquals(9, idShape[0]);
            assertEquals(10, idShape[1]);

            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented
            final long[] idStorage = (long[]) idArr.getStorage();
            final long[] idExpecteds = new long[90];
            Arrays.fill(idExpecteds, -32768);
            assertArrayEquals(idExpecteds, idStorage);

            // **********  dTIME  ************
            final Variable dtime = netcdfFile.findVariable(escape("insitu.dtime"));
            assertEquals("matchup insitu.ntime", dtime.getDimensionsString());
            assertEquals(2, dtime.getAttributes().size());
            assertEquals(new Attribute("_FillValue", -2147483648), dtime.findAttribute("_FillValue"));
            assertEquals(new Attribute("units", "seconds from matchup.time"), dtime.findAttribute("units"));
            final Array dtimeArr = dtime.read();
            assertEquals(DataType.INT, dtimeArr.getDataType());
            final int[] dtimeShape = dtimeArr.getShape();
            assertEquals(9, dtimeShape[0]);
            assertEquals(10, dtimeShape[1]);

            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented
            final int[] dtimeStorage = (int[]) dtimeArr.getStorage();
            final int[] dtimeExpecteds = new int[90];
            Arrays.fill(dtimeExpecteds, -2147483648);
            assertArrayEquals(dtimeExpecteds, dtimeStorage);

            // **********  Latitude  ************
            final Variable latitude = netcdfFile.findVariable(escape("insitu.latitude"));
            assertEquals("matchup insitu.ntime", latitude.getDimensionsString());
            assertEquals(5, latitude.getAttributes().size());
            // todo fix this test by replacing the insitu file with an insitu file new calculated by gery
//            assertEquals("degrees_north", latitude.findAttribute("units").getStringValue());
            assertEquals(new Attribute("_FillValue", -32768.0f), latitude.findAttribute("_FillValue"));
            assertEquals(new Attribute("valid_min", -90.0f), latitude.findAttribute("valid_min"));
            assertEquals(new Attribute("valid_max", 90.0f), latitude.findAttribute("valid_max"));
            assertEquals(new Attribute("long_name", "in situ latitude"), latitude.findAttribute("long_name"));
            final Array latArr = latitude.read();
            assertEquals(DataType.FLOAT, latArr.getDataType());
            final int[] latShape = latArr.getShape();
            assertEquals(9, latShape[0]);
            assertEquals(10, latShape[1]);

            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented
            final float[] latStorage = (float[]) latArr.getStorage();
            final float[] latExpecteds = new float[90];
            Arrays.fill(latExpecteds, -32768.0f);
            assertArrayEquals(latExpecteds, latStorage, 1e-7f);

            // **********  Longitude  ************
            final Variable longitude = netcdfFile.findVariable(escape("insitu.longitude"));
            assertEquals("matchup insitu.ntime", longitude.getDimensionsString());
            assertEquals(5, longitude.getAttributes().size());
            // todo fix this test by replacing the insitu file with an insitu file new calculated by gery
//            assertEquals("degrees_east", longitude.findAttribute("units").getStringValue());
            assertEquals(new Attribute("_FillValue",-32768.0f), longitude.findAttribute("_FillValue"));
            assertEquals(new Attribute("valid_min", -180.0f), longitude.findAttribute("valid_min"));
            assertEquals(new Attribute("valid_max", 180.0f), longitude.findAttribute("valid_max"));
            assertEquals(new Attribute("long_name", "in situ longitude"), longitude.findAttribute("long_name"));
            final Array lonArr = longitude.read();
            assertEquals(DataType.FLOAT, lonArr.getDataType());
            final int[] lonShape = lonArr.getShape();
            assertEquals(9, lonShape[0]);
            assertEquals(10, lonShape[1]);

            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented
            final float[] lonStorage = (float[]) lonArr.getStorage();
            final float[] lonExpecteds = new float[90];
            Arrays.fill(lonExpecteds, -32768.0f);
            assertArrayEquals(lonExpecteds, lonStorage, 1e-7f);

            // **********  MOHC_ID  ************
            assertNotNull(netcdfFile.findVariable(escape("insitu.mohc_id")));
            // todo Check attributes, shape, datatype according to avhrr_f.m01-mmd12-2014-07.nc
            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented

            // **********  SEA_SURFACE_TEMPERATURE  ************
            final Variable temperature = netcdfFile.findVariable(escape("insitu.sea_surface_temperature"));
            assertNotNull(temperature);
            assertEquals("matchup insitu.ntime", temperature.getDimensionsString());
            assertEquals(7, temperature.getAttributes().size());
            assertEquals(new Attribute("long_name", "in situ sea surface temperature"), temperature.findAttribute("long_name"));
            assertEquals(new Attribute("units", "K"), temperature.findAttribute("units"));
            assertEquals(new Attribute("add_offset", 293.15), temperature.findAttribute("add_offset"));
            assertEquals(new Attribute("scale_factor", 0.001), temperature.findAttribute("scale_factor"));
            assertEquals(new Attribute("_FillValue", (short) -32768), temperature.findAttribute("_FillValue"));
            assertEquals(new Attribute("valid_min", (short) -22000), temperature.findAttribute("valid_min"));
            assertEquals(new Attribute("valid_max", (short) 31850), temperature.findAttribute("valid_max"));

            final Array tempArr = temperature.read();
            assertEquals(DataType.SHORT, tempArr.getDataType());
            final int[] tempShape = tempArr.getShape();
            assertEquals(9, tempShape[0]);
            assertEquals(10, tempShape[1]);

            final short[] tempStorage = (short[]) tempArr.getStorage();
            final short[] tempExpecteds = new short[90];
            Arrays.fill(tempExpecteds, (short) -32768);
            assertArrayEquals(tempExpecteds, tempStorage);

            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented

            // **********  SST_UNCERTAINTY  ************
            final Variable uncertainty = netcdfFile.findVariable(escape("insitu.sst_uncertainty"));
            assertNotNull(uncertainty);
            assertEquals("matchup insitu.ntime", uncertainty.getDimensionsString());
            assertEquals(7, uncertainty.getAttributes().size());
            assertEquals(new Attribute("long_name", "in situ sea surface temperature uncertainty"), uncertainty.findAttribute("long_name"));
            assertEquals(new Attribute("units", "K"), uncertainty.findAttribute("units"));
            assertEquals(new Attribute("add_offset", 0.0), uncertainty.findAttribute("add_offset"));
            assertEquals(new Attribute("scale_factor", 0.001), uncertainty.findAttribute("scale_factor"));
            assertEquals(new Attribute("_FillValue", (short) -32768), uncertainty.findAttribute("_FillValue"));
            assertEquals(new Attribute("valid_min", (short) -22000), uncertainty.findAttribute("valid_min"));
            assertEquals(new Attribute("valid_max", (short) 22000), uncertainty.findAttribute("valid_max"));

            final Array uncertArr = uncertainty.read();
            assertEquals(DataType.SHORT, uncertArr.getDataType());
            final int[] uncertShape = uncertArr.getShape();
            assertEquals(9, uncertShape[0]);
            assertEquals(10, uncertShape[1]);

            final short[] uncertStorage = (short[]) uncertArr.getStorage();
            final short[] uncertExpecteds = new short[90];
            Arrays.fill(uncertExpecteds, (short) -32768);
            assertArrayEquals(uncertExpecteds, uncertStorage);
            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented

            // **********  SST_DEPTH  ************
            assertNotNull(netcdfFile.findVariable(escape("insitu.sst_depth")));
            // todo Check attributes, shape, datatype according to avhrr_f.m01-mmd12-2014-07.nc
            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented

            // **********  SST_QC_FLAG  ************
            assertNotNull(netcdfFile.findVariable(escape("insitu.sst_qc_flag")));
            // todo Check attributes, shape, datatype according to avhrr_f.m01-mmd12-2014-07.nc
            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented

            // **********  SST_TRACK_FLAG  ************
            assertNotNull(netcdfFile.findVariable(escape("insitu.sst_track_flag")));
            // todo Check attributes, shape, datatype according to avhrr_f.m01-mmd12-2014-07.nc
            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented

            // **********  Y  ************
            assertNotNull(netcdfFile.findVariable(escape("insitu.y")));
            // todo Check attributes, shape, datatype according to avhrr_f.m01-mmd12-2014-07.nc
            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented
        } finally {
            if (netcdfFile != null) {
                netcdfFile.close();
            }
        }
    }

    private String escape(String name) {
        return NetcdfFile.makeValidCDLName(name);
    }
}
