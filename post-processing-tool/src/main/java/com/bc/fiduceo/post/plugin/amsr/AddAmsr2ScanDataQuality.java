package com.bc.fiduceo.post.plugin.amsr;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.reader.amsr.amsr2.AMSR2_Reader;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class AddAmsr2ScanDataQuality extends PostProcessing {

    private Configuration configuration;

    void configure(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) {
        final String matchupDimensionName = getMatchupDimensionName();
        final Dimension matchupDimension = reader.findDimension(matchupDimensionName);

        final Dimension scanDataQualityDimension = writer.addDimension(null, "scan_data_quality", 512);
        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(matchupDimension);
        dimensions.add(scanDataQualityDimension);
        final Variable variable = writer.addVariable(null, configuration.targetVariableName, DataType.BYTE, dimensions);
        variable.addAttribute(new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class).byteValue()));
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable fileNameVariable = NetCDFUtils.getVariable(reader, configuration.filenameVariableName);
        final Variable processingVersionVariable = NetCDFUtils.getVariable(reader, configuration.processingVersionVariableName);

        final Variable yVariable = NetCDFUtils.getVariable(reader, configuration.yCoordinateVariableName);
        final Array yArray = yVariable.read();

        final String matchupDimensionName = getMatchupDimensionName();
        final int matchup_count = NetCDFUtils.getDimensionLength(matchupDimensionName, reader);
        final int fileNameSize = NetCDFUtils.getDimensionLength(FiduceoConstants.FILE_NAME, reader);
        final int processingVersionSize = NetCDFUtils.getDimensionLength(FiduceoConstants.PROCESSING_VERSION, reader);
        final Variable targetVariable = NetCDFUtils.getVariable(writer, configuration.targetVariableName);

        final ArrayByte.D2 writeArray = new ArrayByte.D2(1, 512, false);
        final int[] origin = new int[]{0, 0};
        for (int i = 0; i < matchup_count; i++) {
            final String fileName = NetCDFUtils.readString(fileNameVariable, i, fileNameSize);
            final String processingVersion = NetCDFUtils.readString(processingVersionVariable, i, processingVersionSize);

            final AMSR2_Reader amsr2Reader = (AMSR2_Reader) readerCache.getReaderFor("amsr2-gcw1", Paths.get(fileName), processingVersion);

            final Array scan_data_quality = amsr2Reader.readScanDataQuality(yArray.getInt(i));
            for (int k = 0; k < 512; k++) {
                writeArray.set(0, k, scan_data_quality.getByte(k));
            }

            origin[0] = i;
            writer.write(targetVariable, origin, writeArray);
        }
    }

    @Override
    protected void initReaderCache() {
        readerCache = createReaderCache(getContext());
    }

    static class Configuration {
        String filenameVariableName;
        String processingVersionVariableName;
        String yCoordinateVariableName;
        String targetVariableName;
    }
}
