package com.bc.fiduceo.qc;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

public class MmdQCToolTest {

    @Test
    public void testPrintUsageTo() {
        final String ls = System.lineSeparator();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MmdQCTool.printUsageTo(outputStream);

        assertEquals("mmd-qc-tool version 1.5.8" + ls +
                ls +
                "usage: mmd-qc-tool <options>" + ls +
                "Valid options are:" + ls +
                "   -h,--help                Prints the tool usage." + ls +
                "   -i,--input <arg>         Defines the MMD input directory." + ls +
                "   -lat,--latitude <arg>    Defines the variable name for the latitude." + ls +
                "   -lon,--longitude <arg>   Defines the variable name for the longitude." + ls +
                "   -o,--outdir <arg>        Defines the result output directory." + ls +
                "   -p,--plot                Enables plotting the matchup locations onto a global map. Requires 'lon' and 'lat' to be" + ls +
                "                            set." + ls +
                "   -t,--time <arg>          Defines matchup time variable name." + ls, outputStream.toString());
    }

    @Test
    public void testGetOptions() {
        final Options options = MmdQCTool.getOptions();
        assertEquals(7, options.getOptions().size());

        Option o = options.getOption("h");
        assertEquals("help", o.getLongOpt());
        assertEquals("Prints the tool usage.", o.getDescription());
        assertFalse(o.hasArg());
        assertFalse(o.isRequired());

        o = options.getOption("i");
        assertEquals("input", o.getLongOpt());
        assertEquals("Defines the MMD input directory.", o.getDescription());
        assertTrue(o.hasArg());
        assertTrue(o.isRequired());

        o = options.getOption("o");
        assertEquals("outdir", o.getLongOpt());
        assertEquals("Defines the result output directory.", o.getDescription());
        assertTrue(o.hasArg());
        assertFalse(o.isRequired());

        o = options.getOption("t");
        assertEquals("time", o.getLongOpt());
        assertEquals("Defines matchup time variable name.", o.getDescription());
        assertTrue(o.hasArg());
        assertTrue(o.isRequired());

        o = options.getOption("p");
        assertEquals("plot", o.getLongOpt());
        assertEquals("Enables plotting the matchup locations onto a global map. Requires 'lon' and 'lat' to be set.", o.getDescription());
        assertFalse(o.hasArg());
        assertFalse(o.isRequired());

        o = options.getOption("lon");
        assertEquals("longitude", o.getLongOpt());
        assertEquals("Defines the variable name for the longitude.", o.getDescription());
        assertTrue(o.hasArg());
        assertFalse(o.isRequired());

        o = options.getOption("lat");
        assertEquals("latitude", o.getLongOpt());
        assertEquals("Defines the variable name for the latitude.", o.getDescription());
        assertTrue(o.hasArg());
        assertFalse(o.isRequired());
    }

    @Test
    public void testWriteReport_empty() {
        final String ls = System.lineSeparator();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MmdQCTool.writeReport(outputStream, new MatchupAccumulator(), new FileMessages());

        assertEquals("Analysed 0 file(s)" + ls, outputStream.toString());
    }

    @Test
    public void testWriteReport_one_file_some_matches_one_day() {
        final String ls = System.lineSeparator();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final MatchupAccumulator accumulator = new MatchupAccumulator();

        accumulator.countFile();

        accumulator.add(1660262400);
        accumulator.add(1660262500);
        accumulator.add(1660262600);
        accumulator.add(1660262700);
        accumulator.add(1660262800);

        MmdQCTool.writeReport(outputStream, accumulator, new FileMessages());

        assertEquals("Analysed 1 file(s)" + ls + ls +
                "0 file(s) with errors" + ls + ls +
                "Total number of matchups: 5" + ls +
                "Daily distribution:" + ls +
                "2022-08-12: 5" + ls, outputStream.toString());
    }

    @Test
    public void testWriteReport_one_file_some_matches_with_errors() {
        final String ls = System.lineSeparator();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final MatchupAccumulator accumulator = new MatchupAccumulator();

        accumulator.countFile();

        accumulator.add(1660262400);
        accumulator.add(1660262800);

        final FileMessages fileMessages = new FileMessages();
        fileMessages.add("heffalump_3.nc", "Unable to read metadata");
        fileMessages.add("heffalump_3.nc", "Quirky format");


        MmdQCTool.writeReport(outputStream, accumulator, fileMessages);

        assertEquals("Analysed 1 file(s)" + ls + ls +
                "1 file(s) with errors" + ls +
                "heffalump_3.nc" + ls +
                " - Unable to read metadata" + ls +
                " - Quirky format" + ls + ls +
                "Total number of matchups: 2" + ls +
                "Daily distribution:" + ls +
                "2022-08-12: 2" + ls, outputStream.toString());
    }
}
