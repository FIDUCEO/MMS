package com.bc.fiduceo.reader.windsat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ArrayInfoTest {

    @Test
    public void testConstruction() {
        final ArrayInfo arrayInfo = new ArrayInfo();

        assertNull(arrayInfo.ncVarName);
        assertEquals(-1, arrayInfo.freqIdx);
        assertEquals(-1, arrayInfo.lookIdx);
        assertEquals(-1, arrayInfo.polIdx);
    }
}
