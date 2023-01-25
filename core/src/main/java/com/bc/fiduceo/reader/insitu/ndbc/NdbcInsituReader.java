package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.esa.snap.core.datamodel.GeoPos;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static com.bc.fiduceo.reader.insitu.ndbc.MeasurementType.CONSTANT_WIND;

class NdbcInsituReader implements Reader {

    private static final String REG_EX_CW = "\\w{5}c\\d{4}.txt";
    private static final String REG_EX_SM = "\\w{5}h\\d{4}.txt";

    private final HashMap<String, GeoPos> buoyLocations;
    private final MeasurementType type;

    public NdbcInsituReader(MeasurementType type) {
        this.type = type;
        this.buoyLocations = new HashMap<>();
    }

    @Override
    public void open(File file) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void close() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getRegEx() {
        if (type == CONSTANT_WIND) {
            return REG_EX_CW;
        } else {
            return REG_EX_SM;
        }
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
}
