package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.store.FileSystemStore;
import com.bc.fiduceo.store.Store;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class NcCacheTest {

    private NcCache cache;

    @Before
    public void setUp() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"slstr-s3a-uor-n", "1.0", "2020", "05", "22", "S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.SEN3"}, false);
        final File file = TestUtil.getTestDataFileAsserted(testFilePath);
        final Store store = new FileSystemStore(file.toPath());
        cache = new NcCache();

        final RasterInfo rasterInfo = new RasterInfo();
        rasterInfo.rasterWidth = 1500;
        rasterInfo.rasterHeight = 1200;
        rasterInfo.rasterResolution = 1000;
        rasterInfo.tiePointResolution = 16000;
        rasterInfo.rasterTrackOffset = 998;
        rasterInfo.tiePointTrackOffset = 64;

        cache.open(store, rasterInfo);
    }

    @After
    public void tearDown() throws IOException {
        cache.close();
    }

    @Test
    public void testGetVariableNames() throws IOException {
        final List<String> varNames = cache.getVariableNames();
        assertEquals(57, varNames.size());
        assertTrue(varNames.contains("cloud_io"));
        assertTrue(varNames.contains("S1_radiance_in"));
        assertTrue(varNames.contains("probability_cloud_dual_io"));
        assertTrue(varNames.contains("S8_BT_in"));
        assertTrue(varNames.contains("longitude_in"));
    }

    @Test
    public void testGetVariable() throws IOException {
        Variable variable = cache.getVariable("S4_radiance_io");
        assertNotNull(variable);
        assertEquals("S4_radiance_io", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());
        assertArrayEquals(new int[]{1200, 900}, variable.getShape());

        variable = cache.getVariable("scan_in");
        assertNotNull(variable);
        assertEquals("scan_in", variable.getShortName());
        assertEquals(DataType.USHORT, variable.getDataType());
        assertArrayEquals(new int[]{1200, 1500}, variable.getShape());
    }

    @Test
    public void testGetVariable_tiePoint() throws IOException {
        Variable variable = cache.getVariable("solar_zenith_tn");
        assertNotNull(variable);
        assertEquals("solar_zenith_tn", variable.getShortName());
        assertEquals(DataType.DOUBLE, variable.getDataType());
        assertArrayEquals(new int[]{1200, 1500}, variable.getShape());

        variable = cache.getVariable("sat_path_to");
        assertNotNull(variable);
        assertEquals("sat_path_to", variable.getShortName());
        assertEquals(DataType.DOUBLE, variable.getDataType());
        assertArrayEquals(new int[]{1200, 1500}, variable.getShape());
    }

    @Test
    public void testGetVariable_invalidName() {
        try {
            cache.getVariable("does_not_exist");
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testIsTiePointVariable() {
        assertTrue(NcCache.isTiePointVariable("sat_azimuth_tn"));
        assertTrue(NcCache.isTiePointVariable("sat_zenith_to"));

        assertFalse(NcCache.isTiePointVariable("confidence_in"));
        assertFalse(NcCache.isTiePointVariable("S3_radiance_io"));
    }
}
