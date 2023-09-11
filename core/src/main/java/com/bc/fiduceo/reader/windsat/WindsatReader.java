package com.bc.fiduceo.reader.windsat;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.RawDataReader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_SecondsSince2000;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import com.bc.fiduceo.util.VariableProxy;
import org.esa.snap.dataio.netcdf.PartialDataCopier;
import ucar.ma2.*;
import ucar.nc2.AttributeContainer;
import ucar.nc2.Variable;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class WindsatReader extends NetCDFReader {

    private static final String REG_EX = "RSS_WindSat_TB_L1C_r\\d{5}_\\d{8}T\\d{6}_\\d{7}_V\\d{2}.\\d.nc";
    private static final String[] LOOKS = {"fore", "aft"};
    private static final String[] FREQ_BANDS = {"068", "107", "187", "238", "370"};
    private static final String[] POLARIZATIONS = {"V", "H", "P", "M", "L", "R"};
    private final List<String> variables2D;

    private final GeometryFactory geometryFactory;

    private PixelLocator pixelLocator;
    private TimeLocator timeLocator;

    private Dimension productSize;
    private int productWidth;
    private int productHeight;

    WindsatReader(ReaderContext readerContext) {
        this.geometryFactory = readerContext.getGeometryFactory();
        variables2D = new ArrayList<>();
        variables2D.add("latitude");
        variables2D.add("longitude");
        variables2D.add("land_fraction_06");
        variables2D.add("land_fraction_10");
    }

    static ArrayInfo extractArrayInfo(String mmsVariableName) {
        final ArrayInfo arrayInfo = new ArrayInfo();

        final int viewIdx = mmsVariableName.lastIndexOf("_");
        final String view = mmsVariableName.substring(viewIdx + 1);
        arrayInfo.lookIdx = getIndex(view, LOOKS);

        final int extIdx = mmsVariableName.lastIndexOf("_", viewIdx - 1);
        final String ext = mmsVariableName.substring(extIdx + 1, viewIdx);
        arrayInfo.polIdx = getIndex(ext, POLARIZATIONS);
        arrayInfo.freqIdx = getIndex(ext, FREQ_BANDS);

        arrayInfo.ncVarName = mmsVariableName.substring(0, extIdx);

        return arrayInfo;
    }

    static int getIndex(String value, String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            if (strings[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void open(File file) throws IOException {
        super.open(file);
        initProductSize();
    }

    @Override
    public void close() throws IOException {
        super.close();
        pixelLocator = null;
        timeLocator = null;
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo info = new AcquisitionInfo();

        final double[] geoMinMax = extractGeoMinMax();
        final Polygon polygon = GeometryUtil.createPolygonFromMinMax(geoMinMax, geometryFactory);
        info.setBoundingGeometry(polygon);

        final String startTimeString = NetCDFUtils.getGlobalAttributeString("time_coverage_start", netcdfFile);
        info.setSensingStart(TimeUtils.parse(startTimeString, "yyyy-MM-dd'T'HH:mm:ss'Z'"));

        final String endTimeString = NetCDFUtils.getGlobalAttributeString("time_coverage_end", netcdfFile);
        info.setSensingStop(TimeUtils.parse(endTimeString, "yyyy-MM-dd'T'HH:mm:ss'Z'"));

        final MultiLineString multiLineString = GeometryUtil.createMultiLineStringFromMinMax(geoMinMax, geometryFactory);
        final TimeAxis timeAxis = new L3TimeAxis(info.getSensingStart(), info.getSensingStop(), multiLineString);
        info.setTimeAxes(new TimeAxis[]{timeAxis});

        info.setNodeType(NodeType.UNDEFINED);

        return info;
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final Array lonSection = readGeoLocationVector("longitude");
            final Array latSection = readGeoLocationVector("latitude");

            final float[] longitudes = (float[]) lonSection.get1DJavaArray(DataType.FLOAT);
            final float[] latitudes = (float[]) latSection.get1DJavaArray(DataType.FLOAT);

            pixelLocator = new OverlappingRasterPixelLocator(longitudes, latitudes);
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
            final Variable variable = netcdfFile.findVariable("time");
            final Number fillValue = NetCDFUtils.getFillValue(variable);
            final int[] origin = {0, 0, 0, 2};  // select layer for "fore" and 18 GHz tb 2022-11-28
            final int[] shape = variable.getShape();
            shape[2] = 1;   // one view layer tb 2022-11-28
            shape[3] = 1;   // one frequency layer tb 2022-11-28

            try {
                final Array timeArraySection = variable.read(origin, shape).reduce();
                timeLocator = new TimeLocator_SecondsSince2000(timeArraySection, fillValue.doubleValue());
            } catch (InvalidRangeException e) {
                throw new IOException(e.getMessage());
            }
        }
        return timeLocator;
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final int year = Integer.parseInt(fileName.substring(26, 30));
        final int month = Integer.parseInt(fileName.substring(30, 32));
        final int day = Integer.parseInt(fileName.substring(32, 34));

        return new int[]{year, month, day};
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        if (variableName.equals("fractional_orbit")) {
            // read x section and fill in y-direction
            final Array array = arrayCache.get(variableName);
            final int[] shape = array.getShape();
            final double fillValue = NetCDFUtils.getDefaultFillValue(DataType.DOUBLE, true).doubleValue();

            final int sectionWidth = interval.getX();
            final int sectionHeight = interval.getY();
            final Array resultArray = Array.factory(DataType.DOUBLE, new int[]{sectionWidth, sectionHeight});
            final Index index = resultArray.getIndex();

            final int offsetX = centerX - sectionWidth / 2;

            int writeX = 0;
            for (int x = offsetX; x < offsetX + sectionWidth; x++) {
                final double value;
                if (x >= 0 && x < shape[0]) {
                    value = array.getDouble(x);
                } else {
                    value = fillValue;
                }

                for (int y = 0; y < sectionHeight; y++) {
                    index.set(y, writeX);
                    resultArray.setDouble(index, value);
                }
                writeX++;
            }

            return resultArray;
        }
        final int winWith = interval.getX();
        final int winHeight = interval.getY();
        final int offsetX = centerX - winWith / 2;
        final int offsetY = centerY - winHeight / 2;
        final boolean isWindowInside = RawDataReader.isWindowInside(offsetX, offsetY, winWith, winHeight, productWidth, productHeight);

        if (variables2D.contains(variableName)) {
            final Variable variable = netcdfFile.findVariable(variableName);
            if (isWindowInside) {
                return variable.read(new int[]{offsetY, offsetX}, new int[]{winHeight, winWith});
            } else {
                final Rectangle2D insideWindow = RawDataReader.getInsideWindow(offsetX, offsetY, winWith, winHeight, productWidth, productHeight);
                final int[] origin = {(int) insideWindow.getY(), (int) insideWindow.getX()};
                final int[] shape = {(int) insideWindow.getHeight(), (int) insideWindow.getWidth()};
                final Array insideRead = variable.read(origin, shape);
                final Number fillValue = NetCDFUtils.getFillValue(variable);
                final Array array = NetCDFUtils.create(insideRead.getDataType(), new int[]{winHeight, winWith}, fillValue);
                final int targetOffsY = Math.min(offsetY, 0);
                final int targetOffsX = Math.min(offsetX, 0);
                PartialDataCopier.copy(new int[]{targetOffsY, targetOffsX}, insideRead, array);
                return array;
            }
        }

        // extract layer indices and NetCDF file variable name from variable name
        final ArrayInfo arrayInfo = extractArrayInfo(variableName);
        final Variable variable = netcdfFile.findVariable(arrayInfo.ncVarName);
        final int[] shape = variable.getShape();
        final int[] origin = new int[4];
        final int xDimPos;
        final int yDimPos;
        final int lookPos;
        final int freqPolPos;
        if (arrayInfo.freqIdx >= 0) {
            yDimPos = 0;
            xDimPos = 1;
            lookPos = 2;
            freqPolPos = 3;
        } else {
            freqPolPos = 0;
            lookPos = 1;
            yDimPos = 2;
            xDimPos = 3;
        }
        if (arrayInfo.freqIdx >= 0) {
            origin[lookPos] = arrayInfo.lookIdx;
            origin[freqPolPos] = arrayInfo.freqIdx;
            shape[lookPos] = 1;
            shape[freqPolPos] = 1;
        } else {
            origin[freqPolPos] = arrayInfo.polIdx;
            origin[lookPos] = arrayInfo.lookIdx;
            shape[freqPolPos] = 1;
            shape[lookPos] = 1;
        }
        if (isWindowInside) {
            origin[xDimPos] = offsetX;
            origin[yDimPos] = offsetY;
            shape[xDimPos] = winWith;
            shape[yDimPos] = winHeight;
            return variable.read(origin, shape).reduce();
        } else {
            final Rectangle2D insideWindow = RawDataReader.getInsideWindow(offsetX, offsetY, winWith, winHeight, productWidth, productHeight);
            origin[xDimPos] = (int) insideWindow.getX();
            origin[yDimPos] = (int) insideWindow.getY();
            shape[xDimPos] = (int) insideWindow.getWidth();
            shape[yDimPos] = (int) insideWindow.getHeight();
            final Array insideRead = variable.read(origin, shape).reduce();
            final Number fillValue = NetCDFUtils.getFillValue(variable);
            final Array array = NetCDFUtils.create(insideRead.getDataType(), new int[]{winHeight, winWith}, fillValue);
            final int targetOffsY = Math.min(offsetY, 0);
            final int targetOffsX = Math.min(offsetX, 0);
            PartialDataCopier.copy(new int[]{targetOffsY, targetOffsX}, insideRead, array);
            return array;
        }
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Array rawArray = readRaw(centerX, centerY, interval, variableName);
        if (!variableName.startsWith("tb_")) {
            // everything except for the brightness temperatures is already scaled tb 2023-01-05
            return rawArray;
        }

        final ArrayInfo arrayInfo = extractArrayInfo(variableName);
        final Variable variable = netcdfFile.findVariable(arrayInfo.ncVarName);
        final AttributeContainer attributes = variable.attributes();
        final double scaleFactor = attributes.findAttributeDouble("scale_factor", 1.0);
        final double offset = attributes.findAttributeDouble("add_offset", 0.0);
        final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, offset);
        return MAMath.convert2Unpacked(rawArray, scaleOffset);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        final Dimension productSize = getProductSize();
        final TimeLocator timeLocator = getTimeLocator();
        return ReaderUtils.readAcquisitionTimeFromTimeLocator(x, y, interval, productSize, timeLocator);
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        final List<Variable> variablesInFile = netcdfFile.getVariables();
        final ArrayList<Variable> exportVariables = new ArrayList<>();

        for (final Variable variable : variablesInFile) {
            final int rank = variable.getRank();
            if (rank == 1 || rank == 2) {
                exportVariables.add(variable);
            } else {
                final String varName = variable.getShortName();
                if (varName.startsWith("tb_")) {
                    addBrightnessTemperatureVariables(exportVariables, variable, varName);
                } else {
                    add4DViewVariables(exportVariables, variable, varName);
                }
            }
        }
        return exportVariables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        return productSize;
    }

    public void initProductSize() {
        final int[] shape = netcdfFile.findVariable("longitude").getShape();
        productWidth = shape[1];
        productHeight = shape[0];
        productSize = new Dimension("size", productWidth, productHeight);
    }

    @Override
    public String getLongitudeVariableName() {
        return "longitude";
    }

    @Override
    public String getLatitudeVariableName() {
        return "latitude";
    }

    private double[] extractGeoMinMax() throws IOException {
        final Array lonSection = readGeoLocationVector("longitude");
        final Array latSection = readGeoLocationVector("latitude");

        final double[] geoMinMax = new double[4];
        geoMinMax[0] = Double.MAX_VALUE;
        geoMinMax[1] = -Double.MAX_VALUE;
        for (int i = 0; i < lonSection.getSize(); i++) {
            double lon = lonSection.getDouble(i);
            if (lon > 180.f) {
                lon -= 360.f;
            }
            if (lon < geoMinMax[0]) {
                geoMinMax[0] = lon;
            }
            if (lon > geoMinMax[1]) {
                geoMinMax[1] = lon;
            }
        }

        geoMinMax[2] = Float.MAX_VALUE;
        geoMinMax[3] = -Float.MAX_VALUE;
        for (int i = 0; i < latSection.getSize(); i++) {
            final double lat = latSection.getDouble(i);
            if (lat < geoMinMax[2]) {
                geoMinMax[2] = lat;
            }
            if (lat > geoMinMax[3]) {
                geoMinMax[3] = lat;
            }
        }
        return geoMinMax;
    }

    private Array readGeoLocationVector(String variableName) throws IOException {
        final Variable lonVar = netcdfFile.findVariable(variableName);
        if (lonVar == null) {
            throw new IOException("Required variable ' " + variableName + "' not found");
        }
        final int[] shape = lonVar.getShape();
        final int[] readShape;
        if (variableName.equals("longitude")) {
            readShape = new int[]{1, shape[1]};
        } else {
            readShape = new int[]{shape[0], 1};
        }

        try {
            return lonVar.read(new int[]{0, 0}, readShape);
        } catch (InvalidRangeException e) {
            throw new IOException(e);
        }
    }

    private void addBrightnessTemperatureVariables(ArrayList<Variable> exportVariables, Variable variable, String varName) {
        // polarization_x look ydim_grid xdim_grid
        final int[] shape = variable.getShape();
        for (int look = 0; look < shape[1]; look++) {
            for (int pol = 0; pol < shape[0]; pol++) {
                final String layerVarName = varName + "_" + POLARIZATIONS[pol] + "_" + LOOKS[look];
                exportVariables.add(new VariableProxy(layerVarName, variable.getDataType(), variable.attributes().getAttributes()));
            }
        }
    }

    private void add4DViewVariables(ArrayList<Variable> exportVariables, Variable variable, String varName) {
        // ydim_grid xdim_grid look frequency_band
        final int[] shape = variable.getShape();
        for (int look = 0; look < shape[2]; look++) {
            for (int freq = 0; freq < shape[3]; freq++) {
                final String layerVarName = varName + "_" + FREQ_BANDS[freq] + "_" + LOOKS[look];
                exportVariables.add(new VariableProxy(layerVarName, variable.getDataType(), variable.attributes().getAttributes()));
            }
        }
    }
}
