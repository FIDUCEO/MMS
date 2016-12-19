package com.bc.fiduceo.matchup.writer;

import com.bc.fiduceo.reader.Reader;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ReaderContainerTest {

    private ReaderContainer readerContainer;

    @Before
    public void setUp() throws Exception {
        readerContainer = new ReaderContainer();
    }

    @Test
    public void testSetGetReader() throws Exception {
        final Reader readerMock = mock(Reader.class);

        readerContainer.setReader(readerMock);
        assertSame(readerMock, readerContainer.getReader());
    }

    @Test
    public void testSetGetSourcePath() throws Exception {
        final Path aPath = Paths.get("aPath");

        readerContainer.setSourcePath(aPath);
        assertSame(aPath, readerContainer.getSourcePath());
    }
}