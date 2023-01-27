package com.bc.fiduceo.reader.insitu.sic_cci;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractSectionParserTest {

    @Test
    public void testParseByte()  {
        assertEquals(15, AbstractSectionParser.parseByte("15").getByte(0));

        assertEquals(-127, AbstractSectionParser.parseByte("noval").getByte(0));
        assertEquals(-127, AbstractSectionParser.parseByte("").getByte(0));
    }

    @Test
    public void testParseShort()  {
        assertEquals(-8713, AbstractSectionParser.parseShort("-8713").getShort(0));

        assertEquals(-32767, AbstractSectionParser.parseShort("noval").getShort(0));
        assertEquals(-32767, AbstractSectionParser.parseShort("").getShort(0));
    }

    @Test
    public void testParseFloat()  {
        assertEquals(66.903, AbstractSectionParser.parseFloat("66.903").getFloat(0), 1e-6);

        assertEquals(9.969209968386869E36, AbstractSectionParser.parseFloat("noval").getFloat(0), 1e-8);
        assertEquals(9.969209968386869E36, AbstractSectionParser.parseFloat("").getFloat(0), 1e-8);
    }
}
