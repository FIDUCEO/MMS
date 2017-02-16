package com.bc.fiduceo.post.plugin.nwp;


import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TemplateVariablesTest {

    @Test
    public void testGetAnalysisVariables() {
        final TemplateVariables templateVariables = new TemplateVariables(new Configuration());

        final List<TemplateVariable> analysisVars = templateVariables.getAnalysisVariables();
        assertEquals(4, analysisVars.size());

        final TemplateVariable templateVariable = analysisVars.get(2);
        assertEquals("matchup.nwp.an.10m_east_wind_component", templateVariable.getName());
        assertEquals("U10", templateVariable.getOriginalName());
        assertEquals("matchup_count matchup.nwp.an.time", templateVariable.getDimensions());
        assertEquals(DataType.FLOAT, templateVariable.getDataType());

        final List<Attribute> attributes = templateVariable.getAttributes();
        assertEquals(5, attributes.size());
    }
}
