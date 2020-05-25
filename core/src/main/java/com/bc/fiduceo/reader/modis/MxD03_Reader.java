package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.RawDataReader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class MxD03_Reader extends NetCDFReader {

    private static final String REG_EX = "M([OY])D03.A\\d{7}.\\d{4}.\\d{3}.\\d{13}.hdf";
    private static final String GEOLOCATION_GROUP = "MODIS_Swath_Type_GEO/Geolocation_Fields";
    private static final String DATA_GROUP = "MODIS_Swath_Type_GEO/Data_Fields";

    private Dimension productSize;

    MxD03_Reader(ReaderContext readerContext) {
    }

    static String getGroupName(String variableName) {
        if (variableName.equals("Longitude") || variableName.equals("Latitude")) {
            return GEOLOCATION_GROUP;
        } else if (variableName.equals("Height") ||
                variableName.contains("Zenith") ||
                variableName.contains("Azimuth") ||
                variableName.equals("Range") ||
                variableName.equals("Land_SeaMask") ||
                variableName.equals("WaterPresent") ||
                variableName.equals("gflags")) {
            return DATA_GROUP;
        } else if (variableName.equals("Scan_number")) {
            return null;
        }

        throw new IllegalArgumentException("not supported variable: " + variableName);
    }

    @Override
    public void open(File file) throws IOException {
        super.open(file);
        injectScanNumberVariable();
    }

    @Override
    public void close() throws IOException {
        productSize = null;
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public String getRegEx() {
        return REG_EX;
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
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException {
        final String groupName = getGroupName(variableName);
        Array array = arrayCache.get(groupName, variableName);

        Number fillValue = arrayCache.getNumberAttributeValue(NetCDFUtils.CF_FILL_VALUE_NAME, groupName, variableName);
        if (fillValue == null) {
            fillValue = NetCDFUtils.getDefaultFillValue(array);
        }

        return RawDataReader.read(centerX, centerY, interval, fillValue, array, getProductSize());
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException {
        final Array rawData = readRaw(centerX, centerY, interval, variableName);
        final String groupName = getGroupName(variableName);

        final double scaleFactor = getScaleFactorCf(groupName, variableName);
        final double offset = getOffset(groupName, variableName);
        if (ReaderUtils.mustScale(scaleFactor, offset)) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, offset);
            return MAMath.convert2Unpacked(rawData, scaleOffset);
        }

        return rawData;
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public List<Variable> getVariables() {
        final List<Variable> variables = netcdfFile.getVariables();
        final List<Variable> result = new ArrayList<>(12);

        for (final Variable variable : variables) {
            final String shortName = variable.getShortName();
            if (shortName.equals("Latitude") ||
                    shortName.equals("Longitude") ||
                    shortName.equals("Height") ||
                    shortName.equals("SensorZenith") ||
                    shortName.equals("SensorAzimuth") ||
                    shortName.equals("Range") ||
                    shortName.equals("SolarZenith") ||
                    shortName.equals("SolarAzimuth") ||
                    shortName.equals("Land_SeaMask") ||
                    shortName.equals("WaterPresent") ||
                    shortName.equals("gflags")) {
                result.add(variable);
            }

            if (shortName.equals("Scan_number")) {
                result.add(new ScanNumberVariable(variable));
            }
        }

        return result;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        if (productSize == null) {
            final Array longitude = arrayCache.get(GEOLOCATION_GROUP, "Longitude");
            final int[] shape = longitude.getShape();
            productSize = new Dimension("shape", shape[1], shape[0]);
        }
        return productSize;
    }

    @Override
    public String getLongitudeVariableName() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public String getLatitudeVariableName() {
        throw new IllegalStateException("not implemented");
    }

    private void injectScanNumberVariable() {
        final Variable scanNumber = netcdfFile.findVariable("Scan_number");
        arrayCache.inject(new ScanNumberVariable(scanNumber));
    }
}
