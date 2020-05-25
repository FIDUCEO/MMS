package com.bc.fiduceo.reader.modis;

import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScanNumberVariableTest {

    @Test
    public void testGetShortName() {
        final Variable variable = mock(Variable.class);
        when(variable.getShortName()).thenReturn("scanNumber");

        final ScanNumberVariable qualityVariable = new ScanNumberVariable(variable);

        assertEquals("scanNumber", qualityVariable.getShortName());
    }

    @Test
    public void testGetDataType() {
        final Variable variable = mock(Variable.class);
        when(variable.getDataType()).thenReturn(DataType.SHORT);

        final ScanNumberVariable qualityVariable = new ScanNumberVariable(variable);

        assertEquals(DataType.SHORT, qualityVariable.getDataType());
    }
}
