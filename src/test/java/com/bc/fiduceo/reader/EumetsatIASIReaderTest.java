package com.bc.fiduceo.reader;

import org.junit.Test;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EumetsatIASIReaderTest {

    @Test
    public void testGetGlobalAttributeAsDate() throws IOException {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);

        final Attribute attribute = mock(Attribute.class);
        when(attribute.getStringValue()).thenReturn("1999-08-21T17:11:52Z");

        when(netcdfFile.findGlobalAttribute("time_converage_start")).thenReturn(attribute);

        final Date start = EumetsatIASIReader.getGlobalAttributeAsDate("time_converage_start", netcdfFile);
        assertNotNull(start);

        TestUtil.assertCorrectUTCDate(1999, 8, 21, 17, 11, 52, start);
    }

    @Test
    public void testGetGlobalAttributeAsDate_parsingError() throws IOException {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);

        final Attribute attribute = mock(Attribute.class);
        when(attribute.getStringValue()).thenReturn("unparseable date");

        when(netcdfFile.findGlobalAttribute("the_failing_attribute")).thenReturn(attribute);

        try {
            EumetsatIASIReader.getGlobalAttributeAsDate("the_failing_attribute", netcdfFile);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testGetGlobalAttributeAsDate_attributeNotPresent() throws IOException {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);

        when(netcdfFile.findGlobalAttribute("missing_attribute")).thenReturn(null);

        try {
            EumetsatIASIReader.getGlobalAttributeAsDate("missing_attribute", netcdfFile);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }
}
