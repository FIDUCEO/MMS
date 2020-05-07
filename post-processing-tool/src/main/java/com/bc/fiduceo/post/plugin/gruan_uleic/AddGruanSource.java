package com.bc.fiduceo.post.plugin.gruan_uleic;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.reader.insitu.gruan_uleic.GruanUleicInsituReader;
import com.bc.fiduceo.util.JDomUtils;
import com.bc.fiduceo.util.NetCDFUtils;
import org.jdom.Element;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

class AddGruanSource extends PostProcessing {

    private final Configuration configuration;

    AddGruanSource(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) {
        final ArrayList<Dimension> targetDimensions = extractTargetDimensions(reader);

        writer.addVariable(null, configuration.targetVariableName, DataType.CHAR, targetDimensions);
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable fileNameVariable = NetCDFUtils.getVariable(reader, configuration.filenameVariableName);
        final Variable processingVersionVariable = NetCDFUtils.getVariable(reader, configuration.processingVersionVariableName);
        final Variable yVariable = NetCDFUtils.getVariable(reader, configuration.yCoordinateName);

        final Array yArray = yVariable.read();

        final Variable targetVariable = NetCDFUtils.getVariable(writer, configuration.targetVariableName);

        final int matchup_count = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, reader);
        final int fileNameSize = NetCDFUtils.getDimensionLength(FiduceoConstants.FILE_NAME, reader);
        final int processingVersionSize = NetCDFUtils.getDimensionLength(FiduceoConstants.PROCESSING_VERSION, reader);

        final Array targetArray = Array.factory(DataType.CHAR, new int[]{matchup_count, fileNameSize});

        for (int i = 0; i < matchup_count; i++) {
            final String fileName = NetCDFUtils.readString(fileNameVariable, i, fileNameSize);
            final String processingVersion = NetCDFUtils.readString(processingVersionVariable, i, processingVersionSize);

            final GruanUleicInsituReader insituReader = (GruanUleicInsituReader) readerCache.getReaderFor("gruan-uleic", Paths.get(fileName), processingVersion);
            final int line = yArray.getInt(i);
            final String sourcePath = insituReader.readSourcePath(line);
            final char[][] sourcePathChar = {sourcePath.toCharArray()};
            final Array sourcePathArray = NetCDFUtils.create(sourcePathChar[0]);
            Array.arraycopy(sourcePathArray, 0, targetArray, i * fileNameSize, (int) sourcePathArray.getSize());

        }
        writer.write(targetVariable, targetArray);
    }

    @Override
    protected void initReaderCache() {
        readerCache = createReaderCache(getContext());
    }

    // package access for testing only tb 2019-01-30
    static ArrayList<Dimension> extractTargetDimensions(NetcdfFile reader) {
        final Dimension file_name = reader.findDimension(FiduceoConstants.FILE_NAME);
        if (file_name == null) {
            throw new RuntimeException("Required dimension not found: filename");
        }

        final Dimension matchup_dim = reader.findDimension(FiduceoConstants.MATCHUP_COUNT);
        if (matchup_dim == null) {
            throw new RuntimeException("Required dimension not found: " + FiduceoConstants.MATCHUP_COUNT);
        }

        final ArrayList<Dimension> targetDimensions = new ArrayList<>();
        targetDimensions.add(0, matchup_dim);
        targetDimensions.add(1, file_name);
        return targetDimensions;
    }

    // package access for testing only tb 2019-01-30
    static Configuration parseConfiguration(Element rootElement) {
        final Configuration configuration = new Configuration();

        final Element targetVariableElement = JDomUtils.getMandatoryChild(rootElement, "target-variable");
        configuration.targetVariableName = JDomUtils.getValueFromNameAttributeMandatory(targetVariableElement);

        final Element yVariableElement = JDomUtils.getMandatoryChild(rootElement, "y-variable");
        configuration.yCoordinateName = JDomUtils.getValueFromNameAttributeMandatory(yVariableElement);

        final Element fileVariableElement = JDomUtils.getMandatoryChild(rootElement, "file-name-variable");
        configuration.filenameVariableName = JDomUtils.getValueFromNameAttributeMandatory(fileVariableElement);

        final Element procVerVariableElement = JDomUtils.getMandatoryChild(rootElement, "processing-version-variable");
        configuration.processingVersionVariableName = JDomUtils.getValueFromNameAttributeMandatory(procVerVariableElement);

        return configuration;
    }

    static class Configuration {
        String targetVariableName;
        String yCoordinateName;
        String filenameVariableName;
        String processingVersionVariableName;
    }
}
