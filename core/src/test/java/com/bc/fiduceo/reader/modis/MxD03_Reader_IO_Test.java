package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.ReaderContext;
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

import static org.junit.Assert.assertEquals;

@RunWith(IOTestRunner.class)
public class MxD03_Reader_IO_Test {

    private MxD03_Reader reader;
    private GeometryFactory geometryFactory;

    @Before
    public void setUp() throws IOException {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);

        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(geometryFactory);

        reader = new MxD03_Reader(readerContext);
    }

    @After
    public void tearDown() throws Exception {
        reader.close();
    }

    @Test
    public void testGetVariables_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);
        final List<Variable> variables = reader.getVariables();
        assertEquals(12, variables.size());

        Variable variable = variables.get(0);
        assertEquals("Scan_number", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());

        variable = variables.get(3);
        assertEquals("Height", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());

        variable = variables.get(6);
        assertEquals("Range", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());

        variable = variables.get(9);
        assertEquals("Land_SeaMask", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());
    }

    @Test
    public void testGetVariables_Aqua() throws IOException {
        final File file = getAquaFile();

        reader.open(file);
        final List<Variable> variables = reader.getVariables();
        assertEquals(12, variables.size());

        Variable variable = variables.get(1);
        assertEquals("Latitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(4);
        assertEquals("SensorZenith", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());

        variable = variables.get(7);
        assertEquals("SolarZenith", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());

        variable = variables.get(10);
        assertEquals("WaterPresent", variable.getShortName());
        assertEquals(DataType.UBYTE, variable.getDataType());
    }

    @Test
    public void testGetProductSize_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);
        final Dimension productSize = reader.getProductSize();
        assertEquals(1354, productSize.getNx());
        assertEquals(2030, productSize.getNy());
    }

    @Test
    public void testGetProductSize_Aqua() throws IOException {
        final File file = getAquaFile();

        reader.open(file);
        final Dimension productSize = reader.getProductSize();
        assertEquals(1354, productSize.getNx());
        assertEquals(2030, productSize.getNy());
    }

    @Test
    public void testReadRaw_Aqua() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readRaw(47, 90, new Interval(3, 3), "Latitude");
        NCTestUtils.assertValueAt(69.51026153564453, 0, 0, array);
        NCTestUtils.assertValueAt(69.51026916503906, 1, 0, array);

        NCTestUtils.assertValueAt(69.45342254638672, 1, 1, array);
        NCTestUtils.assertValueAt(69.45375061035156, 2, 1, array);
    }

    @Test
    public void testReadRaw_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readRaw(48, 91, new Interval(3, 3), "Longitude");
        NCTestUtils.assertValueAt(-118.96125030517578, 0, 0, array);
        NCTestUtils.assertValueAt(-118.90886688232422, 1, 0, array);

        NCTestUtils.assertValueAt(-118.94646453857422, 1, 1, array);
        NCTestUtils.assertValueAt(-118.8941879272461, 2, 1, array);
    }

    @Test
    public void testReadRaw_Aqua_upper() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readRaw(49, 0, new Interval(3, 3), "Height");
        NCTestUtils.assertValueAt(-32767, 0, 0, array);
        NCTestUtils.assertValueAt(-32767, 1, 0, array);

        NCTestUtils.assertValueAt(192, 1, 1, array);
        NCTestUtils.assertValueAt(191, 2, 1, array);

        NCTestUtils.assertValueAt(177, 0, 2, array);
        NCTestUtils.assertValueAt(185, 1, 2, array);
    }

    @Test
    public void testReadRaw_Terra_right() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readRaw(1353, 92, new Interval(3, 3), "SensorZenith");
        NCTestUtils.assertValueAt(6570, 0, 0, array);
        NCTestUtils.assertValueAt(6583, 1, 0, array);
        NCTestUtils.assertValueAt(-32767, 2, 0, array);

        NCTestUtils.assertValueAt(6570, 0, 1, array);
        NCTestUtils.assertValueAt(6583, 1, 1, array);
        NCTestUtils.assertValueAt(-32767, 2, 1, array);
    }

    @Test
    public void testReadRaw_Aqua_bottom() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readRaw(51, 2029, new Interval(3, 3), "SensorAzimuth");
        NCTestUtils.assertValueAt(-8071, 0, 0, array);
        NCTestUtils.assertValueAt(-8123, 1, 0, array);

        NCTestUtils.assertValueAt(-8129, 1, 1, array);
        NCTestUtils.assertValueAt(-8181, 2, 1, array);

        NCTestUtils.assertValueAt(-32767, 0, 2, array);
        NCTestUtils.assertValueAt(-32767, 1, 2, array);
    }

    @Test
    public void testReadRaw_Terra_left() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readRaw(0, 93, new Interval(3, 3), "Range");
        NCTestUtils.assertValueAt(0, 0, 0, array);
        NCTestUtils.assertValueAt(-6648, 1, 0, array);
        NCTestUtils.assertValueAt(-6833, 2, 0, array);

        NCTestUtils.assertValueAt(0, 0, 1, array);
        NCTestUtils.assertValueAt(-6648, 1, 1, array);
        NCTestUtils.assertValueAt(-6834, 2, 1, array);
    }

    @Test
    public void testReadScaled_Aqua() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readScaled(48, 91, new Interval(3, 3), "SolarZenith");
        NCTestUtils.assertValueAt(48.8, 0, 0, array);
        NCTestUtils.assertValueAt(48.78, 1, 0, array);

        NCTestUtils.assertValueAt(48.79, 1, 1, array);
        NCTestUtils.assertValueAt(48.77, 2, 1, array);
    }

    @Test
    public void testReadScaled_Terra() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readScaled(49, 92, new Interval(3, 3), "SolarAzimuth");
        NCTestUtils.assertValueAt(70.45, 0, 0, array);
        NCTestUtils.assertValueAt(70.41, 1, 0, array);

        NCTestUtils.assertValueAt(70.44, 1, 1, array);
        NCTestUtils.assertValueAt(70.4, 2, 1, array);
    }

    @Test
    public void testReadScaled_Aqua_upper() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readScaled(50, 0, new Interval(3, 3), "Land_SeaMask");
        NCTestUtils.assertValueAt(-35, 0, 0, array);
        NCTestUtils.assertValueAt(-35, 1, 0, array);

        NCTestUtils.assertValueAt(2, 1, 1, array);
        NCTestUtils.assertValueAt(2, 2, 1, array);

        NCTestUtils.assertValueAt(2, 0, 2, array);
        NCTestUtils.assertValueAt(3, 1, 2, array);
    }

    @Test
    public void testReadScaled_Terra_right() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readScaled(1353, 93, new Interval(3, 3), "WaterPresent");
        NCTestUtils.assertValueAt(8, 0, 0, array);
        NCTestUtils.assertValueAt(8, 1, 0, array);
        NCTestUtils.assertValueAt(-1, 2, 0, array);

        NCTestUtils.assertValueAt(8, 0, 1, array);
        NCTestUtils.assertValueAt(8, 1, 1, array);
        NCTestUtils.assertValueAt(-1, 2, 1, array);
    }

    @Test
    public void testReadScaled_Aqua_bottom() throws IOException {
        final File file = getAquaFile();

        reader.open(file);

        final Array array = reader.readScaled(52, 2029, new Interval(3, 3), "gflags");
        NCTestUtils.assertValueAt(0, 0, 0, array);
        NCTestUtils.assertValueAt(0, 1, 0, array);

        NCTestUtils.assertValueAt(0, 1, 1, array);
        NCTestUtils.assertValueAt(0, 2, 1, array);

        NCTestUtils.assertValueAt(-1, 0, 2, array);
        NCTestUtils.assertValueAt(-1, 1, 2, array);
    }

    @Test
    public void testReadScaled_Terra_left() throws IOException {
        final File file = getTerraFile();

        reader.open(file);

        final Array array = reader.readScaled(0, 100, new Interval(3, 3), "Scan_number");
        NCTestUtils.assertValueAt(-32767, 0, 0, array);
        NCTestUtils.assertValueAt(10, 1, 0, array);
        NCTestUtils.assertValueAt(10, 2, 0, array);

        NCTestUtils.assertValueAt(-32767, 0, 1, array);
        NCTestUtils.assertValueAt(11, 1, 1, array);
        NCTestUtils.assertValueAt(11, 2, 1, array);
    }

    private File getTerraFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"mod03-te", "v61", "2003", "05", "22", "MOD03.A2003142.1445.061.2017192042416.hdf"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getAquaFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"myd03-aq", "v61", "2011", "06", "17", "MYD03.A2011168.2210.061.2018030150511.hdf"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
