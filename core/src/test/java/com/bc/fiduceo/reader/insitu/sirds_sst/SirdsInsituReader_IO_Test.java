package com.bc.fiduceo.reader.insitu.sirds_sst;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.reader.AcquisitionInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
        openFile("SSTCCI2_refdata_drifter_201304.nc");

        final AcquisitionInfo info = insituReader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(2013, 4, 1, 0, 0, 0, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2013, 4, 30, 23, 58, 47, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testReadAcquisitionInfo_mooring() throws Exception {
        openFile("SSTCCI2_refdata_mooring_201602.nc");

        final AcquisitionInfo info = insituReader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(2016, 2, 1, 0, 0, 0, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2016, 2, 29, 23, 58, 11, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testReadAcquisitionInfo_xbt() throws Exception {
        openFile("SSTCCI2_refdata_xbt_200204.nc");

        final AcquisitionInfo info = insituReader.read();
        assertNotNull(info);

        TestUtil.assertCorrectUTCDate(2002, 4, 1, 0, 1, 12, 0, info.getSensingStart());
        TestUtil.assertCorrectUTCDate(2002, 4, 30, 23, 43, 48, 0, info.getSensingStop());

        assertEquals(NodeType.UNDEFINED, info.getNodeType());

        assertNull(info.getBoundingGeometry());
    }

    @Test
    public void testGetVariables_drifter() throws Exception {
        openFile("SSTCCI2_refdata_drifter_201304.nc");
        final List<Variable> variables = insituReader.getVariables();

        assertNotNull(variables);
        assertEquals(18, variables.size());
        assertEquals("SUBCOL1", variables.get(1).getShortName());
        assertEquals("PROF_ID", variables.get(3).getShortName());
        assertEquals("LATITUDE", variables.get(5).getShortName());
        assertEquals("DEPTH_CORR", variables.get(7).getShortName());
        assertEquals("SST_TYPE_CORR", variables.get(9).getShortName());
        assertEquals("SST_PLAT_CORR", variables.get(11).getShortName());
        assertEquals("SST_RAND_UNC", variables.get(13).getShortName());
        assertEquals("QC1", variables.get(15).getShortName());
        assertEquals("unique_id", variables.get(17).getShortName());
    }

    @Test
    public void testReadRaw_drifter() throws Exception {
        openFile("SSTCCI2_refdata_drifter_201304.nc");
        final Array array = insituReader.readRaw(0, 0, new Interval(1, 1), "DEPTH");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.DOUBLE, array.getDataType());
        assertEquals(0.2, array.getFloat(0), 1e-8);
    }

    @Test
    public void testReadRaw_mooring() throws Exception {
        openFile("SSTCCI2_refdata_mooring_201602.nc");
        final Array array = insituReader.readRaw(0, 1, new Interval(1, 1), "DEPTH_CORR");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.DOUBLE, array.getDataType());
        assertEquals(-9999.0, array.getFloat(0), 1e-8);
    }

    @Test
    public void testReadRaw_xbt() throws Exception {
        openFile("SSTCCI2_refdata_xbt_200204.nc");
        final Array array = insituReader.readRaw(0, 2, new Interval(1, 1), "LATITUDE");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.DOUBLE, array.getDataType());
        assertEquals(2.8117001056671143, array.getFloat(0), 1e-8);
    }

    @Test
    public void testReadRaw_drifter_3x3() throws Exception {
        openFile("SSTCCI2_refdata_drifter_201304.nc");
        final Array array = insituReader.readRaw(0, 3, new Interval(3, 3), "LONGITUDE");

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.DOUBLE, array.getDataType());
        assertEquals(-9999.0, array.getFloat(0), 1e-8);
        assertEquals(-9999.0, array.getFloat(1), 1e-8);
        assertEquals(-9999.0, array.getFloat(2), 1e-8);
        assertEquals(-9999.0, array.getFloat(3), 1e-8);
        assertEquals(31.93000030517578, array.getFloat(4), 1e-8);
        assertEquals(-9999.0, array.getFloat(5), 1e-8);
        assertEquals(-9999.0, array.getFloat(6), 1e-8);
        assertEquals(-9999.0, array.getFloat(7), 1e-8);
        assertEquals(-9999.0, array.getFloat(8), 1e-8);
    }

    @Test
    public void testReadRaw_mooring_3x3() throws Exception {
        openFile("SSTCCI2_refdata_mooring_201602.nc");
        final Array array = insituReader.readRaw(0, 4, new Interval(3, 3), "SST");

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-9999.f, array.getFloat(0), 1e-8);
        assertEquals(-9999.f, array.getFloat(1), 1e-8);
        assertEquals(-9999.f, array.getFloat(2), 1e-8);
        assertEquals(-9999.f, array.getFloat(3), 1e-8);
        assertEquals(3.2f, array.getFloat(4), 1e-8);
        assertEquals(-9999.f, array.getFloat(5), 1e-8);
        assertEquals(-9999.f, array.getFloat(6), 1e-8);
        assertEquals(-9999.f, array.getFloat(7), 1e-8);
        assertEquals(-9999.f, array.getFloat(8), 1e-8);
    }

    @Test
    public void testReadScaled_xbt() throws Exception {
        openFile("SSTCCI2_refdata_xbt_200204.nc");
        final Array array = insituReader.readScaled(0, 5, new Interval(1, 1), "SST_COMB_UNC");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(0.15f, array.getFloat(0), 1e-8);
    }

    @Test
    public void testReadScaled_drifter_3x3() throws Exception {
        openFile("SSTCCI2_refdata_drifter_201304.nc");
        final Array array = insituReader.readScaled(0, 6, new Interval(3, 3), "SST_RAND_UNC");

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(-9999.f, array.getFloat(0), 1e-8);
        assertEquals(-9999.f, array.getFloat(1), 1e-8);
        assertEquals(-9999.f, array.getFloat(2), 1e-8);
        assertEquals(-9999.f, array.getFloat(3), 1e-8);
        assertEquals(0.26f, array.getFloat(4), 1e-8);
        assertEquals(-9999.f, array.getFloat(5), 1e-8);
        assertEquals(-9999.f, array.getFloat(6), 1e-8);
        assertEquals(-9999.f, array.getFloat(7), 1e-8);
        assertEquals(-9999.f, array.getFloat(8), 1e-8);
    }

    @Test
    public void testReadRaw_mooring_uniqueId() throws Exception {
        openFile("SSTCCI2_refdata_mooring_201602.nc");
        final Array idArray = insituReader.readRaw(0, 7, new Interval(1, 1), "unique_id");

        assertNotNull(idArray);
        assertEquals(DataType.LONG, idArray.getDataType());

        NCTestUtils.assertValueAt(2016020000000098L, 0, 0, idArray);
    }

    @Test
    public void testReadRaw_xbt_uniqueId() throws Exception {
        openFile("SSTCCI2_refdata_xbt_200204.nc");
        final Array idArray = insituReader.readRaw(0, 8, new Interval(1, 1), "unique_id");

        assertNotNull(idArray);
        assertEquals(DataType.LONG, idArray.getDataType());

        NCTestUtils.assertValueAt(2002040001657855L, 0, 0, idArray);
    }

    private void openFile(String fileName) throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sirds", "v1.0", fileName}, false);
        final File insituDataFile = TestUtil.getTestDataFileAsserted(testFilePath);

        insituReader.open(insituDataFile);
    }
}
