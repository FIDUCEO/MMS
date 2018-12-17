package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.GeometryUtil;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ArrayCache;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.fiduceo.reader.Geometries;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class AVHRR_FCDR_Reader implements Reader {

    private static final int NUM_SPLITS = 2;

    private final GeometryFactory geometryFactory;

    private NetcdfFile netcdfFile;
    private File file;
    private BoundingPolygonCreator boundingPolygonCreator;
    private ArrayCache arrayCache;

    AVHRR_FCDR_Reader(ReaderContext readerContext) {
        this.geometryFactory = readerContext.getGeometryFactory();
    }

    @Override
    public void open(File file) throws IOException {
        this.file = file;
        netcdfFile = NetcdfFile.open(file.getPath());
        arrayCache = new ArrayCache(netcdfFile);
    }

    @Override
    public void close() throws IOException {
        file = null;
        boundingPolygonCreator = null;
        arrayCache = null;
        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        final String fileName = file.getName();
        acquisitionInfo.setSensingStart(parseStartDate(fileName));
        acquisitionInfo.setSensingStop(parseStopDate(fileName));

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        final Geometries geometries = calculateGeometries();
        final Geometry boundingGeometry = geometries.getBoundingGeometry();
        acquisitionInfo.setBoundingGeometry(boundingGeometry);
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return "FIDUCEO_FCDR_L1C_AVHRR_(METOPA|NOAA[0-9]{2})_[0-9]{14}_[0-9]{14}_EASY_vBeta_fv\\d\\.\\d\\.\\d\\.nc";
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

    static Date parseStartDate(String fileName) {
        return parseDateFromName(fileName, 30);
    }

    static Date parseStopDate(String fileName) {
        return parseDateFromName(fileName, 45);
    }

    private static Date parseDateFromName(String fileName, int offset) {
        final int endIdx = offset + 14;
        final String dateString = fileName.substring(offset, endIdx);

        return TimeUtils.parse(dateString, "yyyyMMddHHmmss");
    }

    private Geometries calculateGeometries() throws IOException {
        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator();
        final Geometries geometries = new Geometries();

        final Array longitudes = arrayCache.getScaled("longitude", "scale_factor", "add_offset");
        final Array latitudes = arrayCache.getScaled("latitude", "scale_factor", "add_offset");
        Geometry timeAxisGeometry;
        Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(longitudes, latitudes);
        if (!boundingGeometry.isValid()) {
            boundingGeometry = boundingPolygonCreator.createBoundingGeometrySplitted(longitudes, latitudes, NUM_SPLITS, false);
            if (!boundingGeometry.isValid()) {
                throw new RuntimeException("Invalid bounding geometry detected");
            }
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometrySplitted(longitudes, latitudes, NUM_SPLITS);
        } else {
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitudes, latitudes);
        }

        geometries.setBoundingGeometry(boundingGeometry);
        geometries.setTimeAxesGeometry(timeAxisGeometry);
        return geometries;
    }

    private BoundingPolygonCreator getBoundingPolygonCreator() {
        if (boundingPolygonCreator == null) {
            // @todo 2 tb/tb move intervals to config 2018-12-17
            boundingPolygonCreator = new BoundingPolygonCreator(new Interval(40, 100), geometryFactory);
        }

        return boundingPolygonCreator;
    }
}
