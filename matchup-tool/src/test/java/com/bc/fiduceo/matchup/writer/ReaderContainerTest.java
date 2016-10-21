package com.bc.fiduceo.matchup.writer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.reader.Reader;
import org.junit.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Sabine on 17.10.2016.
 */
public class ReaderContainerTest {

    private ReaderContainer readerContainer;

    @Before
    public void setUp() throws Exception {
        readerContainer = new ReaderContainer();
    }

    @Test
    public void testSetReader() throws Exception {
        final Reader readerMock = mock(Reader.class);
        readerContainer.setReader(readerMock);
        assertSame(readerMock, readerContainer.getReader());
    }

    @Test
    public void setSourcePath_WithoutSoure() throws Exception {
        final Path aPath = Paths.get("aPath");
        readerContainer.setSourcePath(aPath);

        assertSame(aPath, readerContainer.getSourcePath());
    }

    @Test
    public void setSourcePath_WithSoure() throws Exception {
        final Reader readerMock = mock(Reader.class);
        readerContainer.setReader(readerMock);
        final Path aPath = Paths.get("aPath");
        readerContainer.setSourcePath(aPath);

        assertSame(aPath, readerContainer.getSourcePath());
        verify(readerMock, times(1)).close();
        verify(readerMock,times(1)).open(aPath.toFile());
        verifyNoMoreInteractions(readerMock);

    }
}