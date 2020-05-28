package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import javax.xml.crypto.Data;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScanNumberVariableTest {

    @Test
    public void testGetShortName() {
        final Variable variable = mock(Variable.class);
        when(variable.getShortName()).thenReturn("scanNumber");

        final ScanNumberVariable scanNumberVariable = new ScanNumberVariable(variable);

        assertEquals("scanNumber", scanNumberVariable.getShortName());
    }

    @Test
    public void testGetDataType() {
        final Variable variable = mock(Variable.class);
        when(variable.getDataType()).thenReturn(DataType.SHORT);

        final ScanNumberVariable scanNumberVariable = new ScanNumberVariable(variable);

        assertEquals(DataType.SHORT, scanNumberVariable.getDataType());
    }

    @Test
    public void testRead() throws IOException {
        final short[] originalData = {2, 3, 4, 5};
        final Array originalArray = NetCDFUtils.create(originalData);
        final Variable variable = mock(Variable.class);
        when(variable.read()).thenReturn(originalArray);

        final ScanNumberVariable scanNumberVariable = new ScanNumberVariable(variable);
        final Array expandedArray = scanNumberVariable.read();

        final int[] shape = expandedArray.getShape();
        assertEquals(40, shape[0]);
        assertEquals(DataType.SHORT, expandedArray.getDataType());

        assertEquals(2, expandedArray.getShort(0));
        assertEquals(2, expandedArray.getShort(9));
        assertEquals(3, expandedArray.getShort(10));
        assertEquals(3, expandedArray.getShort(19));
        assertEquals(4, expandedArray.getShort(20));
    }
}
