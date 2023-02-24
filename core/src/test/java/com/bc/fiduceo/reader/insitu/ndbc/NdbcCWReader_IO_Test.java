package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.reader.AcquisitionInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

@RunWith(IOTestRunner.class)
public class NdbcCWReader_IO_Test {

    private NdbcCWReader reader;

    @Before
    public void setUp()  {
        reader = new NdbcCWReader();
    }

    @Test
    public void testReadAcquisitionInfo_oceanBuoy() throws IOException {
        final File testFile = getOCEAN_BUOY();

        try {
            reader.open(testFile);

            final AcquisitionInfo info = reader.read();
// @todo 1 tb/tb continue here 2023-02-24
            //TestUtil.assertCorrectUTCDate(2016, 5, 31, 23, 0, 0, 0, info.getSensingStart());
        } finally {
            reader.close();
        }
    }

    private static File getOCEAN_BUOY() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "ndbc", "ndbc-cw-ob", "v1", "2016", "42002c2016.txt"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}
