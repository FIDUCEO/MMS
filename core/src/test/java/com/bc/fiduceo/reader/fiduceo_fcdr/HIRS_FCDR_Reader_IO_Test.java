package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class HIRS_FCDR_Reader_IO_Test {

    private File testDataDirectory;
    private HIRS_FCDR_Reader reader;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory();

        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        reader = new HIRS_FCDR_Reader(readerContext);
    }

    @After
    public void tearDown() throws IOException {
        reader.close();
    }

    @Test
    public void testReadAcquisitionInfo_NOAA07() throws IOException {
        final File file = createHirsNOAA07File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1983, 10, 4, 16, 24, 22, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1983, 10, 4, 18, 6, 14, sensingStop);

        } finally {
            reader.close();
        }
    }

    private File createHirsNOAA07File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-n07-fcdr", "v0.8rc1", "1983", "10", "04", "FIDUCEO_FCDR_L1C_HIRS2_NOAA07_19831004162422_19831004180614_EASY_v0.8rc1_fv2.0.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

    private File createHirsMetopAFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-ma-fcdr", "v0.8rc1", "2015", "03", "26", "FIDUCEO_FCDR_L1C_HIRS4_METOPA_20150326173656_20150326191810_EASY_v0.8rc1_fv2.0.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}
