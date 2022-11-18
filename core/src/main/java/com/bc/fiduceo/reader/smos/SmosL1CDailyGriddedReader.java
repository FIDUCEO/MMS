package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.location.RasterPixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.RawDataReader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import com.bc.fiduceo.util.VariableProxy;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.esa.snap.core.util.io.FileUtils;
import ucar.ma2.*;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.bc.fiduceo.reader.smos.GeolocationHandler.LATITUDE;
import static com.bc.fiduceo.reader.smos.GeolocationHandler.LONGITUDE;
import static com.bc.fiduceo.util.NetCDFUtils.getDefaultFillValue;

class SmosL1CDailyGriddedReader extends NetCDFReader {

    private final ReaderContext readerContext;
    private final Rectangle2D.Float boundary;
    private final List<String> variablesToSkip;
    private final List<String> variables2D;
    private final SmosAngleExtension layerExtension;

    private File productDir;
    private PixelLocator pixelLocator;
    private TimeLocator timeLocator;
    private GeolocationHandler geolocationHandler;


    SmosL1CDailyGriddedReader(ReaderContext readerContext) {
        this.readerContext = readerContext;
        this.boundary = new Rectangle2D.Float(-180.f, -86.72f, 360.f, 173.44f);

        variablesToSkip = new ArrayList<>();
        variablesToSkip.add("lat");
        variablesToSkip.add("lon");
        variablesToSkip.add("inc");
        variablesToSkip.add("dinc");

        variables2D = new ArrayList<>();
        variables2D.add("X_Swath");
        variables2D.add("Grid_Point_Mask");
        layerExtension = new SmosAngleExtension();
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
        timeLocator = null;
        geolocationHandler = null;
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
        final Polygon polygon = GeometryUtil.createPolygonFromMinMax(geoMinMax, geometryFactory);
        acquisitionInfo.setBoundingGeometry(polygon);

        setSensingTimes(acquisitionInfo);

        final MultiLineString multiLineString = GeometryUtil.createMultiLineStringFromMinMax(geoMinMax, geometryFactory);
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
        if (timeLocator == null) {
            final Array days = arrayCache.get("Days");
            final Array seconds = arrayCache.get("UTC_Seconds");
            final Array micros = arrayCache.get("UTC_Microseconds");

            // get layer at index 8 which is the 40deg observation angle bin
            int layerIndex = 8;
            int[] offset = {layerIndex, 0, 0};
            int[] shape = days.getShape();
            shape[0] = 1; // just one z-layer
            try {
                final Array daysLayer = days.section(offset, shape);
                final Array secondsLayer = seconds.section(offset, shape);
                final Array microsLayer = micros.section(offset, shape);

                timeLocator = new SmosL1CTimeLocator(daysLayer, secondsLayer, microsLayer);
            } catch (InvalidRangeException e) {
                throw new IOException(e);
            }
        }
        return timeLocator;
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
        if (variableName.equals("lon") || variableName.equals("lat")) {
            final GeolocationHandler geoHandler = getGeolocationHandler();
            int type;
            if (variableName.equals("lon")) {
                type = LONGITUDE;
            } else {
                type = LATITUDE;
            }
            return geoHandler.read(centerX, centerY, interval, type);
        } else if (variables2D.contains(variableName)) {
            final Array array = arrayCache.get(variableName);
            final Number fillValue = arrayCache.getNumberAttributeValue(NetCDFUtils.CF_FILL_VALUE_NAME, variableName);
            return RawDataReader.read(centerX, centerY, interval, fillValue, array, getProductSize());
        } else {
            final int extensionIdx = variableName.lastIndexOf("_");
            final int layerIndex = layerExtension.getIndex(variableName.substring(extensionIdx));
            final String ncVariableName = variableName.substring(0, extensionIdx);

            final Array array = arrayCache.get(ncVariableName);
            final Number fillValue = arrayCache.getNumberAttributeValue(NetCDFUtils.CF_FILL_VALUE_NAME, ncVariableName);

            final int[] shape = array.getShape();
            shape[0] = 1;   // we only want one z-layer
            final int[] offsets = {layerIndex, 0, 0};

            final Array angleLayer = NetCDFUtils.section(array, offsets, shape);
            return RawDataReader.read(centerX, centerY, interval, fillValue, angleLayer, getProductSize());
        }
    }

    private GeolocationHandler getGeolocationHandler() throws IOException {
        if (geolocationHandler == null) {
            geolocationHandler = new GeolocationHandler(getPixelLocator());
        }

        return geolocationHandler;
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Array rawArray = readRaw(centerX, centerY, interval, variableName);

        // we know that these are already scaled tb 2022-11-01
        if (variableName.equals("lon") || variableName.equals("lat")) {
            return rawArray;
        }

        final String ncVariableName;
        if (variables2D.contains(variableName)) {
            ncVariableName = variableName;
        } else {
            final int extensionIdx = variableName.lastIndexOf("_");
            ncVariableName = variableName.substring(0, extensionIdx);
        }
        double scaleFactor = arrayCache.getNumberAttributeValue("scale_factor", ncVariableName).doubleValue();
        double offset = arrayCache.getNumberAttributeValue("add_offset", ncVariableName).doubleValue();
        if (ReaderUtils.mustScale(scaleFactor, offset)) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, offset);
            return MAMath.convert2Unpacked(rawArray, scaleOffset);
        }

        return rawArray;
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        final int height = interval.getY();
        final int width = interval.getX();
        final int x_offset = x - width / 2;
        final int y_offset = y - height / 2;
        int[] shape = new int[]{height, width};

        final Dimension productSize = getProductSize();
        final int pWidth = productSize.getNx();
        final int pHeight = productSize.getNy();

        final TimeLocator timeLocator = getTimeLocator();
        final int acquisitionTimeFillValue = getDefaultFillValue(int.class).intValue();

        final ArrayInt.D2 acquisitionTime = (ArrayInt.D2) Array.factory(DataType.INT, shape);
        final Index index = acquisitionTime.getIndex();

        for (int ya = 0; ya < height; ya++) {
            final int yRead = y_offset + ya;

            for (int xa = 0; xa < width; xa++) {
                final int xRead = x_offset + xa;

                int acTime;
                if (xRead < 0 || xRead >= pWidth || yRead < 0 || yRead >= pHeight) {
                    acTime = acquisitionTimeFillValue;
                } else {
                    final long pxTime = timeLocator.getTimeFor(xRead, yRead);
                    if (pxTime < 0) {
                        acTime = acquisitionTimeFillValue;
                    } else {
                        acTime = (int) (pxTime / 1000);
                    }
                }
                index.set(ya, xa);
                acquisitionTime.setInt(index, acTime);
            }
        }

        return acquisitionTime;
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        final List<Variable> variablesInFile = netcdfFile.getVariables();
        final ArrayList<Variable> exportVariables = new ArrayList<>();

        for (Variable variable : variablesInFile) {
            final String variableName = variable.getShortName();
            if (variablesToSkip.contains(variableName)) {
                continue;
            }
            if (variables2D.contains(variableName)) {
                exportVariables.add(variable);
                continue;
            }

            // the remainder is 3d with 14 angle layers tb 2022-10-12
            final ArrayList<Variable> bandVariables = new ArrayList<>();
            final int[] origin = {0, 0, 0};
            addChannelVariables(bandVariables, variable, 14, 0, origin, variableName, layerExtension);
            exportVariables.addAll(bandVariables);
        }

        // add two proxies for the geolocation variables tb 2022-11-01
        final ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("_FillValue", Float.NaN));
        attributes.add(new Attribute("units", "degrees_east"));
        attributes.add(new Attribute("long_name", "longitude"));
        exportVariables.add(new VariableProxy("lon", DataType.FLOAT, attributes));

        attributes.clear();
        attributes.add(new Attribute("_FillValue", Float.NaN));
        attributes.add(new Attribute("units", "degrees_north"));
        attributes.add(new Attribute("long_name", "latitude"));
        exportVariables.add(new VariableProxy("lat", DataType.FLOAT, attributes));

        return exportVariables;
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

        utcCalendar.set(Calendar.HOUR_OF_DAY, 0);
        utcCalendar.set(Calendar.MINUTE, 0);
        utcCalendar.set(Calendar.SECOND, 0);
        utcCalendar.set(Calendar.MILLISECOND, 0);

        acquisitionInfo.setSensingStart(utcCalendar.getTime());

        utcCalendar.set(Calendar.HOUR_OF_DAY, 23);
        utcCalendar.set(Calendar.MINUTE, 59);
        utcCalendar.set(Calendar.SECOND, 59);
        utcCalendar.set(Calendar.MILLISECOND, 999);

        acquisitionInfo.setSensingStop(utcCalendar.getTime());
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

    // package access for testing only tb 2022-09-29
    static Date cfiDateToUtc(int days, long seconds, long microseconds) {
        final Calendar calendar = TimeUtils.getUTCCalendar();

        calendar.set(Calendar.YEAR, 2000);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DATE, days);
        calendar.add(Calendar.SECOND, (int) seconds);
        calendar.add(Calendar.MILLISECOND, (int) (microseconds * 0.001));

        return calendar.getTime();
    }
}
