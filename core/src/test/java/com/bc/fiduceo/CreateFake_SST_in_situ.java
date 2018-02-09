package com.bc.fiduceo;

import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.write.Nc4Chunking;
import ucar.nc2.write.Nc4ChunkingDefault;

import java.io.File;
import java.io.IOException;

public class CreateFake_SST_in_situ {

    public static void main(String[] args) throws IOException, InvalidRangeException {
        final File targetFile = new File(args[0]);

        final File targetDirectory = targetFile.getParentFile();
        if (!targetDirectory.isDirectory()) {
            targetDirectory.mkdirs();
        }

        targetFile.createNewFile();

        final Nc4Chunking chunking = Nc4ChunkingDefault.factory(Nc4Chunking.Strategy.standard, 5, true);

        try (NetcdfFileWriter fileWriter = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, targetFile.getAbsolutePath(), chunking)) {
            fileWriter.setRedefineMode(true);

            fileWriter.addDimension(null, "record", 5);
            fileWriter.addGroupAttribute(null, new Attribute("dataset", "gtmba-sst"));
            
            final Variable latVariable = fileWriter.addVariable(null,  "insitu.lat", DataType.FLOAT, "record");
            fileWriter.addVariableAttribute(latVariable, new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999.0f));

            final Variable lonVariable = fileWriter.addVariable(null, "insitu.lon", DataType.FLOAT, "record");
            fileWriter.addVariableAttribute(lonVariable, new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999.0f));

            final Variable timeVariable = fileWriter.addVariable(null, "insitu.time", DataType.INT, "record");
            fileWriter.addVariableAttribute(timeVariable, new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999));

            final Variable mohcIdVariable = fileWriter.addVariable(null, "insitu.mohc_id", DataType.INT, "record");
            fileWriter.addVariableAttribute(mohcIdVariable, new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999));

            final Variable profIdVariable = fileWriter.addVariable(null, "insitu.prof_id", DataType.INT, "record");
            fileWriter.addVariableAttribute(profIdVariable, new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, -9999));

            fileWriter.create();

            fileWriter.write(latVariable, Array.factory(new float[]{-35.72345f, -28.251474f, -12.8269415f, 1.069617f, 19.703432f}));
            fileWriter.write(lonVariable, Array.factory(new float[]{64.48727f, 61.328587f, 56.472466f, 52.079575f, 46.351933f}));
            fileWriter.write(timeVariable, Array.factory(new int[]{1120211850, 1120211973, 1120212228, 1120212456, 1120212766}));
            fileWriter.write(mohcIdVariable, Array.factory(new int[]{10, 11, 12, 13, 14}));
            fileWriter.write(profIdVariable, Array.factory(new int[]{15, 16, 17, 18, 19}));

        }
    }
}
