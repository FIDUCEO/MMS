package com.bc.fiduceo.db;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

public class DbMaintenanceToolTest {

    @Test
    public void testGetOptions() {
        final Options options = DbMaintenanceTool.getOptions();
        assertNotNull(options);

        final Option helpOption = options.getOption("h");
        assertNotNull(helpOption);
        assertEquals("h", helpOption.getOpt());
        assertEquals("help", helpOption.getLongOpt());
        assertEquals("Prints the tool usage.", helpOption.getDescription());
        assertFalse(helpOption.hasArg());

        final Option configOption = options.getOption("config");
        assertNotNull(configOption);
        assertEquals("c", configOption.getOpt());
        assertEquals("config", configOption.getLongOpt());
        assertEquals("Defines the configuration directory. Defaults to './config'.", configOption.getDescription());
        assertTrue(configOption.hasArg());

        final Option pathOption = options.getOption("path");
        assertNotNull(pathOption);
        assertEquals("p", pathOption.getOpt());
        assertEquals("path", pathOption.getLongOpt());
        assertEquals("Observation path segment to be replaced.", pathOption.getDescription());
        assertTrue(pathOption.hasArg());

        final Option replaceOption = options.getOption("replace");
        assertNotNull(replaceOption);
        assertEquals("r", replaceOption.getOpt());
        assertEquals("replace", replaceOption.getLongOpt());
        assertEquals("Observation path segment replacement.", replaceOption.getDescription());
        assertTrue(replaceOption.hasArg());
    }

    @Test
    public void testPrintUsageTo() {
        final String ls = System.lineSeparator();
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final DbMaintenanceTool tool = new DbMaintenanceTool();

        tool.printUsageTo(stream);

        assertEquals("db-maintenance-tool version 1.4.5" + ls +
                ls +
                "usage: db-maintenance-tool <options>" + ls +
                "Valid options are:" + ls +
                "   -c,--config <arg>    Defines the configuration directory. Defaults to './config'." + ls +
                "   -h,--help            Prints the tool usage." + ls +
                "   -p,--path <arg>      Observation path segment to be replaced." + ls +
                "   -r,--replace <arg>   Observation path segment replacement." + ls, stream.toString());
    }
}
