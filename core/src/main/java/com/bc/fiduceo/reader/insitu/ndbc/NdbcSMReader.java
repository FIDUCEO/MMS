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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class NdbcSMReader extends NdbcReader {

    private static final String REG_EX_SM = "\\w{5}h\\d{4}.txt";

    private static StationDatabase stationDatabase;


    NdbcSMReader() {
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
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getRegEx() {
        return REG_EX_SM;
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
        final ArrayList<Variable> variables = new ArrayList<>();

        return variables;
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
            stationDatabase = parseStationDatabase("buoy_locations_sm.txt");
        }
    }

    SmRecord parseLine(String line, Calendar calendar) {
        final SmRecord record = new SmRecord();

        line = line.replaceAll(" +", " "); // some fields are separated by two or more blanks (sigh) tb 2023-02-27
        final String[] tokens = StringUtils.split(line, new char[]{' '}, true);

        calendar.setTimeInMillis(0);
        calendar.set(Calendar.YEAR, Integer.parseInt(tokens[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(tokens[1]) - 1);  // calendar wants month zero-based tb 2023-02-27
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(tokens[2]));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(tokens[3]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(tokens[4]));
        record.utc = (int) (calendar.getTimeInMillis() * 0.001);

        record.windDir = Short.parseShort(tokens[5]);
        record.windSpeed = Float.parseFloat(tokens[6]);
        record.gustSpeed = Float.parseFloat(tokens[7]);
        record.waveHeight = Float.parseFloat(tokens[8]);
        record.domWavePeriod = Float.parseFloat(tokens[9]);
        record.avgWavePeriod = Float.parseFloat(tokens[10]);
        record.waveDir = Short.parseShort(tokens[11]);
        record.seaLevelPressure = Float.parseFloat(tokens[12]);
        record.airTemp = Float.parseFloat(tokens[13]);
        record.seaSurfTemp = Float.parseFloat(tokens[14]);
        record.dewPointTemp = Float.parseFloat(tokens[15]);
        record.visibility = Float.parseFloat(tokens[16]);
        record.tideLevel = Float.parseFloat(tokens[17]);

        return record;
    }
}
