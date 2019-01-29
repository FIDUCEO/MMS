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

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.post.Constants;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.post.PostProcessingToolMain;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.reader.insitu.sst_cci.SSTInsituReader;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.util.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_SECONDARY_SENSOR_MATCHUP_TIME_VARIABLE;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_SST_INSITU_TIME_SERIES;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_TIME_RANGE_SECONDS;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_TIME_SERIES_SIZE;
import static com.bc.fiduceo.post.plugin.sstInsitu.SstInsituTimeSeriesPlugin.TAG_NAME_VERSION;
import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_LONG_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_UNITS_NAME;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(IOTestRunner.class)
public class SstInsituTimeSeries_IO_Test {

    private File configDir;
    private File dataDir;
    private File testDataDirectory;

    @Before
    public void setUp() throws Exception {
        testDataDirectory = TestUtil.getTestDataDirectory();
        final File testDir = new File(TestUtil.getTestDir(), "PostProcessingToolTest");
        configDir = new File(testDir, "config");
        dataDir = new File(testDir, "data");
    }

    @After
    public void tearDown() {
        final File testDir = TestUtil.getTestDir();
        if (testDir.isDirectory()) {
            FileUtils.deleteTree(testDir);
            assertFalse(testDir.exists());
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
        when(writer.addVariable(isNull(), anyString(), any(DataType.class), anyString())).thenReturn(newVariable);

        final PostProcessingContext context = new PostProcessingContext();
        context.setSystemConfig(SystemConfig.load(new ByteArrayInputStream(
                ("<system-config>" +
                        "    <geometry-library name = \"S2\" />" +
                        "    <archive>" +
                        "        <root-path>" +
                        "            " + testDataDirectory.getAbsolutePath() +
                        "        </root-path>" +
                        "        <rule sensors = \"animal-sst\">" +
                        "            insitu/SENSOR/VERSION" +
                        "        </rule>" +
                        "    </archive>" +
                        "</system-config>").getBytes())));

        context.setReaderFactory(ReaderFactory.create(new GeometryFactory(GeometryFactory.Type.S2), null));

        final SstInsituTimeSeries.Configuration configuration = new SstInsituTimeSeries.Configuration();
        configuration.processingVersion = "v03.3";
        configuration.timeRangeSeconds = 124;
        configuration.timeSeriesSize = 16;
        configuration.matchupTimeVarName = "matchupTimeVarName";
        final SstInsituTimeSeries insituTimeSeries = new SstInsituTimeSeries(configuration);
        insituTimeSeries.setContext(context);

        // method under test
        insituTimeSeries.prepare(reader, writer);

        // verification
        final String insituNtime = SstInsituTimeSeries.INSITU_NTIME;
        final String matchup = FiduceoConstants.MATCHUP_COUNT;
        final String dimString = matchup + " " + insituNtime;

        final InOrder inOrder = inOrder(writer, newVariable);
        inOrder.verify(writer, times(1)).addDimension(null, insituNtime, 16);

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.time", DataType.INT, dimString);
        inOrder.verify(newVariable, times(1)).addAll(anyList());

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.lat", DataType.FLOAT, dimString);
        inOrder.verify(newVariable, times(1)).addAll(anyList());

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.lon", DataType.FLOAT, dimString);
        inOrder.verify(newVariable, times(1)).addAll(anyList());

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.sea_surface_temperature", DataType.FLOAT, dimString);
        inOrder.verify(newVariable, times(1)).addAll(anyList());

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.sst_uncertainty", DataType.FLOAT, dimString);
        inOrder.verify(newVariable, times(1)).addAll(anyList());

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.sst_depth", DataType.FLOAT, dimString);
        inOrder.verify(newVariable, times(1)).addAll(anyList());

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.sst_qc_flag", DataType.SHORT, dimString);
        inOrder.verify(newVariable, times(1)).addAll(anyList());

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.sst_track_flag", DataType.SHORT, dimString);
        inOrder.verify(newVariable, times(1)).addAll(anyList());

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.mohc_id", DataType.INT, dimString);
        inOrder.verify(newVariable, times(1)).addAll(anyList());

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.id", DataType.LONG, dimString);
        inOrder.verify(newVariable, times(1)).addAll(anyList());

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.y", DataType.INT, dimString);
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute(CF_FILL_VALUE_NAME, -2147483647));

        inOrder.verify(writer, times(1)).addVariable(null, "insitu.dtime", DataType.INT, dimString);
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute(CF_UNITS_NAME, "seconds from matchup.time"));
        inOrder.verify(newVariable, times(1)).addAttribute(new Attribute(CF_FILL_VALUE_NAME, -2147483647));

        verifyNoMoreInteractions(writer);
        verifyNoMoreInteractions(newVariable);
    }

    @Test
    public void computeInsituRange() throws Exception {
        final int seconds = 80000;

        final SstInsituTimeSeries.Configuration configuration = new SstInsituTimeSeries.Configuration();
        configuration.processingVersion = "any";
        configuration.timeRangeSeconds = seconds;
        configuration.timeSeriesSize = 300;
        configuration.matchupTimeVarName = "matchupTimeVarName";
        final SstInsituTimeSeries insituTimeSeries = new SstInsituTimeSeries(configuration);

        final ReaderFactory readerFactory = ReaderFactory.create(new GeometryFactory("S2"), null); // we don't need temp file support here tb 2018-01-23
        final Reader insituReader = readerFactory.getReader("animal-sst");

        final File insituFile = testDataDirectory.toPath()
                .resolve("insitu")
                .resolve("animal-sst")
                .resolve("v03.3")
                .resolve("insitu_12_WMOID_11836_20040112_20041006.nc").toAbsolutePath().toFile();


        insituReader.open(insituFile);

        final SstInsituTimeSeries.Range range = insituTimeSeries.computeInsituRange(15, (SSTInsituReader) insituReader);

        assertNotNull(range);
        assertEquals(11, range.min);
        assertEquals(17, range.max);
    }

    @Test
    public void computeOneProduct() throws Exception {
        FiduceoLogger.setLevelSilent();
        if (!configDir.mkdirs()) {
            fail("Unable to create test directory");
        }


        final File systemConfigFile = new File(configDir, "system-config.xml");
        try (OutputStream stream = Files.newOutputStream(systemConfigFile.toPath())) {
            final Document systemConfig = new Document(
                    new Element("system-config").addContent(
                            new Element("archive").addContent(Arrays.asList(
                                    new Element("root-path").addContent(testDataDirectory.getAbsolutePath()),
                                    new Element("rule").setAttribute("sensors", "animal-sst").addContent("insitu/SENSOR/VERSION")
                            ))
                    )
            );
            new XMLOutputter(Format.getPrettyFormat()).output(systemConfig, stream);
        }

        final File postProcessingConfigFile = new File(configDir, "processing-config.xml");
        try (OutputStream stream = Files.newOutputStream(postProcessingConfigFile.toPath())) {
            final Document postProcConfig = new Document(
                    new Element("post-processing-config").addContent(Arrays.asList(
                            new Element("overwrite"),
                            new Element("post-processings").addContent(
                                    new Element(TAG_NAME_SST_INSITU_TIME_SERIES).addContent(Arrays.asList(
                                            new Element(TAG_NAME_VERSION).addContent("v03.3"),
                                            new Element(TAG_NAME_TIME_RANGE_SECONDS).addContent("" + 80000),
                                            new Element(TAG_NAME_TIME_SERIES_SIZE).addContent("10"),
                                            new Element(TAG_NAME_SECONDARY_SENSOR_MATCHUP_TIME_VARIABLE).addContent("amsre.acquisition_time")
                                    ))
                            )
                    ))
            );
            new XMLOutputter(Format.getPrettyFormat()).output(postProcConfig, stream);
        }

        if (!dataDir.mkdirs()) {
            fail("Unable to create test directory");
        }
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
                "-start", "2004-001",
                "-end", "2009-363",
                "-j", "processing-config.xml"
        };

        PostProcessingToolMain.main(args);

        NetcdfFile netcdfFile = null;
        try {
            netcdfFile = NetcdfFile.open(target.toAbsolutePath().toString());

            // **********  ID  ************
            final Variable insituId = netcdfFile.findVariable(escape("insitu.id"));
            assertEquals("matchup_count insitu.ntime", insituId.getDimensionsString());
            assertEquals(3, insituId.getAttributes().size());
            assertEquals(new Attribute(CF_FILL_VALUE_NAME, -32768L), insituId.findAttribute(CF_FILL_VALUE_NAME));
            assertEquals(new Attribute(CF_LONG_NAME, "unique matchup ID"), insituId.findAttribute(CF_LONG_NAME));
            assertEquals(new Attribute("comment", "this unique id is generated by combining YEAR, MONTH and mohc_id"), insituId.findAttribute("comment"));
            final Array idArr = insituId.read();
            assertEquals(DataType.LONG, idArr.getDataType());
            final int[] idShape = idArr.getShape();
            assertEquals(9, idShape[0]);
            assertEquals(10, idShape[1]);

            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented
            final long[] idStorage = (long[]) idArr.getStorage();
            final int fill = -32768;
            final long[] idExpecteds = new long[]{
                    2004010000531670L, fill, fill, fill, fill, fill, fill, fill, fill, fill,
                    2004010000531754L, 2004010000531778L, fill, fill, fill, fill, fill, fill, fill, fill,
                    2004010000531754L, 2004010000531778L, 2004010000531790L, fill, fill, fill, fill, fill, fill, fill,
                    2004010000531778L, 2004010000531790L, 2004010000531814L, fill, fill, fill, fill, fill, fill, fill,
                    2004010000531682L, 2004010000531694L, 2004010000531718L, fill, fill, fill, fill, fill, fill, fill,
                    2004010000531694L, 2004010000531718L, fill, fill, fill, fill, fill, fill, fill, fill,
                    2004010000531790L, 2004010000531814L, 2004010000531826L, 2004010000531850L, 2004010000531874L, fill, fill, fill, fill, fill,
                    2004010000531814L, 2004010000531826L, 2004010000531850L, 2004010000531874L, fill, fill, fill, fill, fill, fill,
                    2004010000531814L, 2004010000531826L, 2004010000531850L, 2004010000531874L, 2004010000531898L, fill, fill, fill, fill, fill
            };
            assertArrayEquals(idExpecteds, idStorage);

            // **********  dTIME  ************
            final Variable dtime = netcdfFile.findVariable(escape("insitu.dtime"));
            assertEquals("matchup_count insitu.ntime", dtime.getDimensionsString());
            assertEquals(2, dtime.getAttributes().size());
            final int fill2 = -2147483647;
            assertEquals(new Attribute(CF_FILL_VALUE_NAME, fill2), dtime.findAttribute(CF_FILL_VALUE_NAME));
            assertEquals(new Attribute(CF_UNITS_NAME, "seconds from matchup.time"), dtime.findAttribute(CF_UNITS_NAME));
            final Array dtimeArr = dtime.read();
            assertEquals(DataType.INT, dtimeArr.getDataType());
            final int[] dtimeShape = dtimeArr.getShape();
            assertEquals(9, dtimeShape[0]);
            assertEquals(10, dtimeShape[1]);

            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented
            final int[] dtimeStorage = (int[]) dtimeArr.getStorage();
            final int[] dtimeExpecteds = new int[]{
                    -8974, fill2, fill2, fill2, fill2, fill2, fill2, fill2, fill2, fill2,
                    -5398, 22981, fill2, fill2, fill2, fill2, fill2, fill2, fill2, fill2,
                    -39845, -11466, 2754, fill2, fill2, fill2, fill2, fill2, fill2, fill2,
                    -11469, 2751, 37072, fill2, fill2, fill2, fill2, fill2, fill2, fill2,
                    -4274, 766, 23387, fill2, fill2, fill2, fill2, fill2, fill2, fill2,
                    768, 23389, fill2, fill2, fill2, fill2, fill2, fill2, fill2, fill2,
                    -45850, -11529, -2349, 7311, 28251, fill2, fill2, fill2, fill2, fill2,
                    -11529, -2349, 7311, 28251, fill2, fill2, fill2, fill2, fill2, fill2,
                    -11526, -2346, 7314, 28254, 41813, fill2, fill2, fill2, fill2, fill2
            };
            assertArrayEquals(dtimeExpecteds, dtimeStorage);

            // **********  Latitude  ************
            final Variable latitude = netcdfFile.findVariable(escape("insitu.lat"));
            assertEquals("matchup_count insitu.ntime", latitude.getDimensionsString());
            assertEquals(3, latitude.getAttributes().size());
            // todo fix this test by replacing the insitu file with an insitu file new calculated by gery
//            assertEquals("degrees_north", latitude.findAttribute("units").getStringValue());
            assertEquals(new Attribute(CF_FILL_VALUE_NAME, -32768.0f), latitude.findAttribute(CF_FILL_VALUE_NAME));
            assertEquals(new Attribute(CF_LONG_NAME, "in situ latitude"), latitude.findAttribute(CF_LONG_NAME));
            final Array latArr = latitude.read();
            assertEquals(DataType.FLOAT, latArr.getDataType());
            final int[] latShape = latArr.getShape();
            assertEquals(9, latShape[0]);
            assertEquals(10, latShape[1]);

            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented
            final float[] latStorage = (float[]) latArr.getStorage();
            final float[] latExpecteds = new float[]{
                    -54.1789f, fill, fill, fill, fill, fill, fill, fill, fill, fill,
                    -54.392f, -54.7024f, fill, fill, fill, fill, fill, fill, fill, fill,
                    -54.392f, -54.7024f, -54.9134f, fill, fill, fill, fill, fill, fill, fill,
                    -54.7024f, -54.9134f, -55.169f, fill, fill, fill, fill, fill, fill, fill,
                    -54.0713f, -54.0569f, -54.0827f, fill, fill, fill, fill, fill, fill, fill,
                    -54.0569f, -54.0827f, fill, fill, fill, fill, fill, fill, fill, fill,
                    -54.9134f, -55.169f, -55.2213f, -55.3544f, -55.53f, fill, fill, fill, fill, fill,
                    -55.169f, -55.2213f, -55.3544f, -55.53f, fill, fill, fill, fill, fill, fill,
                    -55.169f, -55.2213f, -55.3544f, -55.53f, -55.7162f, fill, fill, fill, fill, fill
            };
            assertArrayEquals(latExpecteds, latStorage, 1e-7f);

            // **********  Longitude  ************
            final Variable longitude = netcdfFile.findVariable(escape("insitu.lon"));
            assertEquals("matchup_count insitu.ntime", longitude.getDimensionsString());
            assertEquals(3, longitude.getAttributes().size());
            // todo fix this test by replacing the insitu file with an insitu file new calculated by gery
//            assertEquals("degrees_east", longitude.findAttribute("units").getStringValue());
            assertEquals(new Attribute(CF_FILL_VALUE_NAME, -32768.0f), longitude.findAttribute(CF_FILL_VALUE_NAME));
            assertEquals(new Attribute(CF_LONG_NAME, "in situ longitude"), longitude.findAttribute(CF_LONG_NAME));
            final Array lonArr = longitude.read();
            assertEquals(DataType.FLOAT, lonArr.getDataType());
            final int[] lonShape = lonArr.getShape();
            assertEquals(9, lonShape[0]);
            assertEquals(10, lonShape[1]);

            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented
            final float[] lonStorage = (float[]) lonArr.getStorage();
            final float[] lonExpecteds = new float[]{
                    -36.6781f, fill, fill, fill, fill, fill, fill, fill, fill, fill,
                    -36.1543f, -35.8584f, fill, fill, fill, fill, fill, fill, fill, fill,
                    -36.1543f, -35.8584f, -35.6824f, fill, fill, fill, fill, fill, fill, fill,
                    -35.8584f, -35.6824f, -35.9283f, fill, fill, fill, fill, fill, fill, fill,
                    -36.7689f, -36.7764f, -36.8384f, fill, fill, fill, fill, fill, fill, fill,
                    -36.7764f, -36.8384f, fill, fill, fill, fill, fill, fill, fill, fill,
                    -35.6824f, -35.9283f, -36.1054f, -36.0165f, -36.3847f, fill, fill, fill, fill, fill,
                    -35.9283f, -36.1054f, -36.0165f, -36.3847f, fill, fill, fill, fill, fill, fill,
                    -35.9283f, -36.1054f, -36.0165f, -36.3847f, -36.5203f, fill, fill, fill, fill, fill
            };
            assertArrayEquals(lonExpecteds, lonStorage, 1e-7f);

            // **********  MOHC_ID  ************
            assertNotNull(netcdfFile.findVariable(escape("insitu.mohc_id")));
            // todo Check attributes, shape, datatype according to avhrr_f.m01-mmd12-2014-07.nc
            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented

            // **********  SEA_SURFACE_TEMPERATURE  ************
            final Variable temperature = netcdfFile.findVariable(escape("insitu.sea_surface_temperature"));
            assertNotNull(temperature);
            assertEquals("matchup_count insitu.ntime", temperature.getDimensionsString());
            assertEquals(3, temperature.getAttributes().size());
            assertEquals(new Attribute(CF_LONG_NAME, "in situ sea surface temperature"), temperature.findAttribute(CF_LONG_NAME));
            assertEquals(new Attribute(CF_UNITS_NAME, "Celcius"), temperature.findAttribute(CF_UNITS_NAME));
            assertEquals(new Attribute(CF_FILL_VALUE_NAME, -32768.0f), temperature.findAttribute(CF_FILL_VALUE_NAME));

            final Array tempArr = temperature.read();
            assertEquals(DataType.FLOAT, tempArr.getDataType());
            final int[] tempShape = tempArr.getShape();
            assertEquals(9, tempShape[0]);
            assertEquals(10, tempShape[1]);

            final float[] tempStorage = (float[]) tempArr.getStorage();
            final float[] tempExpecteds = new float[]{
                    3.21f, fill, fill, fill, fill, fill, fill, fill, fill, fill,
                    2.804f, 0.964f, fill, fill, fill, fill, fill, fill, fill, fill,
                    2.804f, 0.964f, 2.643f, fill, fill, fill, fill, fill, fill, fill,
                    0.964f, 2.643f, 2.619f, fill, fill, fill, fill, fill, fill, fill,
                    2.289f, 2.019f, 2.329f, fill, fill, fill, fill, fill, fill, fill,
                    2.019f, 2.329f, fill, fill, fill, fill, fill, fill, fill, fill,
                    2.643f, 2.619f, 2.719f, 2.984f, 2.799f, fill, fill, fill, fill, fill,
                    2.619f, 2.719f, 2.984f, 2.799f, fill, fill, fill, fill, fill, fill,
                    2.619f, 2.719f, 2.984f, 2.799f, 2.814f, fill, fill, fill, fill, fill
            };
            assertArrayEquals(tempExpecteds, tempStorage, 1e-8f);

            // todo Check the data of variable if SstInsituTimeSeries.compute() is implemented

            // **********  SST_UNCERTAINTY  ************
            final Variable uncertainty = netcdfFile.findVariable(escape("insitu.sst_uncertainty"));
            assertNotNull(uncertainty);
            assertEquals("matchup_count insitu.ntime", uncertainty.getDimensionsString());
            assertEquals(3, uncertainty.getAttributes().size());
            assertEquals(new Attribute(CF_LONG_NAME, "in situ sea surface temperature uncertainty"), uncertainty.findAttribute(CF_LONG_NAME));
            assertEquals(new Attribute(CF_UNITS_NAME, "Celcius"), uncertainty.findAttribute(CF_UNITS_NAME));
            assertEquals(new Attribute(CF_FILL_VALUE_NAME, -32768f), uncertainty.findAttribute(CF_FILL_VALUE_NAME));

            final Array uncertArr = uncertainty.read();
            assertEquals(DataType.FLOAT, uncertArr.getDataType());
            final int[] uncertShape = uncertArr.getShape();
            assertEquals(9, uncertShape[0]);
            assertEquals(10, uncertShape[1]);

            final float[] uncertStorage = (float[]) uncertArr.getStorage();
            final float[] uncertExpecteds = new float[]{
                    0.005f, fill, fill, fill, fill, fill, fill, fill, fill, fill,
                    0.005f, 0.005f, fill, fill, fill, fill, fill, fill, fill, fill,
                    0.005f, 0.005f, 0.005f, fill, fill, fill, fill, fill, fill, fill,
                    0.005f, 0.005f, 0.005f, fill, fill, fill, fill, fill, fill, fill,
                    0.005f, 0.005f, 0.005f, fill, fill, fill, fill, fill, fill, fill,
                    0.005f, 0.005f, fill, fill, fill, fill, fill, fill, fill, fill,
                    0.005f, 0.005f, 0.005f, 0.005f, 0.005f, fill, fill, fill, fill, fill,
                    0.005f, 0.005f, 0.005f, 0.005f, fill, fill, fill, fill, fill, fill,
                    0.005f, 0.005f, 0.005f, 0.005f, 0.005f, fill, fill, fill, fill, fill
            };
            assertArrayEquals(uncertExpecteds, uncertStorage, 1e-8f);
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
