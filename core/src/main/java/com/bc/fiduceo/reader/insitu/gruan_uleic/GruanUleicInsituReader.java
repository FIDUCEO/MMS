package com.bc.fiduceo.reader.insitu.gruan_uleic;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class GruanUleicInsituReader implements Reader {

    private static final String REG_EX = "[a-z]{3}_matchup_points.txt";

    private FileReader fileReader;
    private ArrayList<String> linelist;

    @Override
    public void open(File file) throws IOException {
        fileReader = new FileReader(file);

        linelist = new ArrayList<>();
        final BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            linelist.add(line);
        }
    }

    @Override
    public void close() throws IOException {
        linelist.clear();
        linelist = null;
        if (fileReader != null) {
            fileReader.close();
            fileReader = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        final Line startLine = decodeLine(linelist.get(0));
        acquisitionInfo.setSensingStart(startLine.date);

        final Line endLine = decodeLine(linelist.get(linelist.size() - 1));
        acquisitionInfo.setSensingStop(endLine.date);

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        throw new RuntimeException("notImplemented");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new RuntimeException("notImplemented");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        throw new RuntimeException("notImplemented");
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        throw new RuntimeException("notImplemented");
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("notImplemented");
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("notImplemented");
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new RuntimeException("notImplemented");
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        throw new RuntimeException("notImplemented");
    }

    @Override
    public Dimension getProductSize() throws IOException {
        throw new RuntimeException("notImplemented");
    }

    @Override
    public String getLongitudeVariableName() {
        return "lon";
    }

    @Override
    public String getLatitudeVariableName() {
        return "lat";
    }

    static Line decodeLine(String lineString) {
        final String[] tokens = lineString.split(",");
        final Line line = new Line();

        final long millisSinceEpoch = (long) (Float.parseFloat(tokens[0].trim()) * 1000);
        line.date = TimeUtils.create(millisSinceEpoch);

        line.lon = Float.parseFloat(tokens[1].trim());
        line.lat = Float.parseFloat(tokens[2].trim());

        line.path = tokens[3].trim();

        return line;
    }

    static class Line {
        Date date;
        float lon;
        float lat;
        String path;
    }
}
