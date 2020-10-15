package com.bc.fiduceo.reader;

import com.bc.fiduceo.archive.Archive;
import com.bc.fiduceo.archive.ArchiveConfig;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.util.TempFileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ReaderContextTest {

    private ReaderContext readerContext;

    @Before
    public void setUp() {
        readerContext = new ReaderContext();
    }

    @Test
    public void testSetGetGeometryFactory() {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.JTS);

        readerContext.setGeometryFactory(geometryFactory);
        assertSame(geometryFactory, readerContext.getGeometryFactory());
    }

    @Test
    public void testSetTempFileUtils_andAccessors() throws IOException {
        final TempFileUtils tempFileUtils = mock(TempFileUtils.class);

        readerContext.setTempFileUtils(tempFileUtils);

        final File tempFile = readerContext.createTempFile("prefix", "extension");
        readerContext.deleteTempFile(tempFile);

        verify(tempFileUtils, times(1)).create("prefix", "extension");
        verify(tempFileUtils, times(1)).delete(tempFile);
    }

    @Test
    public void testSetGetArchive() {
        final Archive archive = mock(Archive.class);

        readerContext.setArchive(archive);
        assertSame(archive, readerContext.getArchive());
    }
}
