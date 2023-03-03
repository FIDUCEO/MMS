package com.bc.fiduceo.qc;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.util.NetCDFUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bc.fiduceo.FiduceoConstants.VERSION_NUMBER;
import static com.bc.fiduceo.util.MMDUtil.getMMDFileNamePattern;

class MmdQCTool {

    private final static Logger logger = FiduceoLogger.getLogger();

    private FileMessages fileMessages;
    private MatchupAccumulator matchupAccumulator;

    // package access for testing only tb 2023-02-14
    static Options getOptions() {
        final Options options = new Options();

        final Option helpOption = new Option("h", "help", false, "Prints the tool usage.");
        options.addOption(helpOption);

        final Option inputOption = new Option("i", "input", true, "Defines the MMD input directory.");
        inputOption.setRequired(true);
        options.addOption(inputOption);

        final Option timeOption = new Option("t", "time", true, "Defines matchup time variable name.");
        timeOption.setRequired(true);
        options.addOption(timeOption);

        return options;
    }

    // package access for testing only tb 2023-02-14
    static void writeReport(OutputStream outputStream, MatchupAccumulator accumulator, FileMessages fileMessages) {
        final PrintWriter writer = new PrintWriter(outputStream);

        final int fileCount = accumulator.getFileCount();
        writer.println("Analysed " + fileCount + " file(s)");
        if (fileCount == 0) {
            writer.flush();
            return;
        }

        writer.println();
        final HashMap<String, List<String>> messageMap = fileMessages.getMessageMap();
        final int size = messageMap.size();
        writer.println(size + " file(s) with errors");
        if (size > 0) {
            // @todo 1 tb/tb add error messages per file here
        }

        writer.println();
        writer.println("Total number of matchups: " + accumulator.getSummaryCount());
        writer.println("Daily distribution:");

        final TreeMap<String, Integer> treeMap = new TreeMap<>(accumulator.getDaysMap());
        final Set<Map.Entry<String, Integer>> entries = treeMap.entrySet();
        for (final Map.Entry<String, Integer> entry : entries) {
            writer.println(entry.getKey() + ": " + entry.getValue());
        }

        writer.flush();
    }

    void run(CommandLine commandLine) throws IOException {
        final String inputDirOption = commandLine.getOptionValue("i");
        final List<Path> mmdFiles = getInputFiles(inputDirOption);
        logger.info("Found " + mmdFiles.size() + " input file(s) to analyze.");

        fileMessages = new FileMessages();
        matchupAccumulator = new MatchupAccumulator();

        // loop over files
        for (final Path mmdFile : mmdFiles) {

            try (final NetcdfFile netcdfFile = NetCDFUtils.openReadOnly(mmdFile.toAbsolutePath().toString())) {

                // read time variable center pixel
                final String timeVariableName = commandLine.getOptionValue("t");
                final Array timeArray = NetCDFUtils.getCenterPosArrayFromMMDFile(netcdfFile, timeVariableName, null,
                        null, FiduceoConstants.MATCHUP_COUNT);
                final IndexIterator iterator = timeArray.getIndexIterator();
                while (iterator.hasNext()) {
                    final int time = iterator.getIntNext();
                    matchupAccumulator.add(time);
                }
            } catch (IOException | InvalidRangeException ioException) {
                fileMessages.add(mmdFile.getFileName().toString(), ioException.getMessage());
            }
        }

        // write report
        final TreeMap<String, Integer> treeMap = new TreeMap<>(matchupAccumulator.getDaysMap());
        final Set<Map.Entry<String, Integer>> entries = treeMap.entrySet();
        for (final Map.Entry<String, Integer> entry : entries) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    private List<Path> getInputFiles(String inputDirOption) throws IOException {
        final File inputDir = new File(inputDirOption);
        if (!inputDir.isDirectory()) {
            throw new IOException("Not a valid directory: " + inputDir.getAbsolutePath());
        }

        final Pattern pattern = getMMDFileNamePattern();
        try (Stream<Path> pathStream = Files.walk(inputDir.toPath())) {
            final Stream<Path> regularFiles = pathStream.filter(Files::isRegularFile);
            final Stream<Path> mmdFileStream = regularFiles.filter(path -> pattern.matcher(path.getFileName().toString()).matches());
            return mmdFileStream.collect(Collectors.toList());
        }
    }

    static void printUsageTo(OutputStream outputStream) {
        final String ls = System.lineSeparator();
        final PrintWriter writer = new PrintWriter(outputStream);
        writer.write("mmd-qc-tool version " + VERSION_NUMBER);
        writer.write(ls + ls);

        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(writer, 120, "matchup-tool <options>", "Valid options are:",
                getOptions(), 3, 3, "");

        writer.flush();
    }
}
