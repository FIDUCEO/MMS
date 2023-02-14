package com.bc.fiduceo.qc;

import com.bc.fiduceo.log.FiduceoLogger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class MmdQCToolMain {

    public static void main(String[] args) throws ParseException {
        final MmdQCTool mmdQCTool = new MmdQCTool();

        if (args.length == 0) {
            MmdQCTool.printUsageTo(System.err);
            return;
        }

        final CommandLineParser parser = new PosixParser();
        final CommandLine commandLine = parser.parse(MmdQCTool.getOptions(), args);
        if (commandLine.hasOption("h") || commandLine.hasOption("--help")) {
            MmdQCTool.printUsageTo(System.err);
            return;
        }

        try {
            mmdQCTool.run(commandLine);
        } catch (Throwable e) {
            FiduceoLogger.getLogger().severe(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
