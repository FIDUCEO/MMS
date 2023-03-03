package com.bc.fiduceo.post.plugin.avhrr_fcdr;

import com.bc.fiduceo.archive.Archive;
import com.bc.fiduceo.archive.ArchiveConfig;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.reader.fiduceo_fcdr.AVHRR_FCDR_Reader;
import com.bc.fiduceo.util.JDomUtils;
import com.bc.fiduceo.util.NetCDFUtils;
import org.jdom.Element;
import ucar.ma2.*;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.FiduceoConstants.FILE_NAME;

class AddAvhrrCorrCoeffs extends PostProcessing {

    private static final int SWATH_WIDTH = 409;
    private static final int MAX_LINE_CORRELATION = 256;
    private static final int NUM_CHANNELS = 6;

    private static final String INPUT_FILES_DIM_NAME = "input_files";
    private static final String SWATH_WIDTH_DIM_NAME = "swath_width";
    private static final String LINE_CORR_DIM_NAME = "line_correlation";
    private static final String CHANNEL_DIM_NAME = "channels";

    private final Configuration configuration;

    AddAvhrrCorrCoeffs(Configuration configuration) {
        this.configuration = configuration;
    }

    // package access for testing only tb 2019-06-14
    static Configuration createConfiguration(Element rootElement) {
        final Configuration config = new Configuration();

        config.fileNameVariableName = getNameAttributeFromChild(rootElement, "file-name-variable");
        config.versionVariableName = getNameAttributeFromChild(rootElement, "processing-version-variable");
        config.targetXElemName = getNameAttributeFromChild(rootElement, "target-x-elem-variable");
        config.targetXLineName = getNameAttributeFromChild(rootElement, "target-x-line-variable");

        return config;
    }

    static String extractSensorKey(String fileName) {
        final int index = fileName.indexOf("AVHRR");
        if (index <= 0) {
            throw new RuntimeException("Unable to extract sensor key from file name");
        }

        final String platformPart = fileName.substring(index + 6, index + 9);
        if (platformPart.contains("MTA") || platformPart.contains("MET")) {
            return "avhrr-ma-fcdr";
        }

        final String integerPart = platformPart.substring(1);
        final int platformIndex = Integer.parseInt(integerPart);

        return "avhrr-n" + platformIndex + "-fcdr";
    }

    private static String getNameAttributeFromChild(Element rootElement, String elementName) {
        final Element element = JDomUtils.getMandatoryChild(rootElement, elementName);
        return JDomUtils.getValueFromNameAttributeMandatory(element);
    }

    @Override
    protected void initReaderCache() {
        readerCache = createReaderCache(getContext());
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final int filenameSize = NetCDFUtils.getDimensionLength(FILE_NAME, reader);
        final ArrayList<FileDescription> fileList = extractUniqueInputFileNames(reader, filenameSize);

        final Dimension inpFilesDimension = writer.addDimension(null, INPUT_FILES_DIM_NAME, fileList.size());
        final Dimension swathWidthDimension = writer.addDimension(null, SWATH_WIDTH_DIM_NAME, SWATH_WIDTH);
        final Dimension lineCorrelationDimension = writer.addDimension(null, LINE_CORR_DIM_NAME, MAX_LINE_CORRELATION);
        final Dimension channelDimension = writer.addDimension(null, CHANNEL_DIM_NAME, NUM_CHANNELS);

        List<Dimension> dimensions = new ArrayList<>(3);
        dimensions.add(inpFilesDimension);
        dimensions.add(swathWidthDimension);
        dimensions.add(channelDimension);
        writer.addVariable(null, configuration.targetXElemName, DataType.FLOAT, dimensions);

        dimensions.clear();
        dimensions.add(inpFilesDimension);
        dimensions.add(lineCorrelationDimension);
        dimensions.add(channelDimension);
        writer.addVariable(null, configuration.targetXLineName, DataType.FLOAT, dimensions);

        dimensions.clear();
        dimensions.add(inpFilesDimension);
        dimensions.add(reader.findDimension(FILE_NAME));
        writer.addVariable(null, INPUT_FILES_DIM_NAME, DataType.CHAR, dimensions);
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final int filenameSize = NetCDFUtils.getDimensionLength("file_name", reader);
        final ArrayList<FileDescription> fileList = extractUniqueInputFileNames(reader, filenameSize);

        final Variable inpFilesVariable = writer.findVariable(INPUT_FILES_DIM_NAME);

        int[] shape = inpFilesVariable.getShape();
        final ArrayChar fileNamesArray = new ArrayChar.D2(shape[0], shape[1]);
        final Index index = fileNamesArray.getIndex();
        for (int i = 0; i < fileList.size(); i++) {
            index.set(i);
            final FileDescription fileDescription = fileList.get(i);
            fileNamesArray.setString(index, fileDescription.fileName);
        }
        writer.write(inpFilesVariable, fileNamesArray);

        final SystemConfig systemConfig = getContext().getSystemConfig();
        final ArchiveConfig archiveConfig = systemConfig.getArchiveConfig();
        final Archive archive = new Archive(archiveConfig);

        final int[] origin = new int[3];
        final int[] targetShape = new int[3];
        targetShape[0] = 1;
        for (int i = 0; i < fileList.size(); i++) {
            final FileDescription fileDescription = fileList.get(i);
            final String sensorKey = extractSensorKey(fileDescription.fileName);

            final AVHRR_FCDR_Reader fcdrReader = (AVHRR_FCDR_Reader) readerCache.getReaderFor(sensorKey, Paths.get(fileDescription.fileName), fileDescription.processingVersion);
            final int[] ymd = fcdrReader.extractYearMonthDayFromFilename(fileDescription.fileName);
            final Path productsDir = archive.createValidProductPath(fileDescription.processingVersion, sensorKey, ymd[0], ymd[1], ymd[2]);
            final File mmdFile = new File(productsDir.toFile(), fileDescription.fileName);

            final Variable targetXElemVariable = NetCDFUtils.getVariable(writer, configuration.targetXElemName);
            final Variable targetXLineVariable = NetCDFUtils.getVariable(writer, configuration.targetXLineName);

            try (NetcdfFile inputAvhrrFile = NetcdfFile.open(mmdFile.getAbsolutePath())) {
                origin[0] = i;

                final Variable crossElemVariable = NetCDFUtils.getVariable(inputAvhrrFile, "cross_element_correlation_coefficients");
                shape = crossElemVariable.getShape();
                targetShape[1] = shape[0];
                targetShape[2] = shape[1];

                final Array crossElemArray = crossElemVariable.read();
                Array reshaped = crossElemArray.reshape(targetShape);
                writer.write(targetXElemVariable, origin, reshaped);

                final Variable crossLineVariable = NetCDFUtils.getVariable(inputAvhrrFile, "cross_line_correlation_coefficients");
                shape = crossLineVariable.getShape();

                targetShape[1] = shape[0];
                targetShape[2] = shape[1];

                final Array crossLineArray = crossLineVariable.read();
                reshaped = crossLineArray.reshape(targetShape);
                writer.write(targetXLineVariable, origin, reshaped);
            }
        }
    }

    private ArrayList<FileDescription> extractUniqueInputFileNames(NetcdfFile reader, int filenameSize) throws IOException, InvalidRangeException {
        final String matchupDimensionName = getMatchupDimensionName();
        final int matchupCount = NetCDFUtils.getDimensionLength(matchupDimensionName, reader);
        final Variable filenameVariable = NetCDFUtils.getVariable(reader, configuration.fileNameVariableName);
        final Variable versionVariable = NetCDFUtils.getVariable(reader, configuration.versionVariableName);
        final ArrayList<FileDescription> fileList = new ArrayList<>();
        final ArrayList<String> nameList = new ArrayList<>();

        for (int i = 0; i < matchupCount; i++) {
            final String filename = NetCDFUtils.readString(filenameVariable, i, filenameSize);
            final String version = NetCDFUtils.readString(versionVariable, i, 30);
            if (!nameList.contains(filename)) {
                final FileDescription description = new FileDescription();
                description.fileName = filename;
                description.processingVersion = version;
                fileList.add(description);
                nameList.add(filename);
            }
        }
        return fileList;
    }

    static class Configuration {
        String fileNameVariableName;
        String versionVariableName;
        String targetXElemName;
        String targetXLineName;
    }

    static class FileDescription {
        String fileName;
        String processingVersion;
    }
}
