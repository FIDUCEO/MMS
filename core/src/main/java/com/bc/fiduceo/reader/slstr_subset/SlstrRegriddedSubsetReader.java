package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.slstr.VariableType;
import com.bc.fiduceo.reader.slstr.utility.Transform;
import com.bc.fiduceo.reader.slstr.utility.TransformFactory;
import com.bc.fiduceo.reader.snap.SNAP_PixelLocator;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_MicrosSince2000;
import com.bc.fiduceo.store.FileSystemStore;
import com.bc.fiduceo.store.Store;
import com.bc.fiduceo.store.ZipStore;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.s3tbx.dataio.s3.Manifest;
import org.esa.s3tbx.dataio.s3.XfduManifest;
import org.esa.snap.core.dataio.geocoding.*;
import org.esa.snap.core.dataio.geocoding.forward.PixelForward;
import org.esa.snap.core.dataio.geocoding.inverse.PixelQuadTreeInverse;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ucar.ma2.DataType;
import ucar.ma2.*;
import ucar.nc2.Variable;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import static com.bc.fiduceo.reader.slstr.utility.ManifestUtil.getObliqueGridOffset;
import static ucar.ma2.DataType.INT;

public class SlstrRegriddedSubsetReader implements Reader {

    private final ReaderContext readerContext;
    private final String LONGITUDE_VAR_NAME = "longitude_in";
    private final String LATITUDE_VAR_NAME = "latitude_in";
    private String manifestXml;
    private PixelLocator pixelLocator;
    private TimeLocator_MicrosSince2000 timeLocator;
    private long[] timeStamps2000;
    private TransformFactory transformFactory;
    private NcCache ncCache;
    private RasterInfo rasterInfo;
    private Manifest manifest;

    public SlstrRegriddedSubsetReader(ReaderContext readerContext) {
        this.readerContext = readerContext;
    }

    @Override
    public void open(File file) throws IOException {
        final Store store;
        if (isZipFile(file)) {
            store = new ZipStore(file.toPath());
        } else {
            if (!file.isDirectory()) {
                file = file.getParentFile();
            }
            store = new FileSystemStore(file.toPath());
        }
        ncCache = new NcCache();

        try {
            final TreeSet<String> keyManifest = store.getKeysEndingWith("xfdumanifest.xml");
            manifestXml = new String(store.getBytes(keyManifest.first()));

            final InputSource is = new InputSource(new StringReader(manifestXml));
            final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            manifest = XfduManifest.createManifest(document);

            rasterInfo = getRasterInfo(manifest);
            ncCache.open(store, rasterInfo);

            final MetadataElement metadataRoot = new MetadataElement("root");
            metadataRoot.addElement(manifest.getMetadata());
            final int obliqueGridOffset = getObliqueGridOffset(metadataRoot);
            final Dimension productSize = getProductSize();

            transformFactory = new TransformFactory(productSize.getNx() * 2,
                    productSize.getNy() * 2,
                    obliqueGridOffset);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        if (ncCache != null) {
            ncCache.close();
            ncCache = null;
        }
        transformFactory = null;
        rasterInfo = null;
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo info = new AcquisitionInfo();
        info.setNodeType(findNodeType());
        info.setSensingStart(manifest.getStartTime().getAsDate());
        info.setSensingStop(manifest.getStopTime().getAsDate());
        extractGeometries(info);
        return info;
    }

    private void extractGeometries(AcquisitionInfo acquisitionInfo) throws IOException {
        final GeometryFactory geometryFactory = readerContext.getGeometryFactory();
        final BoundingPolygonCreator boundingPolygonCreator = new BoundingPolygonCreator(new Interval(250, 250), geometryFactory);

        final Variable lonVariable = ncCache.getVariable(LONGITUDE_VAR_NAME);
        final Array longitude = NetCDFUtils.readAndScaleIfNecessary(lonVariable);
        final Variable latVariable = ncCache.getVariable(LATITUDE_VAR_NAME);
        final Array latitude = NetCDFUtils.readAndScaleIfNecessary(latVariable);

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

    private NodeType findNodeType() {
        final NodeType nodeType;
        if (manifestXml.contains("groundTrackDirection")) {
            final Pattern pattern = Pattern.compile("groundTrackDirection *= *['\"]ascending['\"]");
            final Matcher matcher = pattern.matcher(manifestXml);
            if (matcher.find()) {
                nodeType = NodeType.ASCENDING;
            } else {
                nodeType = NodeType.DESCENDING;
            }
        } else {
            nodeType = NodeType.UNDEFINED;
        }
        return nodeType;
    }

    @Override
    public String getRegEx() {
        return "S3[AB]_SL_1_RBT____(\\d{8}T\\d{6}_){3}\\d{4}(_\\d{3}){2}_\\d{4}_LN2_O_NT_\\d{3}(.SEN3|.zip)";
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final Variable lonVariable = ncCache.getVariable(LONGITUDE_VAR_NAME);
            final Array longitude = NetCDFUtils.readAndScaleIfNecessary(lonVariable);
            final Variable latVariable = ncCache.getVariable(LATITUDE_VAR_NAME);
            final Array latitude = NetCDFUtils.readAndScaleIfNecessary(latVariable);
            final int[] shape = latitude.getShape();

            final GeoRaster geoRaster = new GeoRaster((double[]) longitude.get1DJavaArray(DataType.DOUBLE),
                    (double[]) latitude.get1DJavaArray(DataType.DOUBLE),
                    LONGITUDE_VAR_NAME, LATITUDE_VAR_NAME,
                    shape[1], shape[0], shape[1], shape[0],
                    1.0, 0.5, 0.5, 1.0, 1.0);

            final ForwardCoding forward = ComponentFactory.getForward(PixelForward.KEY);
            final InverseCoding inverse = ComponentFactory.getInverse(PixelQuadTreeInverse.KEY);
            final ComponentGeoCoding componentGeoCoding = new ComponentGeoCoding(geoRaster, forward, inverse, GeoChecks.ANTIMERIDIAN);
            componentGeoCoding.initialize();

            pixelLocator = new SNAP_PixelLocator(componentGeoCoding);
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        return getPixelLocator();
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        if (timeLocator == null) {
            ensureTimeStamps();
            timeLocator = new TimeLocator_MicrosSince2000(timeStamps2000);
        }
        return timeLocator;
    }

    private void ensureTimeStamps() throws IOException {
        if (timeStamps2000 == null) {
            final Variable timeStampVariable = ncCache.getVariable("time_stamp_i");

            final Array array = timeStampVariable.read();
            timeStamps2000 = (long[]) array.get1DJavaArray(DataType.LONG);

            if (timeStamps2000 == null) {
                throw new IOException("Unable to read time stamp data");
            }
        }
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final String datePart = fileName.substring(16, 24);
        final int[] ymd = new int[3];
        ymd[0] = Integer.parseInt(datePart.substring(0, 4));
        ymd[1] = Integer.parseInt(datePart.substring(4, 6));
        ymd[2] = Integer.parseInt(datePart.substring(6, 8));
        return ymd;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Variable variable = ncCache.getVariable(variableName);
        final Array rawArray = ncCache.getRawArray(variableName);
        final Transform transform = getTransform(variableName);
        final Number fillValue = NetCDFUtils.getFillValue(variable);

        final int mappedX = (int) transform.mapCoordinate_X(centerX);
        final int mappedY = (int) transform.mapCoordinate_Y(centerY);

        return RawDataReader.read(mappedX, mappedY, interval, fillValue, rawArray, getProductSize());
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Variable variable = ncCache.getVariable(variableName);
        final Array scaledArray = ncCache.getScaledArray(variableName);
        final Transform transform = getTransform(variableName);
        final Number fillValue = NetCDFUtils.getFillValue(variable);

        final int mappedX = (int) transform.mapCoordinate_X(centerX);
        final int mappedY = (int) transform.mapCoordinate_Y(centerY);

        return RawDataReader.read(mappedX, mappedY, interval, fillValue, scaledArray, getProductSize());
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        ensureTimeStamps();
        final int targetWidth = interval.getX();
        final int targetHeight = interval.getY();
        final Dimension productSize = getProductSize();
        final int prodWidth = productSize.getNx();

        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();

        final int startX = x - targetWidth / 2;
        final int startY = y - targetHeight / 2;

        final int[] targetShape = {targetHeight, targetWidth};
        final int[] store = new int[targetHeight * targetWidth];
        Arrays.fill(store, fillValue);
        final ArrayInt.D2 target = (ArrayInt.D2) Array.factory(INT, targetShape, store);
        final Index index = target.getIndex();
        for (int yIdx = 0; yIdx < targetHeight; yIdx++) {
            final int srcY = startY + yIdx;
            if (srcY < 0 || srcY >= timeStamps2000.length) {
                continue;
            }
            final long lineTimeMillis = TimeUtils.millisSince2000ToUnixEpoch(timeStamps2000[srcY]);
            int lineTimeSeconds = (int) Math.round(lineTimeMillis * 0.001);
            index.set0(yIdx);
            for (int xIdx = 0; xIdx < targetWidth; xIdx++) {
                final int srcX = startX + xIdx;
                if (srcX < 0 || srcX >= prodWidth) {
                    continue;
                }
                index.set1(xIdx);
                target.setInt(index, lineTimeSeconds);
            }
        }

        return target;
    }

    @Override
    public List<Variable> getVariables() throws IOException {
        final List<Variable> variables = new ArrayList<>();
        final List<String> variableNames = ncCache.getVariableNames();
        for (final String varName : variableNames) {
            variables.add(ncCache.getVariable(varName));
        }
        return variables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        if (rasterInfo != null) {
            return new Dimension("shape", rasterInfo.rasterWidth, rasterInfo.rasterHeight);
        }
        throw new IOException("Product not opened.");
    }

    @Override
    public String getLongitudeVariableName() {
        return LONGITUDE_VAR_NAME;
    }

    @Override
    public String getLatitudeVariableName() {
        return LATITUDE_VAR_NAME;
    }

    private boolean isZipFile(File file) {
        try (ZipFile ignored = new ZipFile(file)) {
            // Try with resource block to automatically close the ZipFile if it does not throw an exception
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // package instead of private for testing purposes
    static String extractName(String key) {
        if (key.contains("\\")) {
            return key.substring(key.lastIndexOf("\\") + 1);
        } else {
            return key.substring(key.lastIndexOf("/") + 1);
        }
    }

    static RasterInfo getRasterInfo(Manifest manifest) {
        final RasterInfo rasterInfo = new RasterInfo();

        final MetadataElement metadata = manifest.getMetadata();
        final MetadataElement metadataSection = metadata.getElement("metadataSection");
        final MetadataElement slstrProductInformation = metadataSection.getElement("slstrProductInformation");
        MetadataElement[] elements = slstrProductInformation.getElements();
        for (final MetadataElement element : elements) {
            if (element.getName().equals("resolution")) {
                final MetadataAttribute grid = element.getAttribute("grid");
                final String gridValue = grid.getData().getElemString();
                if (gridValue.equals("1 km")) {
                    rasterInfo.rasterResolution = getRasterAttributeInt(element, "spatialResolution");
                } else if (gridValue.equals("Tie Points")) {
                    rasterInfo.tiePointResolution = getRasterAttributeInt(element, "spatialResolution");
                }
            }
            if (element.getName().equals("nadirImageSize")) {
                final MetadataAttribute grid = element.getAttribute("grid");
                final String gridValue = grid.getData().getElemString();
                if (gridValue.equals("1 km")) {
                    rasterInfo.rasterTrackOffset = getRasterAttributeInt(element, "trackOffset");
                    rasterInfo.rasterHeight = getRasterAttributeInt(element, "rows");
                    rasterInfo.rasterWidth = getRasterAttributeInt(element, "columns");
                } else if (gridValue.equals("Tie Points")) {
                    rasterInfo.tiePointTrackOffset = getRasterAttributeInt(element, "trackOffset");
                }
            }
        }
        return rasterInfo;
    }

    private Transform getTransform(String variableName) {
        Transform transform;
        if (variableName.endsWith("_io")) {
            transform = transformFactory.get(VariableType.OBLIQUE_1km);
        } else {
            transform = transformFactory.get(VariableType.NADIR_1km);
        }
        return transform;
    }

    private static int getRasterAttributeInt(MetadataElement element, String attributeName) {
        final MetadataAttribute spatialResolution = element.getAttribute(attributeName);
        final String resolutionString = spatialResolution.getData().getElemString();
        return Integer.parseInt(resolutionString);
    }
}
