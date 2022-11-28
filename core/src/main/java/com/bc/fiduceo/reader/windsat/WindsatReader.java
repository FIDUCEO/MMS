package com.bc.fiduceo.reader.windsat;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_SecondsSince2000;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import com.bc.fiduceo.util.VariableProxy;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class WindsatReader extends NetCDFReader {

    private static final String REG_EX = "RSS_WindSat_TB_L1C_r\\d{5}_\\d{8}T\\d{6}_\\d{7}_V\\d{2}.\\d.nc";
    private static final String[] LOOKS = {"fore", "aft"};
    private static final String[] FREQ_BANDS = {"068", "107", "187", "238", "370"};
    private static final String[] POLARIZATIONS = {"V", "H", "P", "M", "L", "R"};

    private final GeometryFactory geometryFactory;

    private PixelLocator pixelLocator;
    private TimeLocator timeLocator;

    WindsatReader(ReaderContext readerContext) {
        this.geometryFactory = readerContext.getGeometryFactory();
    }

    @Override
    public void open(File file) throws IOException {
        super.open(file);
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
            final Array timeArray = arrayCache.get("time");
            final Number fillValue = arrayCache.getNumberAttributeValue("_FillValue", "time");
            final int[] origin = {0, 0, 0, 2};  // select layer for "fore" and 18 GHz tb 2022-11-28
            final int[] shape = timeArray.getShape();
            shape[2] = 1;   // one view layer tb 2022-11-28
            shape[3] = 1;   // one frequency layer tb 2022-11-28

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
        final int year = Integer.parseInt(fileName.substring(26, 30));
        final int month = Integer.parseInt(fileName.substring(30, 32));
        final int day = Integer.parseInt(fileName.substring(32, 34));

        return new int[]{year, month, day};
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implmented");
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implmented");
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implmented");
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
        final Array longitudes = arrayCache.get("longitude");
        final int[] shape = longitudes.getShape();

        return new Dimension("size", shape[1], shape[0]);
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
