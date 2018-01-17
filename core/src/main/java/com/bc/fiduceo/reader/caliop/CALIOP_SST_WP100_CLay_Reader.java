/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.caliop;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.PaddingFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ArrayCache;
import com.bc.fiduceo.reader.PixelLocatorX1Yn;
import com.bc.fiduceo.reader.RawDataReader;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.reader.TimeLocator_TAI1993Vector;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.ma2.StructureData;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.ProxyReader;
import ucar.nc2.Structure;
import ucar.nc2.Variable;
import ucar.nc2.util.CancelTask;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CALIOP_SST_WP100_CLay_Reader implements Reader {

    private static final double FOOTPRINT_WIDTH_KM = 5;
    private static final double FOOTPRINT_HALF_WIDTH_KM = FOOTPRINT_WIDTH_KM / 2;
    private static final String YYYY = "(19[7-9]\\d|20[0-7]\\d)";
    private static final String MM = "(0[1-9]|1[0-2])";
    private static final String DD = "(0[1-9]|[12]\\d|3[01])";
    private static final String hh = "([01]\\d|2[0-3])";
    private static final String mm = "[0-5]\\d";
    private static final String ss = mm;
    private static final String start = "CAL_LID_L2_05kmCLay-Standard-V4-10\\.";
    private static final String end = "Z[DN]\\.hdf";
    public static final String REG_EX = start + YYYY + "-" + MM + "-" + DD + "T" + hh + "-" + mm + "-" + ss + end;
    final GeometryFactory geometryFactory;
    final CaliopUtils caliopUtils;
    private NetcdfFile netcdfFile;
    private ArrayCache arrayCache;
    private PixelLocatorX1Yn pixelLocator;
    private List<Variable> variables;

    CALIOP_SST_WP100_CLay_Reader(GeometryFactory geometryFactory, final CaliopUtils caliopUtils) {
        this.geometryFactory = geometryFactory;
        this.caliopUtils = caliopUtils;
    }

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
        arrayCache = new ArrayCache(netcdfFile);
        variables = initVariables();
    }

    @Override
    public void close() throws IOException {
        if (netcdfFile != null) {
            netcdfFile.close();
        }
        pixelLocator = null;
        variables = null;
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        return getAcquisitionInfo();
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public String getLongitudeVariableName() {
        return "Longitude";
    }

    @Override
    public String getLatitudeVariableName() {
        return "Latitude";
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            createPixelLocator();
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        return getPixelLocator();
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        final Array timeVector = arrayCache.get("Profile_Time");
        return new TimeLocator_TAI1993Vector(timeVector);
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        return caliopUtils.extractYearMonthDayFromFilename(fileName);
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Number fillValue = getFillValue(variableName);
        final Array array = arrayCache.get(variableName);
        return RawDataReader.read(centerX, centerY, interval, fillValue, array, 1);
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return readRaw(centerX, centerY, interval, variableName);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        ensureValidInterval(interval);
        final int targetFillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
        final String variableName = "Profile_Time";
        final Number fillValue = getFillValue(variableName);
        final Array taiSeconds = readRaw(x, y, interval, variableName).reshapeNoCopy(new int[]{interval.getY(), interval.getX()});
        final Array utcSecondsSince1970 = Array.factory(DataType.INT, taiSeconds.getShape());
        for (int i = 0; i < taiSeconds.getSize(); i++) {
            double val = taiSeconds.getDouble(i);
            final int targetVal;
            if (fillValue.equals(val)) {
                targetVal = targetFillValue;
            } else {
                targetVal = (int) Math.round(TimeUtils.tai1993ToUtcInstantSeconds(val));
            }
            utcSecondsSince1970.setInt(i, targetVal);
        }
        return (ArrayInt.D2) utcSecondsSince1970;
    }

    @Override
    public List<Variable> getVariables() throws IOException {
        return Collections.unmodifiableList(variables);
    }

    @Override
    public Dimension getProductSize() throws IOException {
        final Variable latVar = netcdfFile.findVariable("Latitude");
        final int[] shape = latVar.getShape();
        return new Dimension("lat", 1, shape[0]);
    }

    public Variable find(String name) {
        return netcdfFile.findVariable(name);
    }

    private AcquisitionInfo getAcquisitionInfo() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        final Variable metadata = netcdfFile.findVariable("metadata");
        final StructureData structureData = ((Structure) metadata).readStructure();
        final Array start = structureData.getArray(structureData.findMember("Date_Time_at_Granule_Start"));
        final Array end = structureData.getArray(structureData.findMember("Date_Time_at_Granule_End"));
        final Date sensingStart;
        final Date sensingStop;
        try {
            sensingStart = caliopUtils.getDate(start);
            sensingStop = caliopUtils.getDate(end);
        } catch (ParseException e) {
            throw new IOException(e);
        }
        acquisitionInfo.setSensingStart(sensingStart);
        acquisitionInfo.setSensingStop(sensingStop);

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);
        final Array lats = arrayCache.get("Latitude");
        final Array lons = arrayCache.get("Longitude");
        final int size = (int) lats.getSize();
        if (size != lons.getSize()) {
            throw new IOException("Corrupt file. Longitude and Latitude must have the same size!");
        }
        final ArrayList<Point> points = new ArrayList<>();
        final int add;
        if (size < 60) {
            add = 60;
        } else {
            add = size / 60;
        }
        final int lastPosition = size - 1;
        for (int i = 0; i < lastPosition; i += add) {
            points.add(geometryFactory.createPoint(lons.getFloat(i), lats.getFloat(i)));
        }
        points.add(geometryFactory.createPoint(lons.getFloat(lastPosition), lats.getFloat(lastPosition)));

        final LineString lineString = geometryFactory.createLineString(points);
        final Polygon boundingGeometry = PaddingFactory.createLinePadding(lineString, FOOTPRINT_WIDTH_KM, geometryFactory);
        acquisitionInfo.setBoundingGeometry(boundingGeometry);
        acquisitionInfo.setTimeAxes(new TimeAxis[]{geometryFactory.createTimeAxis(lineString, sensingStart, sensingStop)});

        return acquisitionInfo;
    }

    private void createPixelLocator() throws IOException {
        final Array lons = arrayCache.get("Longitude");
        final Array lats = arrayCache.get("Latitude");
        final double maxDistanceKm = getMaxDistance();
        pixelLocator = new PixelLocatorX1Yn(maxDistanceKm, lons, lats);
    }

    private double getMaxDistance() {
        final double fp2 = Math.pow(FOOTPRINT_HALF_WIDTH_KM, 2);
        return Math.sqrt(2 * fp2);
    }

    private List<Variable> initVariables() throws IOException {
        final ArrayList<Variable> variables = new ArrayList<>();

        final Group rootGroup = netcdfFile.getRootGroup();
        variables.add(getVariableCompleted(rootGroup, "Number_Layers_Found"));
        variables.add(getVariableCompleted(rootGroup, "Column_Feature_Fraction"));
        variables.add(getVariableCompleted(rootGroup, "FeatureFinderQC"));
        variables.add(getVariableCompleted(rootGroup, "Feature_Classification_Flags"));
        variables.add(getVariableCompleted(rootGroup, "ExtinctionQC_532"));
        variables.add(getVariableCompleted(rootGroup, "CAD_Score"));
        variables.add(getVariableCompleted(rootGroup, "Layer_IAB_QA_Factor"));
        variables.add(getVariableCompleted(rootGroup, "Opacity_Flag"));
        variables.add(getVariableCompleted(rootGroup, "Ice_Water_Path"));
        variables.add(getVariableCompleted(rootGroup, "Ice_Water_Path_Uncertainty"));
        variables.add(getVariableCompleted(rootGroup, "Feature_Optical_Depth_532"));
        variables.add(getVariableCompleted(rootGroup, "Feature_Optical_Depth_Uncertainty_532"));
        variables.add(getVariableCompleted(rootGroup, "Layer_Top_Altitude"));
        variables.add(getVariableCompleted(rootGroup, "Layer_Base_Altitude"));
        variables.add(getVariableCompleted(rootGroup, "Profile_ID"));
        variables.add(getVariableCompleted(rootGroup, "Latitude"));
        variables.add(getVariableCompleted(rootGroup, "Longitude"));
        variables.add(getVariableCompleted(rootGroup, "Profile_Time"));
        variables.add(getVariableCompleted(rootGroup, "Profile_UTC_Time"));
        variables.add(getVariableCompleted(rootGroup, "Spacecraft_Position_x"));
        variables.add(getVariableCompleted(rootGroup, "Spacecraft_Position_y"));
        variables.add(getVariableCompleted(rootGroup, "Spacecraft_Position_z"));

        return variables;
    }

    private Variable getVariableCompleted(Group rootGroup, String shortName) {
        Variable variable;
        boolean x = shortName.endsWith("_x");
        boolean y = shortName.endsWith("_y");
        boolean z = shortName.endsWith("_z");
        try {
            if (x || y || z) {
                Variable v = netcdfFile.findVariable(rootGroup, shortName.substring(0, shortName.length() - 2));
                int[] shape = v.getShape();
                shape[1] = 1;
                shape[2] = 1;
                int xyz = x ? 0 : y ? 1 : 2;
                final int[] origin = {0, 1, xyz};
                v = v.section(new Section(origin, shape));
                v.setShortName(shortName);
                List<ucar.nc2.Dimension> dimensions = Collections.singletonList(v.getDimension(1));
                variable = v.reduce(dimensions);
                arrayCache.inject(variable);
            } else {
                variable = netcdfFile.findVariable(rootGroup, shortName);
                if (shortName.equals("Latitude")
                    || shortName.equals("Longitude")
                    || shortName.equals("Profile_Time")
                    || shortName.equals("Profile_UTC_Time")
                        ) {
                    int[] shape = variable.getShape();
                    shape[1] = 1;
                    variable = variable.section(new Section(new int[]{0, 1}, shape));
                    arrayCache.inject(variable);
                } else if (shortName.equals("Profile_ID")) {
                    int[] shape = variable.getShape();
                    shape[1] = 1;
                    variable = variable.section(new Section(new int[]{0, 0}, shape));
                    final ProxyReader sectionProxyReader = variable.getProxyReader();
                    variable.setProxyReader(new ProxyReader() {
                        @Override
                        public Array reallyRead(Variable client, CancelTask cancelTask) throws IOException {
                            Array array = sectionProxyReader.reallyRead(client, cancelTask);
                            return addSeven(array);
                        }

                        @Override
                        public Array reallyRead(Variable client, Section section, CancelTask cancelTask) throws IOException, InvalidRangeException {
                            Array array = sectionProxyReader.reallyRead(client, section, cancelTask);
                            return addSeven(array);
                        }

                        private Array addSeven(Array array) {
                            IndexIterator indexIterator = array.getIndexIterator();
                            while (indexIterator.hasNext()) {
                                int val = indexIterator.getIntNext();
                                indexIterator.setIntCurrent(val + 7);
                            }
                            return array;
                        }
                    });
                    arrayCache.inject(variable);
                }
            }
        } catch (InvalidRangeException e) {
            throw new RuntimeException("Unable to initialize " + shortName, e);
        }
        ensureFillValue(variable);
        return variable;
    }

    private Number getFillValue(String variableName) throws IOException {
        return arrayCache.getNumberAttributeValue(CF_FILL_VALUE_NAME, variableName);
    }

    private void ensureValidInterval(Interval interval) {
        if (interval.getX() > 1) {
            throw new RuntimeException("An interval with x > 1 is not allowed.");
        }
    }

    private void ensureFillValue(Variable ncVariable) {
        if (ncVariable.findAttribute(CF_FILL_VALUE_NAME) == null) {
            final Number fillValue = caliopUtils.getFillValue(ncVariable);
            ncVariable.addAttribute(new Attribute(CF_FILL_VALUE_NAME, fillValue));
        }
    }

}
