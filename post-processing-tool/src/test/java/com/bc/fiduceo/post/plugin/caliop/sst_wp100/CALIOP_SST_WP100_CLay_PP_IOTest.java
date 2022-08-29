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
package com.bc.fiduceo.post.plugin.caliop.sst_wp100;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.post.PostProcessingToolMain;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.bc.fiduceo.post.plugin.caliop.sst_wp100.CALIOP_SST_WP100_CLay_PP.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(IOTestRunner.class)
public class CALIOP_SST_WP100_CLay_PP_IOTest {

    private static final String PREFIX_CALIOP_CLAY = "caliop_clay.";
    private final static String fiduceoTestDirPefix = "fiduceoTest";
    private CALIOP_SST_WP100_CLay_PP cLay_pp;
    private Path tempDirectory;

    @BeforeClass
    public static void beforeClass() {
        // This is needed because the NetcdfFileWriter does not release the written file after tests.
        deleteTempDirectoriesFromPreviousComputations();
    }

    private static boolean deleteTree(File tree) {
        File[] files = tree.listFiles();
        if (files != null) {
            for (File file : files) {
                file.deleteOnExit();
                if (file.isDirectory()) {
                    deleteTree(file);
                } else {
                    file.delete();
                }
            }
        }

        tree.deleteOnExit();
        return tree.delete();
    }

    private static void deleteTempDirectoriesFromPreviousComputations() {
        final File[] files = TestUtil.getTestDir().getParentFile().listFiles();
        for (File file : files) {
            if (file.getName().startsWith(fiduceoTestDirPefix)) {
                deleteTree(file.getAbsoluteFile());
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        cLay_pp = new CALIOP_SST_WP100_CLay_PP("caliop_vfm.file_name",
                "caliop_vfm.y",
                "4.10",
                PREFIX_CALIOP_CLAY);
        // In regular usage the PostProcessingContext will be set by post processing framework.
        cLay_pp.setContext(createPostProcessingContext());
        // a call setContext(...) generates an framework call to initReaderCache() method
        tempDirectory = Files.createTempDirectory(fiduceoTestDirPefix);

        ReaderFactory.create(new GeometryFactory(GeometryFactory.Type.S2), null, null, null);
    }

    @After
    public void tearDown() {
        cLay_pp.forTestsOnly_dispose();
        deleteTempDirectories();
    }

    @Test
    @Ignore // @todo 2 tb/tb can not really understand the reasons for failre - check later 2020-05-08
    public void prepare() throws Exception {
        final Path testDirPath = TestUtil.getTestDataDirectory().toPath();
        final Path relMmd15sst = Paths.get("post-processing", "mmd15sst", "mmd15_sst_drifter-sst_amsre-aq_caliop_vfm-cal_2008-149_2008-155.nc");
        final Path absMmd15sst = testDirPath.resolve(relMmd15sst);
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // open the NetCDF file with NetCDFUtils is needed
        // because the standard open mechanism changes the file size
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        NetcdfFile reader = NetCDFUtils.openReadOnly(absMmd15sst.toAbsolutePath().toString());

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        final Variable mVar = mock(Variable.class);
        when(writer.addVariable(isNull(), anyString(), isA(DataType.class), anyString())).thenReturn(mVar);

        //execution
        try {
            cLay_pp.prepare(reader, writer);
        } finally {
            reader.close();
        }

        //verification
        verify(writer, times(1)).addDimension(null, DIM_NAME_CLAY_NX, 1);
        verify(writer, times(1)).addDimension(null, DIM_NAME_CLAY_NX_10, 10);
        verify(writer, times(1)).addDimension(null, DIM_NAME_CLAY_NY, 21);

        verify(writer, times(27)).addVariable(isNull(), anyString(), isA(DataType.class), anyString());
        verify(mVar, times(111)).addAttribute(isA(Attribute.class));

        verifyNoMoreInteractions(mVar, writer);
    }

    @Test
    @Ignore // @todo 2 tb/tb can not really understand the reasons for failure - check later 2020-05-08
    public void compute() throws Exception {
        final Path configDirPath = Files.createDirectory(tempDirectory.resolve("config"));

        final Path outDirPath = Files.createDirectory(configDirPath.getParent().resolve("out"));
        final String jobFileName = writePostProcessingConfig(configDirPath, outDirPath);
        writeSystemConfig(configDirPath);

        final Path mmdTestFile = getMmdTestFile();

        PostProcessingToolMain.main(new String[]{
                "-c", configDirPath.toAbsolutePath().toString(),
                "-i", mmdTestFile.getParent().toAbsolutePath().toString(),
                "-start", "2008-100",
                "-end", "2008-200",
                "-j", jobFileName
        });

        final List<Path> paths = Files.list(outDirPath).collect(Collectors.toList());
        assertEquals(1, paths.size());
        assertNotNull(paths.get(0));
        assertEquals(outDirPath.resolve(mmdTestFile.getFileName()), paths.get(0));

        try (NetcdfFile netcdfFile = NetCDFUtils.openReadOnly(paths.get(0).toAbsolutePath().toString())) {
            final List<Variable> variables = netcdfFile.getVariables();
            int countClayVarNames = 0;
            for (Variable variable : variables) {
                final String shortName = variable.getShortName();
                if (shortName.startsWith(PREFIX_CALIOP_CLAY)) {
                    countClayVarNames++;
                }
            }
            assertEquals(27, countClayVarNames);
        }

        // @todo implement assertions for written data.
    }

    @Test
    public void insertDimensions() {
        //preparation
        final int ny = 25;
        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);

        //execution
        CALIOP_SST_WP100_CLay_PP.insertDimensions(writer, ny);

        //verification
        verify(writer, times(1)).addDimension(null, DIM_NAME_CLAY_NX, 1);
        verify(writer, times(1)).addDimension(null, DIM_NAME_CLAY_NX_10, 10);
        verify(writer, times(1)).addDimension(null, DIM_NAME_CLAY_NY, ny);
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void addVariables() throws Exception {
        //preparation
        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        final Variable mVar = mock(Variable.class);
        when(writer.addVariable(isNull(), anyString(), isA(DataType.class), anyString())).thenReturn(mVar);

        final ArrayList<Variable> variables = new ArrayList<>();
        variables.add(createVariableProxy("var1", DataType.INT, new int[]{12, 1}, new Attribute("a1", "v1")));
        variables.add(createVariableProxy("var2", DataType.FLOAT, new int[]{9, 10}));

        final Reader cLay_reader = mock(Reader.class);
        when(cLay_reader.getVariables()).thenReturn(variables);

        //execution
        cLay_pp.addVariables(writer, cLay_reader);

        //verification
        final InOrder o = inOrder(cLay_reader, writer, mVar);
        o.verify(cLay_reader).getVariables();
        o.verify(writer).addVariable(null, PREFIX_CALIOP_CLAY + "var1", DataType.INT, "matchup_count caliop_clay-cal_ny caliop_clay-cal_nx");
        o.verify(mVar).addAttribute(new Attribute("a1", "v1"));
        o.verify(writer).addVariable(null, PREFIX_CALIOP_CLAY + "var2", DataType.FLOAT, "matchup_count caliop_clay-cal_ny caliop_clay-cal_nx_10");

        o.verify(writer).addVariable(null, PREFIX_CALIOP_CLAY + "acquisition_time", DataType.INT, "matchup_count caliop_clay-cal_ny caliop_clay-cal_nx");
        o.verify(mVar).addAttribute(new Attribute("description", "acquisition time of original pixel"));
        o.verify(mVar).addAttribute(new Attribute("units", "seconds since 1970-01-01"));
        o.verify(mVar).addAttribute(new Attribute("_FillValue", NetCDFUtils.getDefaultFillValue(DataType.INT, false)));

        o.verify(writer).addVariable(null, PREFIX_CALIOP_CLAY + "file_name", DataType.CHAR, "matchup_count file_name");
        o.verify(mVar).addAttribute(new Attribute("description", "file name of the original data file"));

        o.verify(writer).addVariable(null, PREFIX_CALIOP_CLAY + "processing_version", DataType.CHAR, "matchup_count processing_version");
        o.verify(mVar).addAttribute(new Attribute("description", "the processing version of the original data file"));

        o.verify(writer).addVariable(null, PREFIX_CALIOP_CLAY + "x", DataType.INT, FiduceoConstants.MATCHUP_COUNT);
        o.verify(mVar).addAttribute(new Attribute("description", "pixel original x location in satellite raster"));
        o.verify(mVar).addAttribute(new Attribute("_FillValue", NetCDFUtils.getDefaultFillValue(DataType.INT, false)));

        o.verify(writer).addVariable(null, PREFIX_CALIOP_CLAY + "y", DataType.INT, FiduceoConstants.MATCHUP_COUNT);
        o.verify(mVar).addAttribute(new Attribute("description", "pixel original y location in satellite raster"));
        o.verify(mVar).addAttribute(new Attribute("_FillValue", NetCDFUtils.getDefaultFillValue(DataType.INT, false)));

        verifyNoMoreInteractions(mVar, cLay_reader, writer);
    }

    @Test
    public void addAttributes() {
        //preparation
        final Variable mVar = mock(Variable.class);
        final ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("a1", "v1"));
        attributes.add(new Attribute("a2", 2));
        attributes.add(new Attribute("a3", 3.0));

        //execution
        cLay_pp.addAttributes(mVar, attributes);

        //verification
        verify(mVar, times(1)).addAttribute(same(attributes.get(0)));
        verify(mVar, times(1)).addAttribute(same(attributes.get(1)));
        verify(mVar, times(1)).addAttribute(same(attributes.get(2)));
        verifyNoMoreInteractions(mVar);
    }

    private void deleteTempDirectories() {
        deleteTree(tempDirectory.toFile());
    }

    private void writeSystemConfig(Path configDirPath) throws IOException {
        final Path systemConfigPath = configDirPath.resolve("system-config.xml");
        final Path archivePath = TestUtil.getTestDataDirectory().toPath();
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(systemConfigPath)) {
            bufferedWriter.write("<system-config>\n" +
                    "    <geometry-library name=\"S2\"/>\n" +
                    "    <archive>\n" +
                    "        <root-path>" + archivePath.toAbsolutePath() + "</root-path>\n" +
                    "    </archive>\n" +
                    "</system-config>");
        }
    }

    private String writePostProcessingConfig(Path configDirPath, final Path outDirPath) throws IOException {

        final String name = "pp-config.xml";
        final Path postProcessingConfigPath = configDirPath.resolve(name);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(postProcessingConfigPath)) {
            bufferedWriter.write(
                    "<post-processing-config>\n" +
                            "    <create-new-files>\n" +
                            "        <output-directory>\n" +
                            "            " + outDirPath.toAbsolutePath() + "\n" +
                            "        </output-directory>\n" +
                            "    </create-new-files>\n" +
                            "    <post-processings>\n" +
                            "        <caliop-sst-wp100-clay>\n" +
                            "            <mmd-source-file-variable-name>caliop_vfm.file_name</mmd-source-file-variable-name>\n" +
                            "            <processing-version>4.10</processing-version>\n" +
                            "            <mmd-y-variable-name>caliop_vfm.y</mmd-y-variable-name>\n" +
                            "            <target-variable-prefix>" + PREFIX_CALIOP_CLAY + "</target-variable-prefix>\n" +
                            "        </caliop-sst-wp100-clay>\n" +
                            "    </post-processings>\n" +
                            "</post-processing-config>"
            );
        }
        return name;
    }

    private VariableProxy createVariableProxy(String name, ucar.ma2.DataType dataType, int[] shape, Attribute... attributes) {
        final ArrayList<Attribute> attributesList = new ArrayList<>();
        Collections.addAll(attributesList, attributes);
        final VariableProxy var1 = new VariableProxy(name, dataType, attributesList);
        var1.setShape(shape);
        return var1;
    }

    private Path getMmdTestFile() throws IOException {
        final String testDataRoot = TestUtil.getTestDataDirectory().getAbsolutePath();
        String filename = "mmd15_sst_drifter-sst_amsre-aq_caliop_vfm-cal_2008-149_2008-155.nc";
        return Paths.get(testDataRoot, "post-processing", "mmd15sst", filename);
    }

    private PostProcessingContext createPostProcessingContext() throws IOException {
        PostProcessingContext pp_context = new PostProcessingContext();
        pp_context.setSystemConfig(createSystemConfig());
        pp_context.setReaderFactory(ReaderFactory.get());
        return pp_context;
    }

    private SystemConfig createSystemConfig() throws IOException {
        final String archivePath = TestUtil.getTestDataDirectory().getAbsolutePath();
        return SystemConfig.load(new ByteArrayInputStream(
                ("<system-config>" +
                        "    <geometry-library name=\"S2\"/>" +
                        "    <reader-cache-size>24</reader-cache-size>" +
                        "    <archive>" +
                        "        <root-path>" + archivePath + "</root-path>" +
                        "    </archive>" +
                        "</system-config>").getBytes()
        ));
    }
}