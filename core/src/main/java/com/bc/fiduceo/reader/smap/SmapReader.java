package com.bc.fiduceo.reader.smap;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.GeometryUtil;
import com.bc.fiduceo.geometry.L3TimeAxis;
import com.bc.fiduceo.geometry.MultiLineString;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
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
import org.apache.commons.collections.MapUtils;
import org.esa.snap.dataio.netcdf.PartialDataCopier;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.Variable;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class SmapReader extends NetCDFReader {

    private static final String DIM_NAME_X = "xdim_grid";
    private static final String DIM_NAME_Y = "ydim_grid";
    private static final String CF_FillValue = NetCDFUtils.CF_FILL_VALUE_NAME;


    // DATA FORMAT SPECIFICATION
    // at: https://data.remss.com/smap/SSS/V05.0/documents/SMAP_NASA_RSS_Salinity_Release_V5.0.pdf
    // Page 55 / Chapter: 12
    // see chapter 12.1.1
    private static final String REG_EX = "RSS_SMAP_SSS_L2C_r\\d{5}_\\d{8}T\\d{6}_\\d{7}_FNL_V05\\.0\\.nc";

    // see page 56 find: "look = 2 (1 = for look, 2= aft look)"
    // zero based indexes are: 0 = for look, 1= aft look"
    private static final String[] LOOKS = {"for", "aft"};

    // see table at page 44
    private static final String[] UNCERTAINTIES =
            {"ws-ran", "nedt-v", "nedt-h", "sst", "wdir", "ref-gal", "lnd-ctn", "si-ctn", "ws-sys"};

    // see iceflag_components at page 56
    private static final String[] ICEFLAGS = {"ice1", "ice2", "ice3"};

    // see pages 56, 58 and 59
    private static final String[][] POLARIZATIONS = {
            {"V", "H"},  // see polarization_2 at page 58
            {"I", "Q", "S3"},  // see polarization_3 at page 59
            {"V", "H", "S3", "S4"}  // see polarization_4 at pages 58 and 59
    };

    private static final Map<String, String[]> VAR_DIM_EXTENSIONS = MapUtils.putAll(new HashMap(), new Object[]{
            "look", LOOKS,
            "uncertainty_components", UNCERTAINTIES,
            "iceflag_components", ICEFLAGS,
            "polarization_2", POLARIZATIONS[0],
            "polarization_3", POLARIZATIONS[1],
            "polarization_4", POLARIZATIONS[2],
    });

    private final Map<String, VariableInfo> variableInfos = new LinkedHashMap<>();

    private final GeometryFactory geometryFactory;
    final int lookValue;
    private final String lookExtName;

    private PixelLocator pixelLocator;
    private TimeLocator timeLocator;
    private int pWidth;
    private int pHeight;
    private ArrayList<Variable> variables;

    /**
     * Package local constructor of the SMAP data Reader.
     * Should not be instantiated directly but via the plugins
     * {@link SmapReaderPluginForLook#createReader(ReaderContext)} and
     * {@link SmapReaderPluginAftLook#createReader(ReaderContext)}.
     * For more detailed information about the meaning of "for look" and "aft look" please see page 56 in the document:
     * https://data.remss.com/smap/SSS/V05.0/documents/SMAP_NASA_RSS_Salinity_Release_V5.0.pdf
     *
     * @param readerContext
     * @param look
     */
    SmapReader(ReaderContext readerContext, int look) {
        this.geometryFactory = readerContext.getGeometryFactory();

        if (look < 0 || look > 1) {
            throw new RuntimeException("look must be 0 or 1");
        }

        this.lookValue = look;
        this.lookExtName = LOOKS[lookValue];
    }

    @Override
    public void open(File file) throws IOException {
        super.open(file);
        pWidth = netcdfFile.findDimension(DIM_NAME_X).getLength();
        pHeight = netcdfFile.findDimension(DIM_NAME_Y).getLength();
        variables = initVariables();
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
            final Variable cellonVar = netcdfFile.findVariable("cellon");
            final Variable cellatVar = netcdfFile.findVariable("cellat");

            pixelLocator = new SmapPixelLocator(cellonVar, cellatVar, lookValue);
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
            final Array timeArray = arrayCache.get("time");
            final Number fillValue = getFillValue("time", timeArray);
            final int[] origin = {0, 0, lookValue};  // select "look" layer
            final int[] shape = timeArray.getShape();
            shape[2] = 1;   // one "look" layer

            try {
                final Array timeArraySection = timeArray.section(origin, shape).reduce();
                timeLocator = new TimeLocator_SecondsSince2000(timeArraySection, fillValue.doubleValue());
            } catch (InvalidRangeException e) {
                throw new IOException(e.getMessage());
            }
        }
        return timeLocator;
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final int startIndex = 24;
        final int year = Integer.parseInt(fileName.substring(startIndex, startIndex + 4));
        final int month = Integer.parseInt(fileName.substring(startIndex + 4, startIndex + 6));
        final int day = Integer.parseInt(fileName.substring(startIndex + 6, startIndex + 8));

        return new int[]{year, month, day};
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final VariableInfo variableInfo = variableInfos.get(variableName);
        final int[] offset = variableInfo.offset;
        final int[] fullXYShape = new int[offset.length];
        Arrays.fill(fullXYShape, 1);
        fullXYShape[variableInfo.xDimIndex] = pWidth;
        fullXYShape[variableInfo.yDimIndex] = pHeight;

        final Variable variable = netcdfFile.findVariable(variableInfo.ncVarName);

        final int winWith = interval.getX();
        final int winHeight = interval.getY();
        final int offsetX = centerX - winWith / 2;
        final int offsetY = centerY - winHeight / 2;
        final boolean isWindowInside = RawDataReader.isWindowInside(offsetX, offsetY, winWith, winHeight, pWidth, pHeight);
        if (isWindowInside) {
            offset[variableInfo.xDimIndex] = offsetX;
            offset[variableInfo.yDimIndex] = offsetY;
            fullXYShape[variableInfo.xDimIndex] = winWith;
            fullXYShape[variableInfo.yDimIndex] = winHeight;
            return variable.read(offset, fullXYShape).reduce();
        } else {
            final Rectangle2D insideWindow = RawDataReader.getInsideWindow(offsetX, offsetY, winWith, winHeight, pWidth, pHeight);
            offset[variableInfo.xDimIndex] = (int) insideWindow.getX();
            offset[variableInfo.yDimIndex] = (int) insideWindow.getY();
            fullXYShape[variableInfo.xDimIndex] = (int) insideWindow.getWidth();
            fullXYShape[variableInfo.yDimIndex] = (int) insideWindow.getHeight();
            final Array insideRead = variable.read(offset, fullXYShape).reduce();
            final Array a = NetCDFUtils.create(insideRead.getDataType(), new int[]{winHeight, winWith}, variableInfo.fillValue);
            final int targetOffsY = Math.min(offsetY, 0);
            final int targetOffsX = Math.min(offsetX, 0);
            PartialDataCopier.copy(new int[]{targetOffsY, targetOffsX}, insideRead, a);
            return a;
        }
    }

    private Number getFillValue(String variableName, Array array) throws IOException {
        Number fillValue = arrayCache.getNumberAttributeValue(NetCDFUtils.CF_FILL_VALUE_NAME, variableName);
        if (fillValue == null) {
            fillValue = NetCDFUtils.getDefaultFillValue(array);
        }
        return fillValue;
    }

    //    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        // Since SMAP variables apparently have neither an add_offset nor a scale_factor != 1.0, the results are the same as readRaw.
        return readRaw(centerX, centerY, interval, variableName); // everything is already scaled se 2023-04-12
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        final Dimension productSize = getProductSize();
        final TimeLocator timeLocator = getTimeLocator();
        return ReaderUtils.readAcquisitionTimeFromTimeLocator(x, y, interval, productSize, timeLocator);
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        return Collections.unmodifiableList(variables);
    }

    private ArrayList<Variable> initVariables() {
        final List<Variable> variablesInFile = netcdfFile.getVariables();
        final ArrayList<Variable> exportVariables = new ArrayList<>();

        for (final Variable variable : variablesInFile) {
            final int rank = variable.getRank();
            final String ncVarName = variable.getShortName();
            final int xDimIdx = variable.findDimensionIndex(DIM_NAME_X);
            final int yDimIdx = variable.findDimensionIndex(DIM_NAME_Y);
            final Number fillValue = variable.findAttribute(CF_FillValue).getNumericValue();
            if (rank == 2) {
                exportVariables.add(variable);
                variableInfos.put(ncVarName, new VariableInfo(ncVarName, xDimIdx, yDimIdx, new int[rank], fillValue));
            } else {
                final List<ucar.nc2.Dimension> dimensions = variable.getDimensions();
                final List<String[]> dimsToIterate = new ArrayList<>();
                final List<Integer> dimIndex = new ArrayList<>();
                final List<String> dimNamesToIterate = new ArrayList<>();
                for (ucar.nc2.Dimension dimension : dimensions) {
                    final String dimName = dimension.getShortName();
                    if (VAR_DIM_EXTENSIONS.containsKey(dimName)) {
                        if ("look".equals(dimName)) {
                            dimsToIterate.add(new String[]{lookExtName});
                        } else {
                            dimsToIterate.add(VAR_DIM_EXTENSIONS.get(dimName));
                        }
                        dimIndex.add(variable.findDimensionIndex(dimName));
                        dimNamesToIterate.add(dimName);
                    }
                }
                final int[] dimSliceOffset = new int[dimsToIterate.size()];
                final int[] dimSliceOffsetMax = new int[dimsToIterate.size()];
                for (int i = 0; i < dimSliceOffsetMax.length; i++) {
                    final String dimName = dimNamesToIterate.get(i);
                    if ("look".equals(dimName)) {
                        dimSliceOffsetMax[i] = lookValue + 1;
                        dimSliceOffset[i] = lookValue;
                    } else {
                        dimSliceOffsetMax[i] = dimsToIterate.get(i).length;
                    }
                }
                do {
                    String varName2D = ncVarName;
                    for (int i = 0; i < dimsToIterate.size(); i++) {
                        final String dimName = dimNamesToIterate.get(i);
                        if ("look".equals(dimName)) {
                            varName2D += "_" + lookExtName;
                        } else {
                            String[] varExt = dimsToIterate.get(i);
                            varName2D += "_" + varExt[dimSliceOffset[i]];
                        }
                    }
                    exportVariables.add(new VariableProxy(varName2D, variable.getDataType(), variable.attributes().getAttributes()));
                    final int[] offset = new int[rank];
                    for (int i = 0; i < dimSliceOffset.length; i++) {
                        final int dimIdx = dimIndex.get(i);
                        offset[dimIdx] = dimSliceOffset[i];
                    }
                    variableInfos.put(varName2D, new VariableInfo(ncVarName, xDimIdx, yDimIdx, offset, fillValue));
                    for (int i = dimSliceOffset.length - 1; i >= 0; i--) { // !!! reverse order
                        dimSliceOffset[i] = dimSliceOffset[i] + 1;
                        if (i > 0 && dimSliceOffset[i] == dimSliceOffsetMax[i]) {
                            if ("look".equals(dimNamesToIterate.get(i))) {
                                dimSliceOffset[i] = lookValue;
                            } else {
                                dimSliceOffset[i] = 0;
                            }
                        } else {
                            break; // leave the loop and don't increase the lower index position
                        }
                    }
                } while (dimSliceOffset[0] < dimSliceOffsetMax[0]);
            }
        }
        return exportVariables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        return new Dimension("size", pWidth, pHeight);
    }

    @Override
    public String getLongitudeVariableName() {
        return "cellon";
    }

    @Override
    public String getLatitudeVariableName() {
        return "cellat";
    }

    private double[] extractGeoMinMax() throws IOException {
        final int dimIdxY = 0;
        final int dimIdxX = 1;
        final int dimIdxL = 2; // look

        final Variable lonVar = netcdfFile.findVariable(getLongitudeVariableName());
        final float fillValue = lonVar.findAttribute(CF_FillValue).getNumericValue().floatValue();

        final Array lonArr = arrayCache.get(getLongitudeVariableName());
        final int[] verticalSection = lonArr.getShape();
        final int height = verticalSection[dimIdxY];
        final int width = verticalSection[dimIdxX];

        verticalSection[dimIdxX] = 1;
        verticalSection[dimIdxL] = 1;
        final int[] lonOrigin = new int[verticalSection.length];
        lonOrigin[dimIdxL] = lookValue;

        final Array latArr = arrayCache.get(getLatitudeVariableName());
        final int[] horizontalSection = latArr.getShape();

        horizontalSection[dimIdxY] = 1;
        horizontalSection[dimIdxL] = 1;
        final int[] latOrigin = new int[horizontalSection.length];
        latOrigin[dimIdxL] = lookValue;

        double lonMin = Double.MAX_VALUE;
        double lonMax = -Double.MAX_VALUE;
        double latMin = Double.MAX_VALUE;
        double latMax = -Double.MAX_VALUE;
        try {
            for (int i = 0; i < width; i++) {
                lonOrigin[dimIdxX] = i;
                final Array section = lonArr.section(lonOrigin, verticalSection);
                final MAMath.MinMax minMax = MAMath.getMinMaxSkipMissingData(section, fillValue);
                if (minMax.min == Double.MAX_VALUE) {
                    continue;
                }
                while (minMax.min > 180) {
                    minMax.min -= 360;
                }
                if (minMax.min < lonMin) {
                    lonMin = minMax.min;
                }

                while (minMax.max > 180) {
                    minMax.max -= 360;
                }
                if (minMax.max > lonMax) {
                    lonMax = minMax.max;
                }
            }
            // Find latitude maximum --> the product is mirrored vertically --> positive values are at the lower end.
            for (int i = height - 1; i >= 0; i--) { // bottom up
                latOrigin[dimIdxY] = i;
                final Array section = latArr.section(latOrigin, horizontalSection);
                final MAMath.MinMax minMax = MAMath.getMinMaxSkipMissingData(section, fillValue);
                if (minMax.max == latMax) {
                    continue;
                }
                latMax = minMax.max;
                break;
            }
            // Find latitude minimum --> the product is mirrored vertically --> negative values are at the upper end.
            for (int i = 0; i < height; i++) { // top down
                latOrigin[dimIdxY] = i;
                final Array section = latArr.section(latOrigin, horizontalSection);
                final MAMath.MinMax minMax = MAMath.getMinMaxSkipMissingData(section, fillValue);
                if (minMax.min == latMin) {
                    continue;
                }
                latMin = minMax.min;
                break;
            }
        } catch (InvalidRangeException e) {
            throw new IOException(e);
        }

        return new double[]{lonMin, lonMax, latMin, latMax};
    }

    private class VariableInfo {
        final String ncVarName;
        final int xDimIndex;
        final int yDimIndex;
        final int[] offset;
        final Number fillValue;

        private VariableInfo(String ncVarName, int xDimIndex, int yDimIndex, int[] offset, Number fillValue) {
            this.ncVarName = ncVarName;
            this.xDimIndex = xDimIndex;
            this.yDimIndex = yDimIndex;
            this.offset = offset;
            this.fillValue = fillValue;
        }

        @Override
        public String toString() {
            return "VariableInfo{" +
                   "ncVarName='" + ncVarName + '\'' +
                   ", xDimIndex=" + xDimIndex +
                   ", yDimIndex=" + yDimIndex +
                   ", offset=" + Arrays.toString(offset) +
                   ", fillValue=" + fillValue +
                   '}';
        }
    }
}
