package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TemplateVariableTest {

    @Test
    public void testConstructAndGetter() {
        final TemplateVariable variable = new TemplateVariable("theName", "unit", "longName", "standardName", true);

        assertEquals("theName", variable.getName());
        assertEquals("unit", variable.getUnits());
        assertEquals("longName", variable.getLongName());
        assertEquals("standardName", variable.getStandardName());
        assertTrue(variable.is3d());

        assertEquals(NetCDFUtils.getDefaultFillValue(float.class), variable.getFillValue());
    }
}
