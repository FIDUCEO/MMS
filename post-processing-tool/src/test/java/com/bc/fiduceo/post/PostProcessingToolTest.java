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
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class PostProcessingToolTest {

    private static final String CONFIG = PostProcessingConfig.TAG_NAME_ROOT;
    private static final String PROCESSINGS = PostProcessingConfig.TAG_NAME_POST_PROCESSINGS;
    private static final String NEW_FILES = PostProcessingConfig.TAG_NAME_NEW_FILES;
    private static final String OUTPUT_DIR = PostProcessingConfig.TAG_NAME_OUTPUT_DIR;

    private static final String DUMMY_NAME = DummyPostProcessingPlugin.DUMMY_POST_PROCESSING_NAME;
    private Element root;

    @Before
    public void setUp() throws Exception {
        root = new Element(CONFIG).addContent(Arrays.asList(
                new Element(NEW_FILES).addContent(
                        new Element(OUTPUT_DIR).addContent("An_Output_Directory")),
                new Element(PROCESSINGS).addContent(
                        new Element(DUMMY_NAME).addContent("C")
                )
        ));
    }


    @Test
    public void testOptions() {
        final Options options = PostProcessingTool.getOptions();
        assertEquals(6, options.getOptions().size());

        Option o;

        o = options.getOption("c");
        assertNotNull(o);
        assertEquals("config", o.getLongOpt());
        assertEquals("Defines the configuration directory. Defaults to './config'.", o.getDescription());
        assertEquals(true, o.hasArg());
        assertEquals(false, o.isRequired());


        o = options.getOption("i");
        assertNotNull(o);
        assertEquals("input-dir", o.getLongOpt());
        assertEquals("Defines the path to the input mmd files directory.", o.getDescription());
        assertEquals(true, o.hasArg());
        assertEquals(true, o.isRequired());

        o = options.getOption("end");
        assertNotNull(o);
        assertEquals("end-date", o.getLongOpt());
        assertEquals("Defines the processing end-date, format 'yyyy-DDD'. DDD = Day of year.", o.getDescription());
        assertEquals(true, o.hasArg());
        assertEquals(true, o.isRequired());

        o = options.getOption("h");
        assertNotNull(o);
        assertEquals("help", o.getLongOpt());
        assertEquals("Prints the tool usage.", o.getDescription());
        assertEquals(false, o.hasArg());
        assertEquals(false, o.isRequired());

        o = options.getOption("j");
        assertNotNull(o);
        assertEquals("job-config", o.getLongOpt());
        assertEquals("Defines the path to post processing job configuration file. Path is relative to the configuration directory.", o.getDescription());
        assertEquals(true, o.hasArg());
        assertEquals(true, o.isRequired());

        o = options.getOption("start");
        assertNotNull(o);
        assertEquals("start-date", o.getLongOpt());
        assertEquals("Defines the processing start-date, format 'yyyy-DDD'. DDD = Day of year.", o.getDescription());
        assertEquals(true, o.hasArg());
        assertEquals(true, o.isRequired());
    }

    @Test
    public void testPrintUsage() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        PostProcessingTool.printUsageTo(out);
//        PostProcessingTool.printUsageTo(System.out);

        final String ls = System.lineSeparator();
        final String expected = "post-processing-tool version 1.2.9" + ls +
                "" + ls +
                "usage: post-processing-tool <options>" + ls +
                "Valid options are:" + ls +
                "   -c,--config <arg>           Defines the configuration directory. Defaults to './config'." + ls +
                "   -end,--end-date <arg>       Defines the processing end-date, format 'yyyy-DDD'. DDD = Day of year." + ls +
                "   -h,--help                   Prints the tool usage." + ls +
                "   -i,--input-dir <arg>        Defines the path to the input mmd files directory." + ls +
                "   -j,--job-config <arg>       Defines the path to post processing job configuration file. Path is relative to the" + ls +
                "                               configuration directory." + ls +
                "   -start,--start-date <arg>   Defines the processing start-date, format 'yyyy-DDD'. DDD = Day of year.";
        assertEquals(expected, out.toString().trim());
    }

    @Test
    public void testRun() throws Exception {
        final Group rootGroup = mock(Group.class);
        when(rootGroup.getShortName()).thenReturn("root");

        final NetcdfFile reader = mock(NetcdfFile.class);
        when(reader.getRootGroup()).thenReturn(rootGroup);

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        final PostProcessing p1 = mock(PostProcessing.class);
        final PostProcessing p2 = mock(PostProcessing.class);

        final PostProcessingContext context = new PostProcessingContext();
        context.setProcessingConfig(getConfig());
        final PostProcessingTool tool = new PostProcessingTool(context);

        tool.run(reader, writer, Arrays.asList(p1, p2));

        final InOrder inOrder = inOrder(reader, writer, p1, p2);
        inOrder.verify(p1, times(1)).getVariableNamesToRemove();
        inOrder.verify(p2, times(1)).getVariableNamesToRemove();

        inOrder.verify(writer, times(1)).addGroup(null, "root");
        inOrder.verify(p1, times(1)).prepare(reader, writer);
        inOrder.verify(p2, times(1)).prepare(reader, writer);

        inOrder.verify(writer, times(1)).create();
        inOrder.verify(p1, times(1)).compute(same(reader), same(writer));
        inOrder.verify(p2, times(1)).compute(same(reader), same(writer));
    }

    @Test
    public void addPostProcessingConfig_newAttribute() throws Exception {
        final String attName = "post-processing-configuration";

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        when(writer.findGlobalAttribute(attName)).thenReturn(null);

        final PostProcessingContext context = new PostProcessingContext();
        context.setProcessingConfig(getConfig());
        final PostProcessingTool tool = new PostProcessingTool(context);

        tool.addPostProcessingConfig(writer);

        final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<post-processing-config>" +
                "  <create-new-files>" +
                "    <output-directory>An_Output_Directory</output-directory>" +
                "  </create-new-files>" +
                "  <post-processings>" +
                "    <dummy-post-processing>C</dummy-post-processing>" +
                "  </post-processings> " +
                "</post-processing-config>";

        final ArgumentCaptor<Attribute> attribCaptor = ArgumentCaptor.forClass(Attribute.class);
        verify(writer, times(1)).findGlobalAttribute(attName);
        verify(writer, times(1)).addGroupAttribute(isNull(Group.class), attribCaptor.capture());
        assertThat(attribCaptor.getValue().getStringValue(), equalToIgnoringWhiteSpace(expected));
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void addPostProcessingConfig_existingAttribute() throws Exception {
        final String attName = "post-processing-configuration";
        final String prevoiusContent = "previous content";

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        when(writer.findGlobalAttribute(attName)).thenReturn(new Attribute(attName, prevoiusContent));

        final PostProcessingContext context = new PostProcessingContext();
        context.setProcessingConfig(getConfig());
        final PostProcessingTool tool = new PostProcessingTool(context);

        tool.addPostProcessingConfig(writer);

        final String expected = prevoiusContent + " " +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<post-processing-config>" +
                "  <create-new-files>" +
                "    <output-directory>An_Output_Directory</output-directory>" +
                "  </create-new-files>" +
                "  <post-processings>" +
                "    <dummy-post-processing>C</dummy-post-processing>" +
                "  </post-processings> " +
                "</post-processing-config>";

        final ArgumentCaptor<Attribute> attribCaptor = ArgumentCaptor.forClass(Attribute.class);
        verify(writer, times(1)).findGlobalAttribute(attName);
        verify(writer, times(1)).addGroupAttribute(isNull(Group.class), attribCaptor.capture());
        verify(writer, times(1)).deleteGroupAttribute(null, attName);
        assertThat(attribCaptor.getValue().getStringValue(), equalToIgnoringWhiteSpace(expected));
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void testFilenameIsInTimeRange() throws Exception {
        final String fileStart = "2005-123";
        final String filename = "AnyCharactersBefore_" + fileStart + "_" + "anyTime" + ".nc";

        long startTime = TimeUtils.parseDOYBeginOfDay(fileStart).getTime();
        long endTime = startTime;

        assertTrue(PostProcessingTool.isFileInTimeRange(startTime, endTime, filename));
        assertFalse(PostProcessingTool.isFileInTimeRange(startTime + 1, endTime + 1, filename));
        assertFalse(PostProcessingTool.isFileInTimeRange(startTime - 1, endTime - 1, filename));
    }

    @Test
    public void testGetDate() throws Exception {
        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("start")).thenReturn("TheDateString");

        final String start = PostProcessingTool.getDate(commandLine, "start");

        assertEquals("TheDateString", start);
    }

    @Test
    public void testGetDate_emptyString() throws Exception {
        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("start")).thenReturn("");

        try {
            PostProcessingTool.getDate(commandLine, "start");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of cmd-line parameter 'start' is missing.", expected.getMessage());
        }
    }

    @Test
    public void testGetDate_null() throws Exception {
        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("start")).thenReturn(null);

        try {
            PostProcessingTool.getDate(commandLine, "start");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of cmd-line parameter 'start' is missing.", expected.getMessage());
        }
    }

    @Test
    public void testGetVariableRemoveNamesList_noPostProcessings() {
        final ArrayList<PostProcessing> emptyList = new ArrayList<>();

        final List<String> namesList = PostProcessingTool.getVariableRemoveNamesList(emptyList);
        assertEquals(0, namesList.size());
    }

    @Test
    public void testGetVariableRemoveNamesList_onePostProcessing() {
        final List<String> toRemoveNameslist = new ArrayList<>();
        toRemoveNameslist.add("Karl-Heinz");
        toRemoveNameslist.add("Donald");

        final ArrayList<PostProcessing> postProcessingsList = new ArrayList<>();

        final PostProcessing postProcessing = mock(PostProcessing.class);
        when(postProcessing.getVariableNamesToRemove()).thenReturn(toRemoveNameslist);
        postProcessingsList.add(postProcessing);


        final List<String> namesList = PostProcessingTool.getVariableRemoveNamesList(postProcessingsList);
        assertEquals(2, namesList.size());
        assertTrue(namesList.contains("Karl-Heinz"));
        assertTrue(namesList.contains("Donald"));
    }

    @Test
    public void testGetVariableRemoveNamesList_twoPostProcessings_duplicateNames() {
        final List<String> toRemoveNameslist_1 = new ArrayList<>();
        toRemoveNameslist_1.add("Karl-Heinz");
        toRemoveNameslist_1.add("Donald");

        final List<String> toRemoveNameslist_2 = new ArrayList<>();
        toRemoveNameslist_2.add("Hermann");
        toRemoveNameslist_2.add("Donald");

        final ArrayList<PostProcessing> postProcessingsList = new ArrayList<>();

        final PostProcessing postProcessing_1 = mock(PostProcessing.class);
        when(postProcessing_1.getVariableNamesToRemove()).thenReturn(toRemoveNameslist_1);
        postProcessingsList.add(postProcessing_1);

        final PostProcessing postProcessing_2 = mock(PostProcessing.class);
        when(postProcessing_2.getVariableNamesToRemove()).thenReturn(toRemoveNameslist_2);
        postProcessingsList.add(postProcessing_2);


        final List<String> namesList = PostProcessingTool.getVariableRemoveNamesList(postProcessingsList);
        assertEquals(3, namesList.size());
        assertTrue(namesList.contains("Karl-Heinz"));
        assertTrue(namesList.contains("Donald"));
        assertTrue(namesList.contains("Hermann"));
    }

    private PostProcessingConfig getConfig() throws Exception {
        final Document document = new Document(root);

        final ByteArrayOutputStream bs = new ByteArrayOutputStream();
        new XMLOutputter(Format.getPrettyFormat()).output(document, bs);
        bs.close();

        return PostProcessingConfig.load(new ByteArrayInputStream(bs.toByteArray()));
    }
}
