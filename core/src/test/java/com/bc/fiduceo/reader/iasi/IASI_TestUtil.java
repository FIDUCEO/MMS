package com.bc.fiduceo.reader.iasi;


import com.bc.fiduceo.TestUtil;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

class IASI_TestUtil {

    static long MDR_OFFSET_MA = 231818L;
    static long MDR_OFFSET_MB = 231818L;

    static File getIasiFile_MA_v5() throws IOException {
        final String testFilePath = com.bc.fiduceo.TestUtil.assembleFileSystemPath(new String[]{"iasi-ma", "v3-6N", "2016", "01", "IASI_xxx_1C_M02_20160101124754Z_20160101142658Z_N_O_20160101142620Z.nat"}, false);
        return getFile(testFilePath);
    }

    static File getIasiFile_MA_v4() throws IOException {
        final String testFilePath = com.bc.fiduceo.TestUtil.assembleFileSystemPath(new String[]{"iasi-ma", "v4-0N", "2009", "04", "IASI_xxx_1C_M02_20090406011456Z_20090406025952Z_N_O_20090406030227Z.nat"}, false);
        return getFile(testFilePath);
    }

    static File getIasiFile_MB() throws IOException {
        final String testFilePath = com.bc.fiduceo.TestUtil.assembleFileSystemPath(new String[]{"iasi-mb", "v7-0N", "2014", "04", "IASI_xxx_1C_M01_20140425124756Z_20140425142652Z_N_O_20140425133911Z.nat"}, false);
        return getFile(testFilePath);
    }

    private static File getFile(String testFilePath) throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}


