/*
 * Copyright (C) 2018 Brockmann Consult GmbH
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

package com.bc.fiduceo.reader.airs;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.hdf.HdfEOSUtil;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.location.PixelLocatorFactory;
import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ArrayCache;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.fiduceo.reader.Read2dFrom1d;
import com.bc.fiduceo.reader.Read2dFrom2d;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.reader.TimeLocator_TAI1993;
import com.bc.fiduceo.reader.WindowReader;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.jdom2.Element;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class AIRS_L1B_Reader implements Reader {

    final ReaderContext readerContext;
    private final Logger logger;
    private NetcdfFile netcdfFile = null;
    private BoundingPolygonCreator boundingPolygonCreator;
    private ArrayCache arrayCache;
    private PixelLocator pixelLocator;
    private boolean needVariablesInitialisation = true;
    private ArrayList<Variable> variablesList;
    private HashMap<String, Number> fillValueMap;
    private HashMap<String, WindowReader> readersMap;
    private Dimension productSize;

    AIRS_L1B_Reader(ReaderContext readerContext) {
        logger = FiduceoLogger.getLogger();
        this.readerContext = readerContext;
    }

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
        arrayCache = new ArrayCache(netcdfFile).withVariableFinder((group, variableName) -> findVariable(variableName));
    }

    @Override
    public void close() throws IOException {
        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }
        if (fillValueMap != null) {
            fillValueMap.clear();
            fillValueMap = null;
        }
        if (readersMap != null) {
            readersMap.clear();
            readersMap = null;
        }
        if (variablesList != null) {
            variablesList.clear();
            variablesList = null;
        }

        needVariablesInitialisation = true;
        productSize = null;
        arrayCache = null;
        boundingPolygonCreator = null;
        pixelLocator = null;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final Array latitudes = arrayCache.get(getLatitudeVariableName());
            final Array longitudes = arrayCache.get(getLongitudeVariableName());

            final int[] shape = longitudes.getShape();
            final int width = shape[1];
            final int height = shape[0];
            pixelLocator = PixelLocatorFactory.getSwathPixelLocator(longitudes, latitudes, width, height);
        }
        return pixelLocator;
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        acquisitionInfo.setNodeType(readNodeType());

        final Group rootGroup = netcdfFile.getRootGroup();
        final String coreMetaString = HdfEOSUtil.getEosMetadata(HdfEOSUtil.CORE_METADATA, rootGroup);
        final Element eosElement = HdfEOSUtil.getEosElement(coreMetaString);

        final Date sensingStart = HdfEOSUtil.parseDate(HdfEOSUtil.getElementValue(eosElement, HdfEOSUtil.RANGE_BEGINNING_DATE),
                                                       HdfEOSUtil.getElementValue(eosElement, HdfEOSUtil.RANGE_BEGINNING_TIME));
        final Date sensingStop = HdfEOSUtil.parseDate(HdfEOSUtil.getElementValue(eosElement, HdfEOSUtil.RANGE_ENDING_DATE),
                                                      HdfEOSUtil.getElementValue(eosElement, HdfEOSUtil.RANGE_ENDING_TIME));

        acquisitionInfo.setSensingStart(sensingStart);
        acquisitionInfo.setSensingStop(sensingStop);


        final Group l1bAirsGroup = rootGroup.findGroup("L1B_AIRS_Science");
        if (l1bAirsGroup == null) {
            throw new IOException("'L1B_AIRS_Science' data group not found");
        }
        final Group geolocationFields = l1bAirsGroup.findGroup("Geolocation_Fields");
        final Variable latitudeVariable = geolocationFields.findVariable(getLatitudeVariableName());
        final Variable longitudeVariable = geolocationFields.findVariable(getLongitudeVariableName());
        final Array latitudes = latitudeVariable.read();
        final Array longitudes = longitudeVariable.read();
        if (boundingPolygonCreator == null) {
            boundingPolygonCreator = new BoundingPolygonCreator(new Interval(10, 50), readerContext.getGeometryFactory());
        }
        acquisitionInfo.setBoundingGeometry(boundingPolygonCreator.createBoundingGeometry(longitudes, latitudes));
        final LineString timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitudes, latitudes);
        ReaderUtils.setTimeAxes(acquisitionInfo, timeAxisGeometry, readerContext.getGeometryFactory());

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return "AIRS\\.\\d{4}\\.\\d{2}\\.\\d{2}\\.\\d{3}\\.L1B\\.AIRS_Rad\\..*\\.hdf";
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
    public PixelLocator getSubScenePixelLocator(Polygon sceneIndex) throws IOException {
        // There is no need to implement this method
        // AIRS L1B products do not overlap themself.
        throw new RuntimeException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        return new TimeLocator_TAI1993(arrayCache.get("Time"));
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        if (fileName == null) {
            throw new RuntimeException("The file name \"" + fileName + "\" is not valid.");
        }
        final String regEx = getRegEx();
        if (!fileName.matches(regEx)) {
            throw new RuntimeException("A file name matching the expression \"" + regEx + "\" expected. But was \"" + fileName + "\"");
        }
        //noinspection UnnecessaryLocalVariable
        final int[] ymd = new int[]{
                Integer.parseInt(fileName.substring(5, 9)),
                Integer.parseInt(fileName.substring(10, 12)),
                Integer.parseInt(fileName.substring(13, 15))
        };
        return ymd;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        ensureInitialisation();
        return readersMap.get(variableName).read(centerX, centerY, interval);
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return readRaw(centerX, centerY, interval, variableName);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        final int targetFillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
        final String variableName = "Time";
        final Array taiSeconds = readRaw(x, y, interval, variableName);
        final Array utcSecondsSince1970 = Array.factory(DataType.INT, taiSeconds.getShape());
        final Number fillValue = fillValueMap.get(variableName);
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
    public List<Variable> getVariables() throws InvalidRangeException {
        ensureInitialisation();
        return variablesList;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        if (productSize == null) {
            final Array latitudes = arrayCache.get(getLatitudeVariableName());
            final int[] ls = latitudes.getShape();
            productSize = new Dimension("product size", ls[1], ls[0]);
        }
        return productSize;
    }

    public Array readSpectrum(int minY, int minX, int[] readShape, String varName) throws IOException, InvalidRangeException {
        final Variable variable = findVariable(varName);

        final int rank = variable.getRank();
        final int[] shape = variable.getShape();
        if (rank == 2 && !Arrays.equals(shape, new int[]{productSize.getNy(), productSize.getNx()})) {
            return variable.read(new int[]{minY, 0}, new int[]{readShape[0], readShape[2]});
        } else {
            return variable.read(new int[]{minY, minX, 0}, readShape);
        }
    }

    private NodeType readNodeType() {
        String nodeType = null;
        final List<Group> groups = netcdfFile.getRootGroup().getGroups().get(0).getGroups();
        for (Group group : groups) {
            if (group.getShortName().equals("Swath_Attributes")) {
                List<Attribute> attributes = group.getAttributes();
                for (Attribute attribute : attributes) {
                    if (attribute.getShortName().equals("node_type")) {
                        nodeType = attribute.getStringValue();
                    }
                }
            }
        }
        if (nodeType == null) {
            logger.info("NodeType is not DEFINED.");
            return NodeType.UNDEFINED;
        }

        return NodeType.fromId(nodeType.equals("Ascending") ? 0 : 1);
    }

    private void ensureInitialisation() throws InvalidRangeException {
        if (needVariablesInitialisation) {
            initializeVariables();
        }
    }

    private void initializeVariables() throws InvalidRangeException {
        final String dimNameY = "L1B_AIRS_Science/GeoTrack";
        final String dimNameX = "L1B_AIRS_Science/GeoXTrack";
        final String dimNameChannels = "L1B_AIRS_Science/Data_Fields/Channel";
        variablesList = new ArrayList<>();
        fillValueMap = new HashMap<>();
        readersMap = new HashMap<>();

        final int height = NetCDFUtils.getDimensionLength(dimNameY, netcdfFile);
        final int width = NetCDFUtils.getDimensionLength(dimNameX, netcdfFile);
        final int numChannels = NetCDFUtils.getDimensionLength(dimNameChannels, netcdfFile);

        final List<Variable> variables = netcdfFile.getVariables();
        for (Variable variable : variables) {
            String shortName = variable.getShortName();
            int[] shape = variable.getShape();
            final int rank = variable.getRank();
            if (shape[0] != height
                || rank > 2
                || rank == 2 && shape[1] == numChannels) {
                continue;
            }

            final Number fillValue = ensureFillValue(variable);
            variablesList.add(variable);
            fillValueMap.put(shortName, fillValue);
            if (rank == 1) {
                readersMap.put(shortName, new Read2dFrom1d(arrayCache, shortName, width, fillValue));
            } else {
                readersMap.put(shortName, new Read2dFrom2d(arrayCache, shortName, width, fillValue));
            }
        }

        needVariablesInitialisation = false;
    }

    private Number ensureFillValue(Variable variable) {
        final Attribute attribute = variable.findAttribute(CF_FILL_VALUE_NAME);
        if (attribute != null) {
            return attribute.getNumericValue();
        }

        final Attribute FV_Att = netcdfFile.findGlobalAttribute("L1B_AIRS_Science_Swath_Attributes__FV_" + variable.getShortName());
        if (FV_Att != null) {
            final Attribute cfFV = new Attribute(CF_FILL_VALUE_NAME, FV_Att);
            variable.addAttribute(cfFV);
            return FV_Att.getNumericValue();
        }

        final DataType dataType = variable.getDataType();
        if (dataType.isNumeric()) {
            final Number fillValue = NetCDFUtils.getDefaultFillValue(dataType.getPrimitiveClassType());
            variable.addAttribute(new Attribute(CF_FILL_VALUE_NAME, fillValue));
            return fillValue;
        }
        return null;
    }

    private Variable findVariable(String varName) {
        synchronized (netcdfFile) {
            final List<Variable> variables = netcdfFile.getVariables();
            for (Variable var : variables) {
                if (var.getShortName().equals(varName)) {
                    return var;
                }
            }
        }
        return null;
    }

}
