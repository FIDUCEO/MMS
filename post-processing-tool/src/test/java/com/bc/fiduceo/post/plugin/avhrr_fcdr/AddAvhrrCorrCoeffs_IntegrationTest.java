package com.bc.fiduceo.post.plugin.avhrr_fcdr;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.post.PostProcessingToolMain;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class AddAvhrrCorrCoeffs_IntegrationTest {

    private File configDir;
    private File testDirectory;

    @Before
    public void setUp() throws IOException {
        testDirectory = TestUtil.createTestDirectory();
        configDir = new File(testDirectory, "config");
        if (!configDir.mkdir()) {
            fail("unable to create test directory: " + configDir.getAbsolutePath());
        }

        TestUtil.writeSystemConfig(configDir);
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testAddAvhrrCorrCoeffs() throws IOException {
        final File inputDir = getInputDirectory("mmd37");

        writeConfiguration();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2016-334", "-end", "2016-335",
                "-i", inputDir.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

        final File targetFile = new File(testDirectory, "mmd37_avhrr-ma-fcdr_iasi-ma_2016-334_2016-335.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(targetFile.getAbsolutePath())) {
            NCTestUtils.assertDimension("input_files", 2, mmd);
            NCTestUtils.assertDimension("swath_width", 409, mmd);
            NCTestUtils.assertDimension("line_correlation", 256, mmd);
            NCTestUtils.assertDimension("channels", 6, mmd);

//            NCTestUtils.assert4DVariable("iasi-mb_GS1cSpect", 0, 0, 0, 0, 5.123000009916723E-4, mmd);
//            NCTestUtils.assert4DVariable("iasi-mb_GS1cSpect", 1, 0, 0, 0, 5.151000223122537E-4, mmd);
//            NCTestUtils.assert4DVariable("iasi-mb_GS1cSpect", 2, 0, 0, 0, 5.19599998369813E-4, mmd);
//            NCTestUtils.assert4DVariable("iasi-mb_GS1cSpect", 3, 0, 0, 0, 5.481999833136797E-4, mmd);
//            NCTestUtils.assert4DVariable("iasi-mb_GS1cSpect", 4350, 0, 0, 0, 1.6140000298037194E-5, mmd);
//            NCTestUtils.assert4DVariable("iasi-mb_GS1cSpect", 4351, 0, 0, 0, 1.4739999642188195E-5, mmd);
//            NCTestUtils.assert4DVariable("iasi-mb_GS1cSpect", 8698, 0, 0, 0, 9.969209968386869E36, mmd);
//            NCTestUtils.assert4DVariable("iasi-mb_GS1cSpect", 8699, 0, 0, 0, 9.969209968386869E36, mmd);
        }
    }

    // the code below patches the original MMD file to contain only AVHRR file names that we have stored in our test-dataset
    // tb 2019-06-14
//    @Test
//    public void testPatchMMD() throws IOException, InvalidRangeException {
//        final File inputDir = getInputDirectory("mmd37");
//
//        final File mmd = new File(inputDir, "mmd37_avhrr-ma-fcdr_iasi-ma_2016-334_2016-335.nc");
//
//        NetcdfFileWriter writer = NetcdfFileWriter.openExisting(mmd.getAbsolutePath());
//        try {
//
//            final Variable fileNameVar = writer.findVariable("avhrr-ma-fcdr_file_name");
//            final int[] shape = fileNameVar.getShape();
//            final ArrayChar ac2 = new ArrayChar.D2(shape[0], shape[1]);
//            final Index index = ac2.getIndex();
//
//            for (int i = 0; i < shape[0]; i++) {
//                index.set(i);
//                if (i % 2 == 0) {
//                    ac2.setString(index, "FIDUCEO_FCDR_L1C_AVHRR_METOPA_20161108073729_20161108082817_EASY_vBeta_fv2.0.0.nc");
//                } else {
//                    ac2.setString(index, "FIDUCEO_FCDR_L1C_AVHRR_MTAC3A_20161108185739_20161108203900_EASY_v0.2Bet_fv2.0.0.nc");
//                }
//            }
//
//            writer.write(fileNameVar, ac2);
//
//        } finally {
//            writer.flush();
//            writer.close();
//        }
//    }

    private void writeConfiguration() throws IOException {
        final String postProcessingConfig = "<post-processing-config>\n" +
                "    <create-new-files>\n" +
                "        <output-directory>\n" +
                testDirectory.getAbsolutePath() +
                "        </output-directory>\n" +
                "    </create-new-files>\n" +
                "    <post-processings>\n" +
                "        <add-avhrr-corr-coeffs>\n" +
                "            <file-name-variable name=\"avhrr-ma-fcdr_file_name\" />" +
                "            <processing-version-variable name=\"avhrr-ma-fcdr_processing_version\" />" +
                "            <target-x-elem-variable name=\"avhrr-ma-fcdr_cross_element_correlation_coefficients\" />" +
                "            <target-x-line-variable name=\"avhrr-ma-fcdr_cross_line_correlation_coefficients\" />" +
                "        </add-avhrr-corr-coeffs>\n" +
                "    </post-processings>\n" +
                "</post-processing-config>";

        final File postProcessingConfigFile = new File(configDir, "post-processing-config.xml");
        if (!postProcessingConfigFile.createNewFile()) {
            fail("unable to create test file");
        }
        TestUtil.writeStringTo(postProcessingConfigFile, postProcessingConfig);
    }

    private File getInputDirectory(String inputDirName) throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File postProcessingDir = new File(testDataDirectory, "post-processing");
        return new File(postProcessingDir, inputDirName);
    }
}
