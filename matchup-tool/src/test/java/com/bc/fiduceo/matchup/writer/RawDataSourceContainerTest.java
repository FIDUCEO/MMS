package com.bc.fiduceo.matchup.writer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.reader.RawDataSource;
import org.junit.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Sabine on 17.10.2016.
 */
public class RawDataSourceContainerTest {

    private RawDataSourceContainer rawDataSourceContainer;

    @Before
    public void setUp() throws Exception {
        rawDataSourceContainer = new RawDataSourceContainer();
    }

    @Test
    public void testSetReader() throws Exception {
        final RawDataSource source = mock(RawDataSource.class);
        rawDataSourceContainer.setSource(source);
        assertSame(source, rawDataSourceContainer.getSource());
    }

    @Test
    public void setSourcePath_WithoutSoure() throws Exception {
        final Path aPath = Paths.get("aPath");
        rawDataSourceContainer.setSourcePath(aPath);

        assertSame(aPath, rawDataSourceContainer.getSourcePath());
    }

    @Test
    public void setSourcePath_WithSoure() throws Exception {
        final RawDataSource source = mock(RawDataSource.class);
        rawDataSourceContainer.setSource(source);
        final Path aPath = Paths.get("aPath");
        rawDataSourceContainer.setSourcePath(aPath);

        assertSame(aPath, rawDataSourceContainer.getSourcePath());
        verify(source, times(1)).close();
        verify(source,times(1)).open(aPath.toFile());
        verifyNoMoreInteractions(source);

    }
}