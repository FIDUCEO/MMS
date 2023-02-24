package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

class NdbcCWReader extends NdbcReader {

    private static final String REG_EX_CW = "\\w{5}c\\d{4}.txt";

    private static StationDatabase stationDatabase;


    NdbcCWReader() {
    }

    @Override
    public void open(File file) throws IOException {
        ensureStationDatabase();
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX_CW;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Dimension getProductSize() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getLongitudeVariableName() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getLatitudeVariableName() {
        throw new RuntimeException("not implemented");
    }

    private void ensureStationDatabase() throws IOException {
        if (stationDatabase == null) {
            stationDatabase = parseStationDatabase("buoy_locations_cw.txt");
        }
    }

    public CwRecord parseLine(String line, Calendar calendar) {
        final CwRecord cwRecord = new CwRecord();

        line = line.replace("  ", " "); // some fields are separated by two blanks (sigh) tb 2023-02-24
        final String[] tokens = StringUtils.split(line, new char[]{' '}, true);

        calendar.setTimeInMillis(0);
        calendar.set(Calendar.YEAR, Integer.parseInt(tokens[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(tokens[1]) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(tokens[2]));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(tokens[3]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(tokens[4]));

        cwRecord.utc = (int) (calendar.getTimeInMillis() * 0.001);
        cwRecord.windDir = Short.parseShort(tokens[5]);
        cwRecord.windSpeed = Float.parseFloat(tokens[6]);
        cwRecord.gustDir = Short.parseShort(tokens[7]);
        cwRecord.gustSpeed = Float.parseFloat(tokens[8]);
        cwRecord.gustTime = Short.parseShort(tokens[9]);

        return cwRecord;
    }
}
