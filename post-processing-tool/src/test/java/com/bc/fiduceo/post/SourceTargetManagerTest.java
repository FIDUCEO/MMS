/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package com.bc.fiduceo.post;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.post.plugin.DummyPostProcessingPlugin;
import org.esa.snap.core.util.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/**
 * Created by Sabine on 13.12.2016.
 */
public class SourceTargetManagerTest {

    private static final String CONFIG = PostProcessingConfig.TAG_NAME_ROOT;
    private static final String PROCESSINGS = PostProcessingConfig.TAG_NAME_POST_PROCESSINGS;
    private static final String NEW_FILES = PostProcessingConfig.TAG_NAME_NEW_FILES;
    private static final String OUTPUT_DIR = PostProcessingConfig.TAG_NAME_OUTPUT_DIR;
    private static final String OVERWRITE = PostProcessingConfig.TAG_NAME_OVERWRITE;

    private static final String DUMMY_NAME = DummyPostProcessingPlugin.DUMMY_POST_PROCESSING_NAME;
    private Element root;
    private Path testDir;
    private Path srcDir;
    private Path srcFile;
    private Path outputDir;
    private String fileContent;

    @Before
    public void setUp() throws Exception {
        testDir = TestUtil.createTestDirectory().toPath();
        srcDir = testDir.resolve("srcDir");

        srcFile = srcDir.resolve("src.file");
        fileContent = "this is a test source file.";

        Files.createDirectories(srcDir);
        Files.createFile(srcFile);
        Files.write(srcFile, fileContent.getBytes());

        outputDir = testDir.resolve("outputDir");
        Files.createDirectories(outputDir);

        root = new Element(CONFIG).addContent(Arrays.asList(
                    new Element(NEW_FILES).addContent(
                                new Element(OUTPUT_DIR).addContent(outputDir.toAbsolutePath().toString())
                    ),
                    new Element(PROCESSINGS).addContent(
                                new Element(DUMMY_NAME).addContent("strong")
                    )
        ));
    }

    @After
    public void tearDown() throws Exception {
        if (Files.isDirectory(testDir)) {
            FileUtils.deleteTree(testDir.toFile());
            assertFalse(Files.exists(testDir));
        }
    }

    @Test
    public void testGetSourceAndTarget_newFileCase() throws Exception {

        final SourceTargetManager manager = new SourceTargetManager(getConfig());

        assertTrue(Files.isRegularFile(srcFile));
        final Path src = manager.getSource(srcFile);
        assertTrue(Files.isRegularFile(srcFile));

        final Path expectedSrc = srcFile;
        assertThat(src, is(equalTo(expectedSrc)));
        assertTrue(Files.isRegularFile(src));

        final Path target = manager.getTargetPath(srcFile);
        final Path expectedTarget = outputDir.resolve(srcFile.getFileName());
        assertThat(target, is(equalTo(expectedTarget)));
        assertFalse(Files.isRegularFile(target));
    }

    @Test
    public void testGetSourceAndTarget_overwriteFileCase() throws Exception {
        root.removeChild(NEW_FILES);
        root.addContent(new Element(OVERWRITE));

        final SourceTargetManager manager = new SourceTargetManager(getConfig());

        assertThat(Files.isRegularFile(srcFile), is(equalTo(true)));
        final Path src = manager.getSource(srcFile);
        assertThat(Files.exists(srcFile), is(equalTo(false)));

        final Path expectedSrc = srcFile.getParent().resolve(srcFile.getFileName() + ".temp");
        assertThat(src, is(equalTo(expectedSrc)));
        assertTrue(Files.isRegularFile(src));

        final Path target = manager.getTargetPath(srcFile);
        final Path expectedTarget = srcFile;
        assertThat(target, is(equalTo(expectedTarget)));
        assertFalse(Files.exists( target));
    }

    @Test
    public void testProcessingFinished_newFileCase() throws Exception {
        final SourceTargetManager manager = new SourceTargetManager(getConfig());
        final Path src = manager.getSource(srcFile);
        final Path target = manager.getTargetPath(srcFile);

        Files.copy(src, target);

        assertThat(Files.isRegularFile(src), is(equalTo(true)));
        assertThat(Files.isRegularFile(target), is(equalTo(true)));

        manager.processingDone(srcFile, null);

        assertThat(Files.isRegularFile(src), is(equalTo(true)));
        assertThat(Files.isRegularFile(target), is(equalTo(true)));
    }

    @Test
    public void testProcessingFinished_newFileCase_errorCase() throws Exception {
        final SourceTargetManager manager = new SourceTargetManager(getConfig());
        final Path src = manager.getSource(srcFile);
        final Path target = manager.getTargetPath(srcFile);

        Files.copy(src, target);

        assertThat(Files.isRegularFile(src), is(equalTo(true)));
        assertThat(Files.isRegularFile(target), is(equalTo(true)));

        manager.processingDone(srcFile, new RuntimeException());

        assertThat(Files.isRegularFile(src), is(equalTo(true)));
        assertThat(Files.isRegularFile(target), is(equalTo(true)));
    }

    @Test
    public void testProcessingFinished_overwriteFileCase() throws Exception {
        root.removeChild(NEW_FILES);
        root.addContent(new Element(OVERWRITE));

        final SourceTargetManager manager = new SourceTargetManager(getConfig());
        final Path src = manager.getSource(srcFile);
        final Path target = manager.getTargetPath(srcFile);

        Files.copy(src, target);

        assertThat(Files.isRegularFile(src), is(equalTo(true)));
        assertThat(Files.isRegularFile(target), is(equalTo(true)));

        manager.processingDone(srcFile, null);

        assertThat(Files.exists(src), is(equalTo(false)));
        assertThat(Files.isRegularFile(target), is(equalTo(true)));

    }

    @Test
    public void testProcessingFinished_overwriteFileCase_errorCase() throws Exception {
        root.removeChild(NEW_FILES);
        root.addContent(new Element(OVERWRITE));

        final SourceTargetManager manager = new SourceTargetManager(getConfig());
        final Path src = manager.getSource(srcFile);
        final Path target = manager.getTargetPath(srcFile);

        Files.copy(src, target);

        assertThat(Files.isRegularFile(src), is(equalTo(true)));
        assertThat(Files.isRegularFile(target), is(equalTo(true)));

        final OutputStream os = Files.newOutputStream(target, StandardOpenOption.APPEND);
        os.write(" modified".getBytes());
        os.close();

        manager.processingDone(srcFile, new RuntimeException());

        assertThat(Files.exists(src), is(equalTo(false)));
        assertThat(Files.isRegularFile(target), is(equalTo(true)));

        final InputStream is = Files.newInputStream(target, StandardOpenOption.READ);
        final byte[] buffer = new byte[2000];
        is.read(buffer);
        is.close();
        assertEquals(fileContent, new String(buffer).trim());
    }

    private PostProcessingConfig getConfig() throws Exception {
        final Document document = new Document(root);

        final ByteArrayOutputStream bs = new ByteArrayOutputStream();
        new XMLOutputter(Format.getPrettyFormat()).output(document, bs);
        bs.close();

        return PostProcessingConfig.load(new ByteArrayInputStream(bs.toByteArray()));
    }
}