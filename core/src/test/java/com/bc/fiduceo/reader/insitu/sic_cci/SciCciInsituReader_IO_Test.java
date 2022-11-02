package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

@RunWith(IOTestRunner.class)
public class SciCciInsituReader_IO_Test {

    private SicCciInsituReader reader;

    @Before
    public void setUp() {
        reader = new SicCciInsituReader();
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sic-cci", "DMI_SIC0", "v3", "ASCAT-vs-AMSR2-vs-ERA5-vs-DMISIC0-2016-N.text"}, false);
        final File testFile = TestUtil.getTestDataFileAsserted(relativePath);

        try {
            reader.open(testFile);
        } finally {
            reader.close();
        }
    }
}
