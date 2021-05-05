package com.bc.fiduceo.post.plugin.era5;

import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import static org.junit.Assert.*;
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

    @Test
    public void testConvertToEra5TimeStamp_withFillValue() {
        final Array acquisitionTime = Array.factory(DataType.INT, new int[]{6}, new int[]{1490542129, 1490545559, VariableUtils.TIME_FILL, 1490543482, 1490542437, 1490542946});

        final Array converted = VariableUtils.convertToEra5TimeStamp(acquisitionTime);
        assertEquals(6, converted.getSize());
        assertEquals(1490540400, converted.getInt(0));
        assertEquals(1490544000, converted.getInt(1));
        assertEquals(VariableUtils.TIME_FILL, converted.getInt(2));
        assertEquals(1490544000, converted.getInt(3));
        assertEquals(1490544000, converted.getInt(4));
        assertEquals(1490544000, converted.getInt(5));
    }

    @Test
    public void testGetNwpShape() {
        final com.bc.fiduceo.core.Dimension dimension = new com.bc.fiduceo.core.Dimension("whatever", 3, 5);

        final int[] matchupShape = {11, 7, 7};

        final int[] nwpShape = VariableUtils.getNwpShape(dimension, matchupShape);
        assertEquals(3, nwpShape.length);
        assertEquals(11, nwpShape[0]);
        assertEquals(5, nwpShape[1]);
        assertEquals(3, nwpShape[2]);
    }

    @Test
    public void testGetNwpShape_clip() {
        final com.bc.fiduceo.core.Dimension dimension = new com.bc.fiduceo.core.Dimension("clipped", 7, 7);

        final int[] matchupShape = {12, 3, 5};

        final int[] nwpShape = VariableUtils.getNwpShape(dimension, matchupShape);
        assertEquals(3, nwpShape.length);
        assertEquals(12, nwpShape[0]);
        assertEquals(3, nwpShape[1]);
        assertEquals(5, nwpShape[2]);
    }

    @Test
    public void testGetNwpOffset() {
        final int[] matchupShape = {118, 7, 7};
        final int[] nwpShape = {118, 5, 5};

        final int[] nwpOffset = VariableUtils.getNwpOffset(matchupShape, nwpShape);
        assertEquals(3, nwpOffset.length);
        assertEquals(0, nwpOffset[0]);
        assertEquals(1, nwpOffset[1]);
        assertEquals(1, nwpOffset[2]);
    }

    @Test
    public void testIsTimeFill() {
        assertTrue(VariableUtils.isTimeFill(VariableUtils.TIME_FILL));

        assertFalse(VariableUtils.isTimeFill(12));
        assertFalse(VariableUtils.isTimeFill(0));
        assertFalse(VariableUtils.isTimeFill(-125));
    }
}
