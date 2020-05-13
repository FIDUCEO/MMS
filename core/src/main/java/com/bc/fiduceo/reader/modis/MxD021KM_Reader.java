package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.hdf.HdfEOSUtil;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.reader.snap.SNAP_Reader;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.jdom2.Element;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.bc.fiduceo.reader.modis.ModisConstants.LATITUDE_VAR_NAME;
import static com.bc.fiduceo.reader.modis.ModisConstants.LONGITUDE_VAR_NAME;

class MxD021KM_Reader extends NetCDFReader {

    private static final String REG_EX = "M([OY])D021KM.A\\d{7}.\\d{4}.\\d{3}.\\d{13}.hdf";
    private static final String GEOLOCATION_GROUP = "MODIS_SWATH_Type_L1B/Geolocation_Fields";

    private final GeometryFactory geometryFactory;

    MxD021KM_Reader(ReaderContext readerContext) {
        geometryFactory = readerContext.getGeometryFactory();
    }

    @Override
    public void open(File file) throws IOException {
        super.open(file);
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        HdfEOSUtil.extractAcquisitionTimes(acquisitionInfo, netcdfFile);

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);
        extractGeometries(acquisitionInfo);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        return null;
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return null;
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        return null;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        return null;
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
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public String getLongitudeVariableName() {
        return LONGITUDE_VAR_NAME;
    }

    @Override
    public String getLatitudeVariableName() {
        return LATITUDE_VAR_NAME;
    }

    private void extractGeometries(AcquisitionInfo acquisitionInfo) throws IOException {
        final BoundingPolygonCreator boundingPolygonCreator = new BoundingPolygonCreator(new Interval(50, 50), geometryFactory);
        final Array longitude = arrayCache.get(GEOLOCATION_GROUP, LONGITUDE_VAR_NAME);
        final Array latitude = arrayCache.get(GEOLOCATION_GROUP, LATITUDE_VAR_NAME);
        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(longitude, latitude);
        if (!boundingGeometry.isValid()) {
            throw new RuntimeException("Detected invalid bounding geometry");
        }
        acquisitionInfo.setBoundingGeometry(boundingGeometry);

        final Geometries geometries = new Geometries();
        geometries.setBoundingGeometry(boundingGeometry);
        final LineString timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitude, latitude);
        geometries.setTimeAxesGeometry(timeAxisGeometry);
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);
    }
}
