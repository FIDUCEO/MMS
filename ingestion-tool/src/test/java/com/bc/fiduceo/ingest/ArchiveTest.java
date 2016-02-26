package com.bc.fiduceo.ingest;

import com.bc.fiduceo.util.TimeUtils;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        final Date startDate = getDate("2015-021");
        final Date endDate = getDate("2015-025");

        // action
        final Path[] productPaths = archive.get(startDate, endDate, processingVersion, sensorType);
        // validation
        assertNotNull(productPaths);
        assertEquals(15, productPaths.length);
    }


    @Test
    public void testFetchingProductWithNonExistingDir() throws IOException {
        final Date startDate = getDate("2015-021");
        final Date endDate = getDate("2015-025");

        final Path[] productPaths = archive.get(startDate, endDate, processingVersion, sensorType);
        assertNotNull(productPaths);
        assertEquals(0, productPaths.length);
    }


    @Test
    public void testFetchingProductWithEmpty_And_MissingDir() throws IOException {

        //Preparation
        final Path stPath = Files.createDirectory(root.resolve(sensorType));
        final Path pvPath = Files.createDirectory(stPath.resolve(processingVersion));
        final Path yearPath = Files.createDirectory(pvPath.resolve("2015"));
        final Path monPath = Files.createDirectory(yearPath.resolve("01"));
        stPath.resolve(processingVersion).resolve("2015").resolve("01");

        Path _dir_01 = Files.createDirectory(monPath.resolve("01"));
        Files.createFile(_dir_01.resolve(String.format("productFile_%s.nc", "01")));

        Path _dir_04 = Files.createDirectory(monPath.resolve("04"));
        Files.createFile(_dir_04.resolve(String.format("productFile_%s.nc", "04")));

        Path _dir_06 = Files.createDirectory(monPath.resolve("06"));
        Files.createFile(_dir_06.resolve(String.format("productFile_%s.nc", "06")));

        final Date startDate = getDate("2015-01");
        final Date endDate = getDate("2015-10");

        // action
        final Path[] productPaths = archive.get(startDate, endDate, processingVersion, sensorType);

        // validation
        assertNotNull(productPaths);
        assertEquals(3, productPaths.length);
        assertEquals("arciveRoot\\amsub\\1.0\\2015\\01\\01\\productFile_01.nc", productPaths[0].toString());
        assertEquals("arciveRoot\\amsub\\1.0\\2015\\01\\04\\productFile_04.nc", productPaths[1].toString());
        assertEquals("arciveRoot\\amsub\\1.0\\2015\\01\\06\\productFile_06.nc", productPaths[2].toString());
    }

    private Date getDate(String dateString) {
        return TimeUtils.parseDOYBeginOfDay(dateString);
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