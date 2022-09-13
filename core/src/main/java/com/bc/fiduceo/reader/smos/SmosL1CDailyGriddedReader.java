package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.snap.SNAP_PixelLocator;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.esa.snap.core.datamodel.CrsGeoCoding;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SmosL1CDailyGriddedReader implements Reader {
    public SmosL1CDailyGriddedReader(ReaderContext readerContext) {

    }

    @Override
    public void open(File file) throws IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void close() throws IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public String getRegEx() {
        return "SM_RE07_MIR_CDF3T[AD]_(\\d{8}T\\d{6}_){2}\\d{3}_\\d{3}_\\d{1}.tgz";
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Dimension getProductSize() throws IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public String getLongitudeVariableName() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public String getLatitudeVariableName() {
        throw new IllegalStateException("not implemented");
    }
}
