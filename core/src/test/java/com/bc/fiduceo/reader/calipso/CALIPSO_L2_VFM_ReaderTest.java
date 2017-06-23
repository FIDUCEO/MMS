package com.bc.fiduceo.reader.calipso;

import static org.junit.Assert.*;

import org.junit.*;
import ucar.ma2.Array;

/**
 * Created by Sabine on 20.06.2017.
 */
public class CALIPSO_L2_VFM_ReaderTest {

    @Test
    public void calculateIndizes() throws Exception {
        short[] is = CALIPSO_L2_VFM_Reader.calcalculateIndizes();

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
        final Array flags = CALIPSO_L2_VFM_Reader.readNadirClassificationFlags(array);

        //verification
        final short[] expected = createExpectedFlagsStorage();
        assertArrayEquals(expected, (short[]) flags.getStorage());
    }

    private short[] createExpectedFlagsStorage() {
        final short[] indizes = CALIPSO_L2_VFM_Reader.calcalculateIndizes();

        final short[] expected = new short[545];
        int expIdx = 0;
        for (int i = 0; i < indizes.length; i += 2) {
            short start = indizes[i];
            short stop = indizes[i + 1];
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
}