package com.bc.fiduceo.reader.insitu.sirds_sst;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.reader.AcquisitionInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class SirdsInsituReader_IO_Test {

    private SirdsInsituReader insituReader;

    @Before
    public void setUp() throws Exception {
        insituReader = new SirdsInsituReader();
    }

    @After
    public void tearDown() throws Exception {
        insituReader.close();
    }

    @Test
    public void testReadAcquisitionInfo_drifter() throws Exception {
        openFile("SSTCCI2_refdata_drifter_201304.nc", "v1.0");

        final AcquisitionInfo info = insituReader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(2013, 4, 1, 0, 0, 0, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2013, 4, 30, 23, 58, 47, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testReadAcquisitionInfo_mooring() throws Exception {
        openFile("SSTCCI2_refdata_mooring_201602.nc", "v1.0");

        final AcquisitionInfo info = insituReader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(2016, 2, 1, 0, 0, 0, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2016, 2, 29, 23, 58, 11, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testReadAcquisitionInfo_xbt() throws Exception {
        openFile("SSTCCI2_refdata_xbt_200204.nc", "v1.0");

        final AcquisitionInfo info = insituReader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(2002, 4, 1, 0, 1, 12, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2002, 4, 30, 23, 43, 48, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    private void openFile(String fileName, String version) throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sirds", version, fileName}, false);
        final File insituDataFile = TestUtil.getTestDataFileAsserted(testFilePath);

        insituReader.open(insituDataFile);
    }
}
