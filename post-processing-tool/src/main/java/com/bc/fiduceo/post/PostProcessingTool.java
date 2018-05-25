/*
 * Copyright (C) 2018 Brockmann Consult GmbH
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

import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TempFileUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.esa.snap.core.util.StringUtils;
import org.jdom.Element;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.*;
import ucar.nc2.constants.DataFormatType;
import ucar.nc2.write.Nc4Chunking;
import ucar.nc2.write.Nc4ChunkingDefault;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bc.fiduceo.FiduceoConstants.VERSION_NUMBER;

class PostProcessingTool {

    private final static Logger logger = FiduceoLogger.getLogger();
    private final static String SUPPRESSED_ATTRIBUTE = "_NCProperties";
    private final PostProcessingContext context;

    PostProcessingTool(PostProcessingContext context) {
        this.context = context;
    }

    static PostProcessingContext initializeContext(CommandLine commandLine) throws IOException {
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

        final String tempDir = systemConfig.getTempDir();
        if (StringUtils.isNullOrEmpty(tempDir)) {
            context.setTempFileUtils(new TempFileUtils());
        } else {
            context.setTempFileUtils(new TempFileUtils(tempDir));
        }

        final String geometryLibraryType = systemConfig.getGeometryLibraryType();
        final ReaderFactory readerFactory = ReaderFactory.create(new GeometryFactory(geometryLibraryType), context.getTempFileUtils());
        context.setReaderFactory(readerFactory);

        logger.info("Success loading configuration.");
        return context;
    }

    void runPostProcessing() throws Exception {
        final Path inputDirectory = context.getMmdInputDirectory();
        final Pattern pattern = Pattern.compile("mmd\\d{1,2}.*_.*_.*_\\d{4}-\\d{3}_\\d{4}-\\d{3}.nc");

        try (Stream<Path> pathStream = Files.walk(inputDirectory)) {
            final Stream<Path> regularFiles = pathStream.filter(path -> Files.isRegularFile(path));
            final Stream<Path> mmdFileStream = regularFiles.filter(path -> pattern.matcher(path.getFileName().toString()).matches());
            List<Path> mmdFiles = mmdFileStream.collect(Collectors.toList());

            computeFiles(mmdFiles);
        } finally {
            context.getTempFileUtils().cleanup();
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

    static boolean isFileInTimeRange(long startTime, long endTime, String filename) {
        final int dotIdx = filename.lastIndexOf(".");
        final int endIdx = filename.lastIndexOf("_", dotIdx);
        final int startIdx = filename.lastIndexOf("_", endIdx - 1);

        final String startDOY = filename.substring(startIdx + 1, endIdx);

        final long fileStart = TimeUtils.parseDOYBeginOfDay(startDOY).getTime();

        return fileStart >= startTime && fileStart <= endTime;
    }

    private void computeFiles(List<Path> mmdFiles) throws Exception {
        final PostProcessingConfig processingConfig = context.getProcessingConfig();

        final List<PostProcessing> processings = new ArrayList<>();
        final PostProcessingFactory factory = PostProcessingFactory.get();
        for (Element processing : processingConfig.getPostProcessingElements()) {
            final PostProcessing postProcessing = factory.getPostProcessing(processing);
            postProcessing.setContext(context);
            processings.add(postProcessing);
        }

        try {
            final SourceTargetManager manager = new SourceTargetManager(processingConfig);
            for (Path mmdFile : mmdFiles) {
                Exception ex = null;
                try {
                    computeFile(mmdFile, manager, processings);
                } catch (Exception e) {
                    ex = e;
                    logger.severe("Unable to execute post processing for matchup '" + mmdFile.getFileName().toString() + "'");
                    logger.severe("Cause: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    manager.processingDone(mmdFile, ex);
                }

                if (ex != null) {
                    throw ex;  // do not hide exceptions, we need this one to propagate to the main-method tb 2017-04-24
                }
            }
        } finally {
            disposePostProcessings(processings);
        }
    }

    private void disposePostProcessings(List<PostProcessing> processings) {
        for (final PostProcessing postProcessing : processings) {
            postProcessing.dispose();
        }
    }

    private void computeFile(Path mmdFile, final SourceTargetManager manager, List<PostProcessing> processings) throws IOException, InvalidRangeException {
        final long startTime = context.getStartDate().getTime();
        final long endTime = context.getEndDate().getTime();
        if (isFileInTimeRange(startTime, endTime, mmdFile.getFileName().toString())) {
            logger.info("Compute file '" + mmdFile.getFileName().toString() + "'");
            final Path source = manager.getSource(mmdFile);
            final Path target = manager.getTargetPath(mmdFile);

            NetcdfFile reader = null;
            NetcdfFileWriter writer = null;

            try {
                final String absSource = source.toAbsolutePath().toString();

                // open the file that way is needed because the standard open mechanism changes the file size
                reader = NetCDFUtils.openReadOnly(absSource);

                writer = createWriter(target, reader);

                run(reader, writer, processings);
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    // when writer is in define mode, the file has not been created. Closing it in this state causes a
                    // null pointer exception tb 2016-12-21
                    if (!writer.isDefineMode()) {
                        writer.close();
                    }
                }
            }
        }
    }

    // package access for testing only se 2016-11-28
    void run(NetcdfFile reader, NetcdfFileWriter writer, List<PostProcessing> postProcessings) throws IOException, InvalidRangeException {
        final Group rootGroup = reader.getRootGroup();

        final List<String> variableNamesToRemove = getVariableRemoveNamesList(postProcessings);

        copyHeader(writer, rootGroup, variableNamesToRemove, null, 0);
        addPostProcessingConfig(writer);

        for (PostProcessing postProcessing : postProcessings) {
            postProcessing.prepare(reader, writer);
        }
        writer.create();

        transferData(writer, rootGroup);
        for (PostProcessing postProcessing : postProcessings) {
            postProcessing.compute(reader, writer);
        }
    }

    // package access for testing only tb 2017-06-02
    void addPostProcessingConfig(NetcdfFileWriter writer) throws IOException {
        final String attName = "post-processing-configuration";

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Attribute attribute = writer.findGlobalAttribute(attName);
        if (attribute != null) {
            outputStream.write(attribute.getStringValue().getBytes());
            outputStream.write("\n".getBytes());
            writer.deleteGroupAttribute(null, attName);
        }

        context.getProcessingConfig().store(outputStream);
        outputStream.close();

        writer.addGroupAttribute(null, new Attribute(attName, outputStream.toString()));
    }

    // package access for testing only tb 2017-06-02
    static List<String> getVariableRemoveNamesList(List<PostProcessing> postProcessings) {
        final List<String> variableNamesToRemove = new ArrayList<>();
        for (PostProcessing postProcessing : postProcessings) {
            final List<String> pluginVariableNamesToRemove = postProcessing.getVariableNamesToRemove();
            for (final String name : pluginVariableNamesToRemove) {
                if (!variableNamesToRemove.contains(name)) {
                    variableNamesToRemove.add(name);
                }
            }
        }
        return variableNamesToRemove;
    }

    private static NetcdfFileWriter createWriter(Path target, NetcdfFile reader) throws IOException {
        NetcdfFileWriter writer;
        final String absTarget = target.toAbsolutePath().toString();
        if (DataFormatType.NETCDF.name().equalsIgnoreCase(reader.getFileTypeId())) {
            writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, absTarget);
        } else {
            final Nc4Chunking chunking = Nc4ChunkingDefault.factory(Nc4Chunking.Strategy.standard, 5, true);
            writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, absTarget, chunking);
        }

        return writer;
    }

    private static void copyHeader(NetcdfFileWriter writer, Group oldGroup, List<String> namesToRemove, Group newParent, int anonymousDimensionIndex) {
        Group newGroup = writer.addGroup(newParent, oldGroup.getShortName());

        for (Attribute att : oldGroup.getAttributes()) {
            if (att.getFullName().contains(SUPPRESSED_ATTRIBUTE)) {
                continue;
            }
            newGroup.addAttribute(att);
        }

        for (Dimension dim : oldGroup.getDimensions()) {
            writer.addDimension(newGroup, dim.getShortName(), dim.getLength(), true, dim.isUnlimited(), dim.isVariableLength());
        }

        for (Variable v : oldGroup.getVariables()) {
            final String shortName = v.getShortName();
            if (namesToRemove.contains(shortName)) {
                continue;
            }

            List<Dimension> dims = v.getDimensions();
            // all dimensions must be shared (!)
            for (Dimension dim : dims) {
                if (!dim.isShared()) {
                    dim.setName("anonymous" + anonymousDimensionIndex);
                    dim.setShared(true);
                    anonymousDimensionIndex++;
                    writer.addDimension(newGroup, dim.getShortName(), dim.getLength(), true, dim.isUnlimited(), dim.isVariableLength());
                }
            }

            final Variable nv = writer.addVariable(newGroup, shortName, v.getDataType(), v.getDimensionsString());
            for (Attribute att : v.getAttributes()) {
                writer.addVariableAttribute(nv, att);
            }
        }

        // recurse
        for (Group g : oldGroup.getGroups()) {
            copyHeader(writer, g, namesToRemove, newGroup, anonymousDimensionIndex);
        }
    }

    private static void transferData(NetcdfFileWriter writer, Group oldGroup) throws IOException, InvalidRangeException {
        for (Variable v : oldGroup.getVariables()) {

            logger.info(String.format("write %s", v.getNameAndDimensions()));
            Variable nv = writer.findVariable(v.getFullName());
            if (nv != null) {
                writer.write(nv, v.read());
            }
        }

        // recurse
        for (Group g : oldGroup.getGroups()) {
            transferData(writer, g);
        }
    }

}
