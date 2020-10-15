package com.bc.fiduceo.reader.insitu.gruan_uleic;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.SecsSince1970TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import com.bc.fiduceo.util.VariableProxy;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;

public class GruanUleicInsituReader implements Reader {

    private static final String REG_EX = "[a-z]{3}_matchup_points.txt";

    private final HashMap<String, LineDecoder> decoder;

    private FileReader fileReader;
    private ArrayList<String> linelist;
    private List<Variable> variableList;

    GruanUleicInsituReader() {
        decoder = new HashMap<>();
        decoder.put("time", new LineDecoder.Time());
        decoder.put("lon", new LineDecoder.Lon());
        decoder.put("lat", new LineDecoder.Lat());
        decoder.put("source_file_path", new LineDecoder.Source());
    }

    static Line decodeLine(String lineString) {
        final String[] tokens = lineString.split(",");
        final Line line = new Line();

        final long millisSinceEpoch = (long) (Double.parseDouble(tokens[0].trim()) * 1000);
        line.date = TimeUtils.create(millisSinceEpoch);

        line.lon = Float.parseFloat(tokens[1].trim());
        line.lat = Float.parseFloat(tokens[2].trim());

        line.path = tokens[3].trim();

        return line;
    }

    @Override
    public void open(File file) throws IOException {
        fileReader = new FileReader(file);

        linelist = new ArrayList<>();
        final BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            linelist.add(line);
        }

        variableList = null;
    }

    @Override
    public void close() throws IOException {
        variableList = null;
        if (linelist != null) {
            linelist.clear();
            linelist = null;
        }
        if (fileReader != null) {
            fileReader.close();
            fileReader = null;
        }
    }

    @Override
    public AcquisitionInfo read() {
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
    public PixelLocator getPixelLocator() {
        throw new RuntimeException("notImplemented");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) {
        throw new RuntimeException("notImplemented");
    }

    @Override
    public TimeLocator getTimeLocator() {
        return new SecsSince1970TimeLocator(this);
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        return new int[3];
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) {
        return readScaled(centerX, centerY, interval, variableName);
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) {
        final Variable variable = findVariable(variableName);
        final Number fillValue = NetCDFUtils.getFillValue(variable);
        final LineDecoder lineDecoder = decoder.get(variableName);

        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();
        final int windowCenterX = windowWidth / 2;
        final int windowCenterY = windowHeight / 2;

        final int[] shape = {windowWidth, windowHeight};
        final Array windowArray = Array.factory(variable.getDataType(), shape);
        for (int y = 0; y < windowHeight; y++) {
            for (int x = 0; x < windowWidth; x++) {
                windowArray.setObject(windowWidth * y + x, fillValue);
            }
        }

        final String lineRaw = linelist.get(centerY);
        final Object lineValue = lineDecoder.get(lineRaw);
        windowArray.setObject(windowWidth * windowCenterY + windowCenterX, lineValue);

        return windowArray;
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) {
        return (ArrayInt.D2) readScaled(x, y, interval, "time");
    }

    @Override
    public List<Variable> getVariables() {
        ensureVariablesList();
        return variableList;
    }

    private void ensureVariablesList() {
        if (variableList == null) {
            createVariableList();
        }
    }

    @Override
    public Dimension getProductSize() {
        return new Dimension("product_size", 1, linelist.size());
    }

    @Override
    public String getLongitudeVariableName() {
        return "lon";
    }

    @Override
    public String getLatitudeVariableName() {
        return "lat";
    }

    public String readSourcePath(int y) {
        final String lineString = linelist.get(y);
        final Line line = decodeLine(lineString);
        return line.path;
    }

    private Variable findVariable(String name) {
        ensureVariablesList();
        for (final Variable variable : variableList) {
            if (name.equalsIgnoreCase(variable.getShortName())) {
                return variable;
            }
        }
        throw new RuntimeException("Variable not contained in file: " + name);
    }

    private void createVariableList() {
        variableList = new ArrayList<>();

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_east"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "longitude"));
        variableList.add(new VariableProxy("lon", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_north"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "latitude"));
        variableList.add(new VariableProxy("lat", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "Seconds since 1970-01-01"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(int.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "time"));
        variableList.add(new VariableProxy("time", DataType.INT, attributes));
    }

    static class Line {
        Date date;
        float lon;
        float lat;
        String path;
    }
}
