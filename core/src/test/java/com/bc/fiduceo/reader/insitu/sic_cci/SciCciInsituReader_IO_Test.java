package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.reader.AcquisitionInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

            final AcquisitionInfo acquisitionInfo = reader.read();
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 1, 0, 0, acquisitionInfo.getSensingStart());
            TestUtil.assertCorrectUTCDate(2016, 12, 31, 16, 0, 0, acquisitionInfo.getSensingStop());

            assertEquals(NodeType.UNDEFINED, acquisitionInfo.getNodeType());
            assertNull(acquisitionInfo.getBoundingGeometry());
        } finally {
            reader.close();
        }
    }
}
