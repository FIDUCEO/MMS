package com.bc.fiduceo.db;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import java.io.IOException;
import java.sql.SQLException;

public class DbMaintenanceToolMain {

    public static void main(String[] args) throws ParseException {
        final DbMaintenanceTool maintenanceTool = new DbMaintenanceTool();

        if (args.length == 0) {
            maintenanceTool.printUsageTo(System.err);
            return;
        }

        final CommandLineParser parser = new PosixParser();
        final CommandLine commandLine = parser.parse(DbMaintenanceTool.getOptions(), args);
        if (commandLine.hasOption("h") || commandLine.hasOption("--help")) {
            maintenanceTool.printUsageTo(System.out);
            return;
        }

        try {
            maintenanceTool.run(commandLine);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
