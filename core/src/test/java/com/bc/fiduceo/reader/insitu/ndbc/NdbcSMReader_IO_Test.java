package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

@RunWith(IOTestRunner.class)
public class NdbcSMReader_IO_Test {

    private NdbcSMReader reader;

    @Before
    public void setUp()  {
        reader = new NdbcSMReader();
    }

    @Test
    public void testReadAcquisitionInfo_coastBuoy() throws IOException {
        final File testFile = getCOAST_BUOY();

        try {
            reader.open(testFile);
        } finally {
            reader.close();
        }
    }

    private static File getCOAST_BUOY() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "ndbc", "ndbc-sm-cb", "v1", "2017", "42088h2017.txt"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}
