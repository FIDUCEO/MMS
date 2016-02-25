package com.bc.fiduceo.ingest;

import static org.junit.Assert.*;

import com.bc.fiduceo.util.TimeUtils;
import com.google.common.jimfs.Jimfs;
import org.junit.*;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

public class ArchiveTest {

    private Archive archive;
    private String processingVersion = "1.0";
    private String sensorType = "amsub";
    private Path root;

    @Before
    public void setUp() throws IOException {
        FileSystem fs = Jimfs.newFileSystem();
        root = fs.getPath("arciveRoot");
        Files.createDirectory(root);
        archive = new Archive(root);
    }

    @After
    public void tearDown() throws Exception {
        root.getFileSystem().close();
    }

    @Test
    public void testFetchingProductFilesFromArchive() throws Exception {
        // preparation
        final Path stPath = Files.createDirectory(root.resolve(sensorType));
        final Path pvPath = Files.createDirectory(stPath.resolve(processingVersion));
        final Path yearPath = Files.createDirectory(pvPath.resolve("2015"));
        final Path monPath = Files.createDirectory(yearPath.resolve("01"));
        stPath.resolve(processingVersion).resolve("2015").resolve("01");
        for (int day = 21; day < 26; day++) {
            final Path directory = Files.createDirectory(monPath.resolve("" + day));
            for (int i = 0; i < day - 20; i++) {
                Files.createFile(directory.resolve(String.format("productFile%d.nc", i)));
            }
        }
        final Date startDate = TimeUtils.parseDOYBeginOfDay("2015-021");
        final Date endDate = TimeUtils.parseDOYBeginOfDay("2015-025");

        // action
        final Path[] productPaths = archive.get(startDate, endDate, processingVersion, sensorType);

        // validation
        assertNotNull(productPaths);
        assertEquals(15, productPaths.length);
    }

    @Test
    public void testValidProductPathCreation() throws Exception {
        // preparation

        // action
        final Path productPath = archive.createAValidProductPath(processingVersion, sensorType, 2001, 4, 3);

        // validation
        assertEquals("arciveRoot\\amsub\\1.0\\2001\\04\\03", productPath.toString());
    }
}