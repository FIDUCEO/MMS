package com.bc.fiduceo.reader.windsat;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;

class WindsatReader implements Reader {

    private static final String REG_EX = "RSS_WindSat_TB_L1C_r\\d{5}_\\d{8}T\\d{6}_\\d{7}_V\\d{2}.\\d.nc";

    private NetcdfFile netcdfFile;

    WindsatReader(ReaderContext readerContext) {
    }

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFiles.open(file.getPath());
    }

    @Override
    public void close() throws IOException {
        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        throw new RuntimeException("not implmented");
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        throw new RuntimeException("not implmented");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new RuntimeException("not implmented");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        throw new RuntimeException("not implmented");
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final int year = Integer.parseInt(fileName.substring(26, 30));
        final int month = Integer.parseInt(fileName.substring(30, 32));
        final int day = Integer.parseInt(fileName.substring(32, 34));

        return new int[] {year, month, day};
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implmented");
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implmented");
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implmented");
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        throw new RuntimeException("not implmented");
    }

    @Override
    public Dimension getProductSize() throws IOException {
        throw new RuntimeException("not implmented");
    }

    @Override
    public String getLongitudeVariableName() {
        return "longitude";
    }

    @Override
    public String getLatitudeVariableName() {
        return "latitude";
    }
}
