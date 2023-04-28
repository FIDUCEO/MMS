package com.bc.fiduceo.reader.insitu.tao;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class TaoReader_IO_Test {

    private TaoReader reader;

    @Before
    public void setUp() {
        reader = new TaoReader();
    }

    @After
    public void tearDown() throws IOException {
        reader.close();
    }

    @Test
    public void testReadAcquisitionInfo_TAO() throws Exception {
        final File insituDataFile = getTAOProduct();

        reader.open(insituDataFile);

        final AcquisitionInfo info = reader.read();
        TestUtil.assertCorrectUTCDate(2016, 6, 1, 0, 0, 0, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2016, 6, 30, 23, 0, 0, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }


    @Test
    public void testReadAcquisitionInfo_TRITON() throws Exception {
        final File insituDataFile = getTritonProduct();

        reader.open(insituDataFile);

        final AcquisitionInfo info = reader.read();
        TestUtil.assertCorrectUTCDate(2017, 10, 1, 12, 0, 0, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2017, 10, 31, 12, 0, 0, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testGetProductSize_TAO() throws IOException {
        final File insituDataFile = getTAOProduct();

        reader.open(insituDataFile);

        final Dimension productSize = reader.getProductSize();

        assertNotNull(productSize);
        assertEquals("product_size", productSize.getName());
        assertEquals(1, productSize.getNx());
        assertEquals(720, productSize.getNy());
    }

    @Test
    public void testGetProductSize_TRITON() throws IOException {
        final File insituDataFile = getTritonProduct();

        reader.open(insituDataFile);

        final Dimension productSize = reader.getProductSize();

        assertNotNull(productSize);
        assertEquals("product_size", productSize.getName());
        assertEquals(1, productSize.getNx());
        assertEquals(31, productSize.getNy());
    }

    @Test
    public void testGetTimeLocator_TAO() throws IOException {
        final File insituDataFile = getTAOProduct();

        reader.open(insituDataFile);

        final TimeLocator timeLocator = reader.getTimeLocator();
        assertEquals(1464739200000L, timeLocator.getTimeFor(0, 0));
        assertEquals(1465664400000L, timeLocator.getTimeFor(10, 257));
        assertEquals(1466982000000L, timeLocator.getTimeFor(20, 623));

        assertEquals(-1L, timeLocator.getTimeFor(20, -1));
        assertEquals(-1L, timeLocator.getTimeFor(20, 2567));
    }

    @Test
    public void testGetTimeLocator_TRITON() throws IOException {
        final File insituDataFile = getTritonProduct();

        reader.open(insituDataFile);

        final TimeLocator timeLocator = reader.getTimeLocator();
        assertEquals(1506859200000L, timeLocator.getTimeFor(0, 0));
        assertEquals(1508155200000L, timeLocator.getTimeFor(10, 15));
        assertEquals(1509451200000L, timeLocator.getTimeFor(20, 30));

        assertEquals(-1L, timeLocator.getTimeFor(20, -1));
        assertEquals(-1L, timeLocator.getTimeFor(20, 45));
    }

    private static File getTAOProduct() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "tao", "v1", "2016", "06", "TAO_T2S140W_DM167A-20160228_2016-06.txt"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private static File getTritonProduct() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "tao", "v1", "2017", "10", "TRITON_TR0N156E_1998_2017-10.txt"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
