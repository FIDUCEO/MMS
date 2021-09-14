package com.bc.fiduceo.matchup.writer;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class StringWritingIOVariableTest {

    private StringWritingIOVariable ioVariable;

    @Before
    public void setUp() {
        ioVariable = new StringWritingIOVariable(null, 7);
    }

    @Test
    public void testHasCustomDimension() {
        assertTrue(ioVariable.hasCustomDimension());
    }

    @Test
    public void testGetCustomDimension() {
        ioVariable.setTargetVariableName("heffalump");

        final Dimension dimension = ioVariable.getCustomDimension();
        assertEquals("heffalump_dim", dimension.getShortName());
        assertEquals(7, dimension.getLength());
    }

    @Test
    public void testGetDimensionNames() {
        ioVariable.setTargetVariableName("targa");

        assertEquals("matchup_count targa_dim", ioVariable.getDimensionNames());
    }

    @Test
    public void testGetDataType() {
        assertEquals("char", ioVariable.getDataType());
    }

    @Test
    public void testWriteData() throws InvalidRangeException, IOException {
        final Target target = mock(Target.class);
        final Reader readerMock = mock(Reader.class);

        final Array data = NetCDFUtils.create(new char[]{'w', 'a', 'n', 't', 'a', 'n'});
        when(readerMock.readRaw(anyInt(), anyInt(), any(), anyString())).thenReturn(data);

        final ReaderContainer sourceContainer = new ReaderContainer();
        sourceContainer.setReader(readerMock);

        final StringWritingIOVariable ioVariable = new StringWritingIOVariable(sourceContainer, 6);
        ioVariable.setTarget(target);
        ioVariable.setSourceVariableName("yammas");
        ioVariable.setTargetVariableName("ouzo");

        final Interval interval = new Interval(1, 1);
        ioVariable.writeData(5, 7, interval, 6);

        verify(readerMock, times(1)).readRaw(5, 7, interval, "yammas");
        verify(target, times(1)).write("wantan", "ouzo", 6);

        verifyNoMoreInteractions(readerMock);
        verifyNoMoreInteractions(target);
    }
}
