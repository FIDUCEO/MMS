package com.bc.fiduceo.reader.insitu.sirds_sst;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class SirdsInsituReader_IO_Test {

    private SirdsInsituReader insituReader;

    @Before
    public void setUp() throws Exception {
        insituReader = null;
    }

    @After
    public void tearDown() throws Exception {
        if (insituReader != null) {
            insituReader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_drifter() throws Exception {
        insituReader = new SirdsInsituReader("drifter");
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
        insituReader = new SirdsInsituReader("mooring");
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
        insituReader = new SirdsInsituReader("xbt");
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
        insituReader = new SirdsInsituReader("drifter");
        openFile("SSTCCI2_refdata_drifter_201304.nc");
        final List<Variable> variables = insituReader.getVariables();

        assertNotNull(variables);
        assertEquals(19, variables.size());
        assertEquals("subcol1", variables.get(1).getShortName());
        assertEquals("prof_id", variables.get(3).getShortName());
        assertEquals("longitude", variables.get(5).getShortName());
        assertEquals("depth", variables.get(7).getShortName());
        assertEquals("sst", variables.get(9).getShortName());
        assertEquals("sst_type_corr_unc", variables.get(11).getShortName());
        assertEquals("sst_plat_corr_unc", variables.get(13).getShortName());
        assertEquals("sst_comb_unc", variables.get(15).getShortName());
        assertEquals("qc2", variables.get(17).getShortName());
        assertEquals("unique_id", variables.get(18).getShortName());
    }

    @Test
    public void testReadRaw_drifter() throws Exception {
        insituReader = new SirdsInsituReader("drifter");
        openFile("SSTCCI2_refdata_drifter_201304.nc");
        final Array array = insituReader.readRaw(0, 0, new Interval(1, 1), "depth");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.DOUBLE, array.getDataType());
        assertEquals(0.2, array.getFloat(0), 1e-8);
    }

    @Test
    public void testReadRaw_mooring() throws Exception {
        insituReader = new SirdsInsituReader("mooring");
        openFile("SSTCCI2_refdata_mooring_201602.nc");
        final Array array = insituReader.readRaw(0, 1, new Interval(1, 1), "depth_corr");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.DOUBLE, array.getDataType());
        assertEquals(-9999.0, array.getFloat(0), 1e-8);
    }

    @Test
    public void testReadRaw_xbt() throws Exception {
        insituReader = new SirdsInsituReader("xbt");
        openFile("SSTCCI2_refdata_xbt_200204.nc");
        final Array array = insituReader.readRaw(0, 2, new Interval(1, 1), "latitude");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.DOUBLE, array.getDataType());
        assertEquals(2.8117001056671143, array.getFloat(0), 1e-8);
    }

    @Test
    public void testReadRaw_drifter_3x3() throws Exception {
        insituReader = new SirdsInsituReader("drifter");
        openFile("SSTCCI2_refdata_drifter_201304.nc");
        final Array array = insituReader.readRaw(0, 3, new Interval(3, 3), "longitude");

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
    public void testReadRaw_drifter_3x3_plat_id() throws Exception {
        insituReader = new SirdsInsituReader("drifter");
        openFile("SSTCCI2_refdata_drifter_201304.nc");
        final Array array = insituReader.readRaw(0, 4, new Interval(3, 3), "plat_id");

        final int[] shape = array.getShape();
        assertEquals(9, shape[0]);
        final char[] dataVector = (char[]) array.get1DJavaArray(DataType.CHAR);

        assertEquals("63552    ", new String(dataVector));
    }

    @Test
    public void testReadRaw_mooring_3x3() throws Exception {
        insituReader = new SirdsInsituReader("mooring");
        openFile("SSTCCI2_refdata_mooring_201602.nc");
        final Array array = insituReader.readRaw(0, 4, new Interval(3, 3), "sst");

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
        insituReader = new SirdsInsituReader("xbt");
        openFile("SSTCCI2_refdata_xbt_200204.nc");
        final Array array = insituReader.readScaled(0, 5, new Interval(1, 1), "sst_comb_unc");

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.FLOAT, array.getDataType());
        assertEquals(0.15f, array.getFloat(0), 1e-8);
    }

    @Test
    public void testReadScaled_drifter_3x3() throws Exception {
        insituReader = new SirdsInsituReader("drifter");
        openFile("SSTCCI2_refdata_drifter_201304.nc");
        final Array array = insituReader.readScaled(0, 6, new Interval(3, 3), "sst_rand_unc");

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
        insituReader = new SirdsInsituReader("mooring");
        openFile("SSTCCI2_refdata_mooring_201602.nc");
        final Array idArray = insituReader.readRaw(0, 7, new Interval(1, 1), "unique_id");

        assertNotNull(idArray);
        assertEquals(DataType.LONG, idArray.getDataType());

        NCTestUtils.assertValueAt(2016020000000098L, 0, 0, idArray);
    }

    @Test
    public void testReadRaw_xbt_uniqueId() throws Exception {
        insituReader = new SirdsInsituReader("xbt");
        openFile("SSTCCI2_refdata_xbt_200204.nc");
        final Array idArray = insituReader.readRaw(0, 8, new Interval(1, 1), "unique_id");

        assertNotNull(idArray);
        assertEquals(DataType.LONG, idArray.getDataType());

        NCTestUtils.assertValueAt(2002040001657855L, 0, 0, idArray);
    }

    @Test
    public void testReadAcquisitionTime_drifter() throws Exception {
        insituReader = new SirdsInsituReader("drifter");
        openFile("SSTCCI2_refdata_drifter_201304.nc");
        final ArrayInt.D2 array = insituReader.readAcquisitionTime(0, 9, new Interval(3, 3));

        assertNotNull(array);
        assertArrayEquals(new int[]{3, 3}, array.getShape());
        assertEquals(DataType.INT, array.getDataType());

        NCTestUtils.assertValueAt(-2147483647, 0, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 1, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 0, array);
        NCTestUtils.assertValueAt(-2147483647, 0, 1, array);
        NCTestUtils.assertValueAt(1364774400, 1, 1, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 1, array);
        NCTestUtils.assertValueAt(-2147483647, 0, 2, array);
        NCTestUtils.assertValueAt(-2147483647, 1, 2, array);
        NCTestUtils.assertValueAt(-2147483647, 2, 2, array);
    }

    @Test
    public void testReadAcquisitionTime_mooring() throws Exception {
        insituReader = new SirdsInsituReader("mooring");
        openFile("SSTCCI2_refdata_mooring_201602.nc");
        final ArrayInt.D2 array = insituReader.readAcquisitionTime(0, 10, new Interval(1, 1));

        assertNotNull(array);
        assertArrayEquals(new int[]{1, 1}, array.getShape());
        assertEquals(DataType.INT, array.getDataType());

        NCTestUtils.assertValueAt(1454284800, 0, 0, array);
    }

    @Test
    public void testGetProductSize_xbt() throws Exception {
        insituReader = new SirdsInsituReader("xbt");
        openFile("SSTCCI2_refdata_xbt_200204.nc");
        final Dimension productSize = insituReader.getProductSize();

        assertNotNull(productSize);
        assertEquals("product_size", productSize.getName());
        assertEquals(1, productSize.getNx());
        assertEquals(1339, productSize.getNy());
    }

    @Test
    public void testGetProductSize_drifter() throws Exception {
        insituReader = new SirdsInsituReader("drifter");
        openFile("SSTCCI2_refdata_drifter_201304.nc");
        final Dimension productSize = insituReader.getProductSize();

        assertNotNull(productSize);
        assertEquals("product_size", productSize.getName());
        assertEquals(1, productSize.getNx());
        assertEquals(671404, productSize.getNy());
    }

    @Test
    public void testGetTimeLocator_mooring() throws Exception {
        insituReader = new SirdsInsituReader("mooring");
        openFile("SSTCCI2_refdata_mooring_201602.nc");
        final TimeLocator timeLocator = insituReader.getTimeLocator();

        assertNotNull(timeLocator);
        assertEquals(1454284800000L, timeLocator.getTimeFor(0, 11));
        assertEquals(1454284800000L, timeLocator.getTimeFor(1, 12));
        assertEquals(1454284800000L, timeLocator.getTimeFor(2, 13));
    }

    @Test
    public void testGetTimeLocator_xbt() throws Exception {
        insituReader = new SirdsInsituReader("xbt");
        openFile("SSTCCI2_refdata_xbt_200204.nc");
        final TimeLocator timeLocator = insituReader.getTimeLocator();

        assertNotNull(timeLocator);
        assertEquals(1017639360000L, timeLocator.getTimeFor(3, 14));
        assertEquals(1017641772000L, timeLocator.getTimeFor(4, 15));
        assertEquals(1017644292000L, timeLocator.getTimeFor(5, 16));
    }

    @Test
    public void testGetPixelLocator() throws Exception {
        insituReader = new SirdsInsituReader("drifter");
        openFile("SSTCCI2_refdata_drifter_201304.nc");

        try {
            insituReader.getPixelLocator();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetSubScenePixelLocator() throws Exception {
        insituReader = new SirdsInsituReader("mooring");
        openFile("SSTCCI2_refdata_mooring_201602.nc");
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Polygon polygon = geometryFactory.createPolygon(Arrays.asList(
                geometryFactory.createPoint(4, 5),
                geometryFactory.createPoint(5, 6),
                geometryFactory.createPoint(6, 5)
        ));

        try {
            insituReader.getSubScenePixelLocator(polygon);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    private void openFile(String fileName) throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sirds", "v1.0", fileName}, false);
        final File insituDataFile = TestUtil.getTestDataFileAsserted(testFilePath);

        insituReader.open(insituDataFile);
    }
}
