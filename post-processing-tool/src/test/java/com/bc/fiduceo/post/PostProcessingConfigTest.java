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

import com.bc.fiduceo.post.plugin.DummyPostProcessingPlugin;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PostProcessingConfigTest {

    private static final String CONFIG = PostProcessingConfig.TAG_NAME_ROOT;
    private static final String PROCESSINGS = PostProcessingConfig.TAG_NAME_POST_PROCESSINGS;
    private static final String NEW_FILES = PostProcessingConfig.TAG_NAME_NEW_FILES;
    private static final String OUTPUT_DIR = PostProcessingConfig.TAG_NAME_OUTPUT_DIR;
    private static final String OVERWRITE = PostProcessingConfig.TAG_NAME_OVERWRITE;

    private static final String DUMMY_NAME = DummyPostProcessingPlugin.DUMMY_POST_PROCESSING_NAME;
    private Element root;

    @Before
    public void setUp() throws Exception {
        root = new Element(CONFIG).addContent(Arrays.asList(
                new Element(NEW_FILES).addContent(
                        new Element(OUTPUT_DIR).addContent("An_Output_Directory")),
                new Element(PROCESSINGS).addContent(Arrays.asList(
                        new Element(DUMMY_NAME).addContent("A"),
                        new Element(DUMMY_NAME).addContent("B"),
                        new Element(DUMMY_NAME).addContent("C")
                ))
        ));
    }

    @Test
    public void testStore() throws Exception {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        getConfig().store(outputStream);

        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<post-processing-config>");
        pw.println("  <create-new-files>");
        pw.println("    <output-directory>An_Output_Directory</output-directory>");
        pw.println("  </create-new-files>");
        pw.println("  <post-processings>");
        pw.println("    <" + DUMMY_NAME + ">A</" + DUMMY_NAME + ">");
        pw.println("    <" + DUMMY_NAME + ">B</" + DUMMY_NAME + ">");
        pw.println("    <" + DUMMY_NAME + ">C</" + DUMMY_NAME + ">");
        pw.println("  </post-processings>");
        pw.println("</post-processing-config>");
//        pw.println();
        pw.flush();

        assertEquals(sw.toString().replaceAll("\\s+",""), outputStream.toString().replaceAll("\\s+",""));
    }

    @Test
    public void testValidInitialised() throws Exception {
        final List<Element> postProcessingElements = getConfig().getPostProcessingElements();

        assertNotNull(postProcessingElements);
        assertEquals("java.util.Collections$UnmodifiableList", postProcessingElements.getClass().getTypeName());
        assertEquals(3, postProcessingElements.size());

        Element postProcessingElem;

        postProcessingElem = postProcessingElements.get(0);
        assertNotNull(postProcessingElem);
        assertEquals(DUMMY_NAME, postProcessingElem.getName());
        assertEquals("A", postProcessingElem.getValue());

        postProcessingElem = postProcessingElements.get(1);
        assertNotNull(postProcessingElem);
        assertEquals(DUMMY_NAME, postProcessingElem.getName());
        assertEquals("B", postProcessingElem.getValue());

        postProcessingElem = postProcessingElements.get(2);
        assertNotNull(postProcessingElem);
        assertEquals(DUMMY_NAME, postProcessingElem.getName());
        assertEquals("C", postProcessingElem.getValue());
    }

    @Test
    public void testLoad_createNewFiles() throws Exception {
        final PostProcessingConfig config = getConfig();

        assertTrue(config.isNewFiles());
        assertEquals("An_Output_Directory", config.getOutputDirectory());
    }

    @Test
    public void testLoad_overwrite() throws Exception {
        root.removeChild(NEW_FILES);
        root.addContent(new Element(OVERWRITE));

        final PostProcessingConfig config = getConfig();

        assertTrue(config.isOverwrite());
        assertFalse(config.isNewFiles());
        assertNull(config.getOutputDirectory());
    }

    @Test
    public void testLoad_overrideAndNewFilesAreNotAllowedAtTheSameTime() throws Exception {
        root.addContent(new Element(OVERWRITE));

        try {
            getConfig();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String message = expected.getMessage();
            assertTrue(message.startsWith("Unable to initialize post processing configuration: "));
            assertTrue(message.contains("Tag <" + NEW_FILES + "> and <" + OVERWRITE + "> is not allowed at the same time."));
        }
    }

    @Test
    public void testLoad_eitherOverrideOrNewFilesMustBeConfigured() throws Exception {
        root.removeChild(NEW_FILES);

        try {
            getConfig();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String message = expected.getMessage();
            assertTrue(message.startsWith("Unable to initialize post processing configuration: "));
            assertTrue(message.contains("Either <" + NEW_FILES + "> or <" + OVERWRITE + "> must be configured."));
        }
    }

    @Test
    public void testLoad_emptyPostProcessings() throws Exception {
        root.getChild(PROCESSINGS).removeContent();
        try {
            getConfig();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String message = expected.getMessage();
            assertTrue(message.startsWith("Unable to initialize post processing configuration: "));
            assertTrue(message.contains("Empty list of post processings."));
        }
    }

    @Test
    public void testLoad_CauseIsJDOMParseException() {
        final InputStream inputStream = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><post-processing-".getBytes());
        try {
            PostProcessingConfig.load(inputStream);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("org.jdom.input.JDOMParseException", expected.getCause().getClass().getTypeName());
            assertTrue(expected.getMessage().matches("Unable to initialize post processing configuration: .*"));
        }
    }

    @Test
    public void testLoad_CauseIsRuntimeException() {
        final InputStream inputStream = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><post-processing-config/>".getBytes());
        try {
            PostProcessingConfig.load(inputStream);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("java.lang.RuntimeException", expected.getCause().getClass().getTypeName());
            assertTrue(expected.getMessage().matches("Unable to initialize post processing configuration: .*"));
        }
    }

    @Test
    public void testLoad_CaseIOException() throws Exception {
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read()).thenThrow(IOException.class);

        try {
            PostProcessingConfig.load(inputStream);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("java.io.IOException", expected.getCause().getClass().getTypeName());
            assertTrue(expected.getMessage().matches("Unable to initialize post processing configuration: .*"));
        }
    }

    private PostProcessingConfig getConfig() throws Exception {
        final Document document = new Document(root);

        final ByteArrayOutputStream bs = new ByteArrayOutputStream();
        new XMLOutputter(Format.getPrettyFormat()).output(document, bs);
        bs.close();

        return PostProcessingConfig.load(new ByteArrayInputStream(bs.toByteArray()));
    }
}
