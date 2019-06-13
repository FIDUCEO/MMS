package com.bc.fiduceo.post.plugin.avhrr_fcdr;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;

import static com.bc.fiduceo.FiduceoConstants.MATCHUP_COUNT;

class AddAvhrrCorrCoeffs extends PostProcessing {

    private static final String FILE_NAME_VARIABLE_NAME = "avhrr-ma-fcdr_file_name";

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final int matchupCount = NetCDFUtils.getDimensionLength(MATCHUP_COUNT, reader);
        final int filenameSize = NetCDFUtils.getDimensionLength("file_name", reader);
        final Variable filenameVariable = reader.findVariable(FILE_NAME_VARIABLE_NAME);
        final ArrayList<String> nameList = new ArrayList<>();

        for (int i = 0; i< matchupCount; i++) {
            final String filename = NetCDFUtils.readString(filenameVariable, i, filenameSize);
            if (!nameList.contains(filename)) {
                nameList.add(filename);
            }
        }

        System.out.println(filenameSize);
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {

    }
}
