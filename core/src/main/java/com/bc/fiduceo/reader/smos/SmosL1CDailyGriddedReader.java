package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.esa.snap.core.util.io.FileUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class SmosL1CDailyGriddedReader extends NetCDFReader {

    private final ReaderContext readerContext;
    private final Rectangle2D.Float boundary;

    private File productDir;
    private PixelLocator pixelLocator;

    SmosL1CDailyGriddedReader(ReaderContext readerContext) {
        this.readerContext = readerContext;
        this.boundary = new Rectangle2D.Float(-180.f, -86.72f, 360.f, 173.44f);
    }

    @Override
    public void open(File file) throws IOException {
        if (ReaderUtils.isCompressed(file)) {
            final String fileName = FileUtils.getFilenameWithoutExtension(file);
            final long millis = System.currentTimeMillis();
            productDir = readerContext.createDirInTempDir(fileName + millis);

            try {
                final File inputFile = extractFromTar(file);
                super.open(inputFile);
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        } else {
            throw new IOException("Unsupported format, this reader accepts only compressed input (tgz)");
        }
    }

    @Override
    public void close() throws IOException {
        super.close();

        pixelLocator = null;
        if (productDir != null) {
            readerContext.deleteTempFile(productDir);
            productDir = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        final Array longitudes = arrayCache.get("lon");
        final Array latitudes = arrayCache.get("lat");

        final double[] geoMinMax = extractMinMax(longitudes, latitudes);

        final GeometryFactory geometryFactory = readerContext.getGeometryFactory();
        final Polygon polygon = createPolygonFromMinMax(geoMinMax, geometryFactory);
        acquisitionInfo.setBoundingGeometry(polygon);

        setSensingTimes(acquisitionInfo);

        final MultiLineString multiLineString = createMultiLineStringFromMinMax(geoMinMax, geometryFactory);
        final TimeAxis timeAxis = new L3TimeAxis(acquisitionInfo.getSensingStart(), acquisitionInfo.getSensingStop(), multiLineString);
        acquisitionInfo.setTimeAxes(new TimeAxis[]{timeAxis});

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return "SM_RE07_MIR_CDF3T[AD]_(\\d{8}T\\d{6}_){2}\\d{3}_\\d{3}_\\d{1}.tgz";
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final Array longitudes = arrayCache.get("lon");
            final Array latitudes = arrayCache.get("lat");

            pixelLocator = new RasterPixelLocator((float[]) longitudes.get1DJavaArray(DataType.FLOAT),
                    (float[]) latitudes.get1DJavaArray(DataType.FLOAT),
                    boundary);
        }

        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        return getPixelLocator();   // no distinction between pixel locators tb 2022-09-29
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final String datePart = fileName.substring(19, 27);
        final int[] ymd = new int[3];
        ymd[0] = Integer.parseInt(datePart.substring(0, 4));
        ymd[1] = Integer.parseInt(datePart.substring(4, 6));
        ymd[2] = Integer.parseInt(datePart.substring(6, 8));
        return ymd;
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
        final Array longitudes = arrayCache.get("lon");
        final Array latitudes = arrayCache.get("lat");

        return new Dimension("size", (int) longitudes.getSize(), (int) latitudes.getSize());
    }

    @Override
    public String getLongitudeVariableName() {
        return "lon";
    }

    @Override
    public String getLatitudeVariableName() {
        return "lat";
    }

    private File extractFromTar(File file) throws IOException {
        TarArchiveInputStream tarIn = null;
        final int oneMb = 1024 * 1024;

        try {
            final BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath()));
            final GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(inputStream);
            tarIn = new TarArchiveInputStream(gzipIn);

            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                if (entry.isFile()) {
                    if (entry.getName().endsWith(".DBL.nc")) {
                        // uncompress and open
                        int count;
                        byte[] data = new byte[oneMb];
                        final File targetFile = new File(productDir, entry.getName());
                        FileOutputStream fos = new FileOutputStream(targetFile, false);
                        try (BufferedOutputStream dest = new BufferedOutputStream(fos, oneMb)) {
                            while ((count = tarIn.read(data, 0, oneMb)) != -1) {
                                dest.write(data, 0, count);
                            }
                        }
                        return targetFile;
                    }
                }
            }
            throw new IOException("No suitable netcdf file found in tar");
        } finally {
            if (tarIn != null) {
                tarIn.close();
            }
        }
    }

    private void setSensingTimes(AcquisitionInfo acquisitionInfo) {
        final String location = netcdfFile.getLocation();
        final String filename = FileUtils.getFilenameFromPath(location);
        final int[] ymd = extractYearMonthDayFromFilename(filename);

        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.set(Calendar.YEAR, ymd[0]);
        utcCalendar.set(Calendar.MONTH, ymd[1] - 1);    // month is zero-based tb 2022-09-15
        utcCalendar.set(Calendar.DAY_OF_MONTH, ymd[2]);

        utcCalendar.set(Calendar.HOUR, 0);
        utcCalendar.set(Calendar.MINUTE, 0);
        utcCalendar.set(Calendar.SECOND, 0);

        acquisitionInfo.setSensingStart(utcCalendar.getTime());

        utcCalendar.set(Calendar.HOUR, 23);
        utcCalendar.set(Calendar.MINUTE, 59);
        utcCalendar.set(Calendar.SECOND, 59);

        acquisitionInfo.setSensingStop(utcCalendar.getTime());
    }

    // package access for testing only tb 2022-09-15
    static Polygon createPolygonFromMinMax(double[] geoMinMax, GeometryFactory geometryFactory) {
        final double lonMin = geoMinMax[0];
        final double lonMax = geoMinMax[1];
        final double latMin = geoMinMax[2];
        final double latMax = geoMinMax[3];

        final Point ll = geometryFactory.createPoint(lonMin, latMin);
        final Point ul = geometryFactory.createPoint(lonMin, latMax);
        final Point ur = geometryFactory.createPoint(lonMax, latMax);
        final Point lr = geometryFactory.createPoint(lonMax, latMin);
        final ArrayList<Point> polygonPoints = new ArrayList<>();
        polygonPoints.add(ll);
        polygonPoints.add(ul);
        polygonPoints.add(ur);
        polygonPoints.add(lr);
        polygonPoints.add(ll);
        return geometryFactory.createPolygon(polygonPoints);
    }

    /**
     * extract minimal and maximal value of geolocation arrays passed in.
     * the resulting array is ordered:
     * [0] lonMin
     * [1] lonMax
     * [2] latMin
     * [3] latMax
     * package access for testing only tb 2022-09-26
     *
     * @param longitudes longitude data
     * @param latitudes  latitude data
     * @return array with the extreme values
     */
    static double[] extractMinMax(Array longitudes, Array latitudes) {
        final double[] minMax = new double[4];

        int size = (int) longitudes.getSize();
        minMax[0] = longitudes.getDouble(0);
        minMax[1] = longitudes.getDouble(size - 1);

        size = (int) latitudes.getSize();
        minMax[2] = latitudes.getDouble(0);
        minMax[3] = latitudes.getDouble(size - 1);

        return minMax;
    }

    // package access for testing only tb 2022-09-26
    static MultiLineString createMultiLineStringFromMinMax(double[] geoMinMax, GeometryFactory geometryFactory) {
        final double lonMin = geoMinMax[0];
        final double lonMax = geoMinMax[1];

        final double latMin = geoMinMax[2];
        final double latMax = geoMinMax[3];

        final List<Point> points = new ArrayList<>();
        points.add(geometryFactory.createPoint(lonMin, 0.0));
        points.add(geometryFactory.createPoint(lonMax, 0.0));
        final LineString we = geometryFactory.createLineString(points);

        points.clear();
        points.add(geometryFactory.createPoint(0.0, latMax));
        points.add(geometryFactory.createPoint(0.0, latMin));

        final LineString ns = geometryFactory.createLineString(points);

        final List<LineString> lines = new ArrayList<>();
        lines.add(we);
        lines.add(ns);

        return geometryFactory.createMultiLineString(lines);
    }
}
