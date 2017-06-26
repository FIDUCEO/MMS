/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.fiduceo.reader.calipso;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ArrayCache;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.reader.TimeLocator_TAI1993Vector;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.ma2.StructureData;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Structure;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

class CALIPSO_L2_VFM_Reader implements Reader {

    private static final String YYYY = "(19[7-9]\\d|20[0-7]\\d)";
    private static final String MM = "(0[1-9]|1[0-2])";
    private static final String DD = "(0[1-9]|[12]\\d|3[01])";
    private static final String hh = "([01]\\d|2[0-3])";
    private static final String mm = "[0-5]\\d";
    private static final String ss = mm;
    private static final String start = "CAL_LID_L2_VFM-Standard-V4-10.";
    private static final String end = "Z[DN].hdf";
    private static final String REG_EX = start + YYYY + "-" + MM + "-" + DD + "T" + hh + "-" + mm + "-" + ss + end;

    private final static short[] nadirLineIndices = calcalculateIndizes();
    private final GeometryFactory geometryFactory;
    private NetcdfFile netcdfFile;
    private ArrayCache arrayCache;

    public CALIPSO_L2_VFM_Reader(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
        arrayCache = new ArrayCache(netcdfFile);

    }

    @Override
    public void close() throws IOException {
        if (netcdfFile != null) {
            netcdfFile.close();
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        final Variable metadata = netcdfFile.findVariable("metadata");
        final StructureData structureData = ((Structure) metadata).readStructure();
        final Array start = structureData.getArray(structureData.findMember("Date_Time_at_Granule_Start"));
        final Array end = structureData.getArray(structureData.findMember("Date_Time_at_Granule_End"));
        final Date sensingStart;
        final Date sensingStop;
        try {
            sensingStart = getDate(start);
            sensingStop = getDate(end);
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
        final int add = size / 60;
        final int lastPosition = size - 1;
        for (int i = 0; i < lastPosition; i += add) {
            points.add(geometryFactory.createPoint(lons.getFloat(i), lats.getFloat(i)));
        }
        points.add(geometryFactory.createPoint(lons.getFloat(lastPosition), lats.getFloat(lastPosition)));

        final LineString lineString = geometryFactory.createLineString(points);
        acquisitionInfo.setBoundingGeometry(lineString);
        acquisitionInfo.setTimeAxes(new TimeAxis[]{geometryFactory.createTimeAxis(lineString, sensingStart, sensingStop)});

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        final Array timeVector = arrayCache.get("Profile_Time");
        return new TimeLocator_TAI1993Vector(timeVector);
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        final ArrayList<Variable> variables = new ArrayList<>();

        final Group rootGroup = netcdfFile.getRootGroup();
        final List<Variable> ncVariables = netcdfFile.getVariables();

        for (Variable ncVariable : ncVariables) {
            final String fullName = ncVariable.getFullName();
            if (ncVariable.getGroup() != rootGroup
                || fullName.contains("metadata")
                || fullName.contains("Feature_Classification_Flags")) {
                continue;
            }
            final String spacecraftName = "Spacecraft_Position";
            if (fullName.equals(spacecraftName)) {
                final String[] suffixes = {"_x", "_y", "_z"};
                final int[] shape = ncVariable.getShape();
                shape[1] = 1;
                for (int i = 0; i < 3; i++) {
                    final int[] origin = {0, i};
                    final Variable section = ncVariable.section(new Section(origin, shape));
                    section.setName(spacecraftName + suffixes[i]);
                    arrayCache.inject(section);
                    ensureFillValue(section);
                    variables.add(section);
                }
            } else {
                ensureFillValue(ncVariable);
                variables.add(ncVariable);
            }
        }
        return variables;
    }

    private void ensureFillValue(Variable ncVariable) {
        final DataType dataType = ncVariable.getDataType();
        boolean fillValueRenamed = renameFillValueAttributeIfExist(ncVariable);
        if (!fillValueRenamed) {
            final Attribute attribute = ncVariable.findAttribute(NetCDFUtils.CF_UNSIGNED);
            final boolean unsigned = attribute != null && Boolean.parseBoolean(attribute.getStringValue());
            final Number fillValue = NetCDFUtils.getDefaultFillValue(dataType, unsigned);
            final Attribute fillValueAtt = new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, fillValue);
            ncVariable.addAttribute(fillValueAtt);
        }
    }

    @Override
    public Dimension getProductSize() throws IOException {
        final Variable latVar = netcdfFile.findVariable("Latitude");
        final int[] shape = latVar.getShape();
        return new Dimension("lat", shape[1], shape[0]);
    }

    static Array readNadirClassificationFlags(Array array) throws InvalidRangeException {
        final short[] storage = (short[]) array.getStorage();
        final short[] nadirStorage = new short[545];
        int nadirIdx = 0;
        for (int i = 0; i < nadirLineIndices.length; i += 2) {
            int begin = nadirLineIndices[i];
            int end = nadirLineIndices[i + 1];
            final short[] shorts = Arrays.copyOfRange(storage, begin, end + 1);
            for (short aShort : shorts) {
                nadirStorage[nadirIdx] = aShort;
                nadirIdx++;
            }
        }
        return Array.factory(nadirStorage);
    }

    static short[] calcalculateIndizes() {
        final short a1Samples = 55; // altitude region 1
        final short a1Begin = a1Samples;
        final short a1End = a1Samples * 2 - 1;

        final short a1Block = a1Samples * 3;

        final short a2Samples = 200; // altitude region 2
        final short a2Begin = a1Block + a2Samples * 2;
        final short a2End = a1Block + a2Samples * 3 - 1;

        final short a2Block = a2Samples * 5;

        final short a3Samples = 290; // altitude region 3
        final short a3Begin = a1Block + a2Block + a3Samples * 7;
        final short a3End = a1Block + a2Block + a3Samples * 8 - 1;

        return new short[]{a1Begin, a1End, a2Begin, a2End, a3Begin, a3End};
    }

    private boolean renameFillValueAttributeIfExist(Variable ncVariable) {
        final List<Attribute> attributes = ncVariable.getAttributes();
        for (Attribute attribute : attributes) {
            final String shortName = attribute.getShortName();
            if (shortName.toLowerCase().contains("fillvalue")) {
                ncVariable.removeAttribute(attribute.getShortName());
                final Attribute fillAttr = new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, attribute.getNumericValue());
                ncVariable.addAttribute(fillAttr);
                return true;
            }
        }
        return false;
    }

    private Date getDate(Array charArray) throws ParseException {
        final String pattern = "yyyy-MM-dd'T'HH:mm:ss";
        final String str = charArray.toString().trim();
        final String analysableUtcStr = stripTrailingZ(str);
        final ProductData.UTC utc = ProductData.UTC.parse(analysableUtcStr, pattern);
        return utc.getAsDate();
    }

    private String stripTrailingZ(String str) {
        if (str.endsWith("Z")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }
}
