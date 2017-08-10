package com.bc.fiduceo.reader.caliop;

import static org.junit.Assert.*;

import org.junit.*;
import ucar.ma2.Array;

public class CALIOP_L2_VFM_ReaderTest {

    @Test
    public void calculateIndizes() throws Exception {
        short[] is = CALIOP_L2_VFM_Reader.calcalculateIndizes();

        assertNotNull(is);
        assertEquals(6, is.length);
        assertEquals(55, is[0]);
        assertEquals(109, is[1]);
        assertEquals(565, is[2]);
        assertEquals(764, is[3]);
        assertEquals(3195, is[4]);
        assertEquals(3484, is[5]);
    }

    @Test
    public void readNadirClassificationFlags() throws Exception {
        //preparation
        final short[] storage = createFullStorage();
        final Array array = Array.factory(storage);

        //execution
        final Array flags = CALIOP_L2_VFM_Reader.readNadirClassificationFlags(array);

        //verification
        final short[] expected = createExpectedFlagsStorage();
        assertArrayEquals(expected, (short[]) flags.getStorage());
    }

    private short[] createExpectedFlagsStorage() {
        final short[] indices = CALIOP_L2_VFM_Reader.calcalculateIndizes();

        final short[] expected = new short[545];
        int expIdx = 0;
        for (int i = 0; i < indices.length; i += 2) {
            short start = indices[i];
            short stop = indices[i + 1];
            for (short v = start; v <= stop; v++) {
                expected[expIdx] = (short) (v + 1);
                expIdx++;
            }
        }
        return expected;
    }

    private short[] createFullStorage() {
        final short[] storage = new short[5515];
        for (int i = 0; i < storage.length; i++) {
            storage[i] = (short) (i + 1);
        }
        return storage;
    }

    @Test
    public void testExtractYearMonthDayFromFilename() throws Exception {
        final CALIOP_L2_VFM_Reader reader = new CALIOP_L2_VFM_Reader(null);
        String caliopFileName;
        int[] ymd;

        caliopFileName = "CAL_LID_L2_VFM-Standard-V4-10.2008-05-31T00-11-58ZN.hdf";
        ymd = reader.extractYearMonthDayFromFilename(caliopFileName);
        assertArrayEquals(new int[]{2008, 5, 31}, ymd);

        caliopFileName = "CAL_LID_L2_VFM-Standard-V4-10.2010-06-02T09-00-40ZD.hdf";
        ymd = reader.extractYearMonthDayFromFilename(caliopFileName);
        assertArrayEquals(new int[]{2010, 6, 2}, ymd);
    }

    @Test
    public void testGetLongitudeVariableName() {
        final CALIOP_L2_VFM_Reader reader = new CALIOP_L2_VFM_Reader(null); // we do not need a geometry factory for this test tb 2017-08-10

        assertEquals("Longitude", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        final CALIOP_L2_VFM_Reader reader = new CALIOP_L2_VFM_Reader(null); // we do not need a geometry factory for this test tb 2017-08-10

        assertEquals("Latitude", reader.getLatitudeVariableName());
    }
}