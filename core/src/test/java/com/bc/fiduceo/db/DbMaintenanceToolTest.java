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
        assertEquals("Observation path segment to be replaced or truncated.", pathOption.getDescription());
        assertTrue(pathOption.hasArg());

        final Option replaceOption = options.getOption("replace");
        assertNotNull(replaceOption);
        assertEquals("r", replaceOption.getOpt());
        assertEquals("replace", replaceOption.getLongOpt());
        assertEquals("Observation path segment replacement.", replaceOption.getDescription());
        assertTrue(replaceOption.hasArg());

        final Option truncateOption = options.getOption("truncate");
        assertNotNull(truncateOption);
        assertEquals("t", truncateOption.getOpt());
        assertEquals("truncate", truncateOption.getLongOpt());
        assertEquals("Command to truncate path segment.", truncateOption.getDescription());
        assertFalse(truncateOption.hasArg());
    }

    @Test
    public void testPrintUsageTo() {
        final String ls = System.lineSeparator();
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final DbMaintenanceTool tool = new DbMaintenanceTool();

        tool.printUsageTo(stream);

        assertEquals("db-maintenance-tool version 1.5.7" + ls +
                ls +
                "usage: db_maintenance <options>" + ls +
                "Valid options are:" + ls +
                "   -c,--config <arg>     Defines the configuration directory. Defaults to './config'." + ls +
                "   -d,--dryrun           Defines 'dryrun' status, i.e. just test the replacement and report problems." + ls +
                "   -h,--help             Prints the tool usage." + ls +
                "   -p,--path <arg>       Observation path segment to be replaced or truncated." + ls +
                "   -r,--replace <arg>    Observation path segment replacement." + ls +
                "   -s,--segments <arg>   Number of segments to consider for paths missing the search expression (default: 4)" + ls +
                "   -t,--truncate         Command to truncate path segment." + ls, stream.toString());
    }
}
