package com.bc.fiduceo.post.plugin.gruan_uleic;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.post.PostProcessingToolMain;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static ucar.nc2.NetcdfFileWriter.Version.netcdf4;

@RunWith(IOTestRunner.class)
public class AddGruanSource_IntegrationTest {

    private static final int NUM_MATCHUPS = 6;
    private static final String MMD_NAME = "mmd36_gruan-uleic_hirs-n14_2010-004_2010-008.nc";
    
    private File outputDir;
    private File config_dir;
    private File testDirectory;
    private File postProcConfig;

    @Before
    public void setUp() throws IOException, InvalidRangeException {
        testDirectory = TestUtil.createTestDirectory();

        outputDir = new File(testDirectory, "output");

        writeMMDTestFile(testDirectory);
        writeConfigFiles(testDirectory);
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testCompute() throws IOException, InvalidRangeException {
        PostProcessingToolMain.main(new String[]{
                "-c", config_dir.toString(),
                "-i", testDirectory.toString(),
                "-j", postProcConfig.getAbsolutePath(),
                "-start", "2010-001",
                "-end", "2010-365"
        });

        final File targetFile = new File(outputDir, MMD_NAME);
        if (!targetFile.isFile()) {
            fail("Expected file not present: " + targetFile.getAbsolutePath());
        }

        try(final NetcdfFile target = NetCDFUtils.openReadOnly(targetFile.getAbsolutePath())){
            NCTestUtils.assertStringVariable("gruan-uleic_source-path", 0, "BAR/2009/BAR-RS-01_2_RS92-GDP_002_20090522T060000_1-000-001.nc", target);
            NCTestUtils.assertStringVariable("gruan-uleic_source-path", 1, "BAR/2009/BAR-RS-01_2_RS92-GDP_002_20090523T060000_1-000-001.nc", target);
            NCTestUtils.assertStringVariable("gruan-uleic_source-path", 2, "BAR/2009/BAR-RS-01_2_RS92-GDP_002_20090624T180000_1-000-001.nc", target);
            NCTestUtils.assertStringVariable("gruan-uleic_source-path", 3, "NAU/2011/NAU-RS-01_2_RS92-GDP_002_20110305T120000_1-000-001.nc", target);
            NCTestUtils.assertStringVariable("gruan-uleic_source-path", 4, "NAU/2011/NAU-RS-01_2_RS92-GDP_002_20110320T120000_1-000-001.nc", target);
            NCTestUtils.assertStringVariable("gruan-uleic_source-path", 5, "BAR/2014/BAR-RS-02_2_RS92-GDP_002_20140211T180000_1-000-001.nc", target);
        }
    }

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    private void writeMMDTestFile(File testDirectory) throws IOException, InvalidRangeException {
        final File mmdFileName = new File(testDirectory, MMD_NAME);

        final NetcdfFileWriter writer = NetcdfFileWriter.createNew(netcdf4, mmdFileName.getAbsolutePath());
        final Dimension dimFileName = writer.addDimension(null, "file_name", 128);
        final Dimension dimProcessingVersion = writer.addDimension(null, "processing_version", 30);
        final Dimension dimMatchupCount = writer.addDimension(null, FiduceoConstants.MATCHUP_COUNT, NUM_MATCHUPS);

        final Variable varFileName = writer.addVariable(null, "gruan-uleic_file_name", DataType.CHAR, Arrays.asList(dimMatchupCount, dimFileName));
        final Variable varProcessingVersion = writer.addVariable(null, "gruan-uleic_processing_version", DataType.CHAR, Arrays.asList(dimMatchupCount, dimProcessingVersion));
        final Variable varY = writer.addVariable(null, "gruan-uleic_y", DataType.INT, Arrays.asList(dimMatchupCount));

        writer.create();

        writer.write(varY, Array.factory(new int[]{
                127, 129, 157, 6733, 6741, 2078
        }));

        final char[][] file = {"nya_matchup_points.txt".toCharArray()};
        final char[][] version = {"v1.0".toCharArray()};

        final Array arrayFile = Array.factory(file);
        final Array arrayVersion = Array.factory(version);

        for (int i = 0; i < NUM_MATCHUPS; i++) {
            writer.write(varFileName, new int[]{i, 0}, arrayFile);
            writer.write(varProcessingVersion, new int[]{i, 0}, arrayVersion);
        }

        writer.flush();
        writer.close();
    }

    private void writeConfigFiles(File testDirectory) throws IOException {
        config_dir = new File(testDirectory, "config_dir");
        if (!config_dir.mkdirs()) {
            throw new IOException("unable to create test directory");
        }
        TestUtil.writeSystemConfig(config_dir);

        postProcConfig = new File(testDirectory, "post-processing-config.xml");
        if (!postProcConfig.createNewFile()) {
            throw new IOException("unable to create test file");
        }

        final FileOutputStream outStream = new FileOutputStream(postProcConfig);
        final PrintWriter pw = new PrintWriter(outStream);

        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<post-processing-config>");
        pw.println("    <create-new-files>");
        pw.println("        <output-directory>" + outputDir.getAbsolutePath() + "</output-directory>");
        pw.println("    </create-new-files>");
        pw.println("    <post-processings>");
        pw.println("        <add-gruan-source>");
        pw.println("            <target-variable name=\"gruan-uleic_source-path\" />");
        pw.println("            <y-variable name=\"gruan-uleic_y\" />");
        pw.println("            <file-name-variable name=\"gruan-uleic_file_name\" />");
        pw.println("            <processing-version-variable name=\"gruan-uleic_processing_version\" />");
        pw.println("        </add-gruan-source>");
        pw.println("    </post-processings>");
        pw.println("</post-processing-config>");

        pw.flush();
        pw.close();
    }
}
