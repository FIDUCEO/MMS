package com.bc.fiduceo.ingest;

import static org.junit.Assert.*;

import com.bc.fiduceo.util.TimeUtils;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.*;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class ArchiveTest {

    private Archive archive;

    @Before
    public void setUp() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        Path foo = fs.getPath("/foo");
        Files.createDirectory(foo);
        archive = new Archive(Paths.get("testpath"), fs);
    }

    @Test
    public void testGet() throws Exception {
        final Date startDate = TimeUtils.parseDOYBeginOfDay("2015-20");
        final Date endDate = TimeUtils.parseDOYBeginOfDay("2015-200");
        final Path[] productPaths = archive.get(startDate, endDate, "1.0", "amsub");
        assertNotNull(productPaths);
        assertEquals(1, productPaths.length);
    }
}