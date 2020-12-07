package com.bc.fiduceo.post.plugin.era5;

import org.junit.Test;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

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
}
