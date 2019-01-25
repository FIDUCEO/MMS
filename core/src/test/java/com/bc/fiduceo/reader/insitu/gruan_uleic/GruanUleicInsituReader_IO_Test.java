package com.bc.fiduceo.reader.insitu.gruan_uleic;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(IOTestRunner.class)
public class GruanUleicInsituReader_IO_Test {

    private GruanUleicInsituReader reader;

    @Before
    public void setUp() throws IOException {
        reader = new GruanUleicInsituReader();

        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "gruan-uleic", "v1.0", "nya_matchup_points.txt"}, false);
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File testFile = new File(testDataDirectory, relativePath);
        reader.open(testFile);
    }

    @After
    public void tearDown() throws IOException {
        reader.close();
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final AcquisitionInfo info = reader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(2009, 1, 1, 5, 54, 39, 744, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2018, 3, 27, 11, 9, 26, 400, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }
}
