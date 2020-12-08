package com.bc.fiduceo.post.plugin.era5;

import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class VariableUtilsTest {

    @Test
    public void testAddAttributes() {
        final TemplateVariable templateVariable = new TemplateVariable("theName", "metres", "a_long_name", "a_standard_name", true);
        final Variable variable = mock(Variable.class);

        VariableUtils.addAttributes(templateVariable, variable);

        verify(variable, times(1)).addAttribute(new Attribute("units", "metres"));
        verify(variable, times(1)).addAttribute(new Attribute("long_name", "a_long_name"));
        verify(variable, times(1)).addAttribute(new Attribute("standard_name", "a_standard_name"));
        verify(variable, times(1)).addAttribute(new Attribute("_FillValue", 9.96921E36f));
        verifyNoMoreInteractions(variable);
    }

    @Test
    public void testAddAttributes_missingStandarName() {
        final TemplateVariable templateVariable = new TemplateVariable("Carola", "gramm", "Heffalump", null, true);
        final Variable variable = mock(Variable.class);

        VariableUtils.addAttributes(templateVariable, variable);

        verify(variable, times(1)).addAttribute(new Attribute("units", "gramm"));
        verify(variable, times(1)).addAttribute(new Attribute("long_name", "Heffalump"));
        verify(variable, times(1)).addAttribute(new Attribute("_FillValue", 9.96921E36f));
        verifyNoMoreInteractions(variable);
    }

    @Test
    public void testToEra5TimeStamp() {
        assertEquals(1212400800, VariableUtils.toEra5TimeStamp(1212399488));
        assertEquals(1212145200, VariableUtils.toEra5TimeStamp(1212145250));
    }

    @Test
    public void testConvertToEra5TimeStamp() {
        final Array acquisitionTime = Array.factory(DataType.INT, new int[]{6}, new int[]{1480542129, 1480545559, 1480541820, 1480543482, 1480542437, 1480542946});

        final Array converted = VariableUtils.convertToEra5TimeStamp(acquisitionTime);
        assertEquals(6, converted.getSize());
        assertEquals(1480543200, converted.getInt(0));
        assertEquals(1480546800, converted.getInt(1));
        assertEquals(1480543200, converted.getInt(2));
        assertEquals(1480543200, converted.getInt(3));
        assertEquals(1480543200, converted.getInt(4));
        assertEquals(1480543200, converted.getInt(5));
    }
}
