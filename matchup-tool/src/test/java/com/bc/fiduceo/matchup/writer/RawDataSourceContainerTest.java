package com.bc.fiduceo.matchup.writer;

import static org.junit.Assert.*;

import com.bc.fiduceo.reader.RawDataSource;
import org.junit.*;
import org.mockito.Mockito;

/**
 * Created by Sabine on 17.10.2016.
 */
public class RawDataSourceContainerTest {

    @Test
    public void testSetReader() throws Exception {
        final RawDataSourceContainer rawDataSourceContainer = new RawDataSourceContainer();
        final RawDataSource source = Mockito.mock(RawDataSource.class);
        rawDataSourceContainer.setSource(source);
        assertSame(source, rawDataSourceContainer.getSource());
    }
}