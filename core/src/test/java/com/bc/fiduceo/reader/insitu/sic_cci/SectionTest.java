package com.bc.fiduceo.reader.insitu.sic_cci;

import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SectionTest {

    private Section section;

    @Before
    public void setUp() {
        final Array floatData = Array.factory(DataType.FLOAT, new int[]{1}, new float[]{1.89f});
        final Array intData = Array.factory(DataType.INT, new int[]{1}, new int[]{29});

        section = new Section();
        section.add("float", floatData);
        section.add("integer", intData);
    }

    @Test
    public void testGetData() {
        Array data = section.get("integer");
        assertEquals(29, data.getInt(0));

        data = section.get("float");
        assertEquals(1.89f, data.getFloat(0), 1e-8);
    }

    @Test
    public void testGetData_notExistent() {
        try {
            section.get("firlefanz");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
