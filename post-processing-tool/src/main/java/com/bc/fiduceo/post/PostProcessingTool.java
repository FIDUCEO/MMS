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
 *
 */

package com.bc.fiduceo.post;

import static com.bc.fiduceo.FiduceoConstants.VERSION_NUMBER;

import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PostProcessingTool {
    private final static Logger logger = FiduceoLogger.getLogger();

    public void run(CommandLine commandLine) throws IOException, InvalidRangeException {
        final PostProcessingContext context = initialize(commandLine);
        runPostProcessing(context);
    }

    static void runPostProcessing(PostProcessingContext context) throws IOException, InvalidRangeException {
        final Path inputDirectory = context.getMmdInputDirectory();
        final Pattern pattern = Pattern.compile("mmd\\d{1,2}_.*_.*_\\d{4}-\\d{3}_\\d{4}-\\d{3}.nc");

        try (Stream<Path> pathStream = Files.walk(inputDirectory)) {
            final Stream<Path> regularFiles = pathStream.filter(path -> Files.isRegularFile(path));
            final Stream<Path> mmdFileStream = regularFiles.filter(path -> pattern.matcher(path.getFileName().toString()).matches());
            List<Path> mmdFiles = mmdFileStream.collect(Collectors.toList());

            computeFiles(mmdFiles, context);
        }
    }

    static void computeFiles(List<Path> mmdFiles, PostProcessingContext context) {
        for (Path mmdFile : mmdFiles) {
            try {
                computeFile(mmdFile, context);
            } catch (Exception e) {
                logger.severe("Unable to execute post processing for matchup '" + mmdFile.getFileName().toString() + "'");
                logger.severe("Cause: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void computeFile(Path mmdFile, PostProcessingContext context) throws IOException, InvalidRangeException {
        final long startTime = context.getStartDate().getTime();
        final long endTime = context.getEndDate().getTime();
        if (isFileInTimeRange(startTime, endTime, mmdFile.getFileName().toString())) {
            final String mmdAbsFile = mmdFile.toAbsolutePath().toString();
            // todo 1 se/se implement overwrite or new files in dedicated output directory
            try (NetcdfFileWriter netcdfFileWriter = NetcdfFileWriter.openExisting(mmdAbsFile)) {
                run(netcdfFileWriter, context.getProcessingConfig().getProcessings());
            }
        }
    }

    static void run(NetcdfFileWriter ncFile, List<PostProcessing> postProcessings) throws IOException, InvalidRangeException {
        ncFile.setRedefineMode(true);
        for (PostProcessing postProcessing : postProcessings) {
            postProcessing.prepare(ncFile);
        }
        ncFile.setRedefineMode(false);
        for (PostProcessing postProcessing : postProcessings) {
            postProcessing.compute(ncFile);
        }
    }

    // package access for testing only se 2016-11-28
    static String getDate(CommandLine commandLine, final String optionName) {
        final String dateString = commandLine.getOptionValue(optionName);
        if (StringUtils.isNullOrEmpty(dateString)) {
            throw new RuntimeException("Value of cmd-line parameter '" + optionName + "' is missing.");
        }
        return dateString;
    }

    // package access for testing only se 2016-11-28
    static void printUsageTo(OutputStream outputStream) {
        final PrintWriter writer = new PrintWriter(outputStream);
        writer.println("post-processing-tool version " + VERSION_NUMBER);
        writer.println();

        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(writer, 120, "post-processing-tool <options>", "Valid options are:", getOptions(), 3, 3, "");

        writer.flush();
    }

    // package access for testing only se 2016-11-28
    static Options getOptions() {
        final Options options = new Options();

        final Option helpOption = new Option("h", "help", false, "Prints the tool usage.");
        options.addOption(helpOption);

        final Option configOption = new Option("c", "config", true, "Defines the configuration directory. Defaults to './config'.");
        options.addOption(configOption);

        final Option mmdDir = new Option("i", "input-dir", true, "Defines the path to the input mmd files directory.");
        mmdDir.setRequired(true);
        options.addOption(mmdDir);

        final Option ppuc = new Option("j", "job-config", true, "Defines the path to post processing job configuration file. Path is relative to the configuration directory.");
        ppuc.setRequired(true);
        options.addOption(ppuc);

        final Option startOption = new Option("start", "start-date", true, "Defines the processing start-date, format 'yyyy-DDD'. DDD = Day of year.");
        startOption.setRequired(true);
        options.addOption(startOption);

        final Option endOption = new Option("end", "end-date", true, "Defines the processing end-date, format 'yyyy-DDD'. DDD = Day of year.");
        endOption.setRequired(true);
        options.addOption(endOption);

        return options;
    }

    // package access for testing only se 2016-11-28
    static PostProcessingContext initialize(CommandLine commandLine) throws IOException {
        logger.info("Loading configuration ...");
        final PostProcessingContext context = new PostProcessingContext();

        final String configValue = commandLine.getOptionValue("config", "./config");
        final Path configDirectory = Paths.get(configValue);

        final SystemConfig systemConfig = SystemConfig.loadFrom(configDirectory.toFile());
        context.setSystemConfig(systemConfig);

        final String jobConfigPathString = commandLine.getOptionValue("job-config");
        final Path jobConfigPath = Paths.get(jobConfigPathString);
        final InputStream inputStream = Files.newInputStream(configDirectory.resolve(jobConfigPath));
        final PostProcessingConfig jobConfig = PostProcessingConfig.load(inputStream);
        context.setProcessingConfig(jobConfig);

        final String startDate = getDate(commandLine, "start");
        context.setStartDate(TimeUtils.parseDOYBeginOfDay(startDate));

        final String endDate = getDate(commandLine, "end");
        context.setEndDate(TimeUtils.parseDOYEndOfDay(endDate));

        final String mmdFilesDir = commandLine.getOptionValue("input-dir");
        context.setMmdInputDirectory(Paths.get(mmdFilesDir));

        logger.info("Success loading configuration.");
        return context;
    }

    static boolean isFileInTimeRange(long startTime, long endTime, String filename) {
        final int dotIdx = filename.lastIndexOf(".");
        final int endIdx = filename.lastIndexOf("_", dotIdx);
        final int startIdx = filename.lastIndexOf("_", endIdx - 1);

        final String endDOY = filename.substring(endIdx + 1, dotIdx);
        final String startDOY = filename.substring(startIdx + 1, endIdx);

        final long fileStart = TimeUtils.parseDOYBeginOfDay(startDOY).getTime();
        final long fileEnd = TimeUtils.parseDOYEndOfDay(endDOY).getTime();

        return fileStart >= startTime && fileEnd <= endTime;
    }

}
