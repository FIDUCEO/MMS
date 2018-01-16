package com.bc.fiduceo.reader.amsr.amsr2;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.amsr.AmsrUtils;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SynchronizeOnNonFinalField")
class AMSR2_Reader implements Reader {

    private static final String REG_EX = "GW1AM2_\\d{12}_\\d{3}[AD]_L1SGRTBR_\\d{7}.h5(.gz)?";
    private static final String[] LAND_OCEAN_FLAG_EXTENSIONS = new String[]{"6", "10", "23", "36"};

    private static final String LON_VARIABLE_NAME = "Longitude_of_Observation_Point_for_89A";
    private static final String LAT_VARIABLE_NAME = "Latitude_of_Observation_Point_for_89A";

    private final GeometryFactory geometryFactory;
    private NetcdfFile netcdfFile;
    private ArrayCache arrayCache;
    private BoundingPolygonCreator boundingPolygonCreator;

    AMSR2_Reader(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
        arrayCache = new ArrayCache(netcdfFile);

        arrayCache.inject(new GeolocationVariable(LON_VARIABLE_NAME, netcdfFile));
        arrayCache.inject(new GeolocationVariable(LAT_VARIABLE_NAME, netcdfFile));
    }

    @Override
    public void close() throws IOException {
        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        setSensingTimes(acquisitionInfo);
        AmsrUtils.setNodeType(acquisitionInfo, netcdfFile);
        setGeometries(acquisitionInfo);

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
        final Array scan_time = arrayCache.get("Scan_Time");
        return new TimeLocator_TAI1993Vector(scan_time);
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        throw new RuntimeException("not implemented");
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
        final List<Variable> variables = new ArrayList<>();

        final List<Variable> fileVariables = netcdfFile.getVariables();
        for (final Variable fileVariable: fileVariables) {
            final String shortName = fileVariable.getShortName();
            if(shortName.contains("(original,89GHz") ||
                    shortName.contains("Attitude_Data") ||
                    shortName.contains("Position_in_Orbit") ||
                    shortName.contains("Navigation_Data") ||
                    shortName.contains("Latitude_of_Observation_Point") ||  // we add the relevant sub-sampled arrays during file-open tb 2018-01-16
                    shortName.contains("Longitude_of_Observation_Point") ||
                    shortName.contains("Pixel_Data_Quality_89")||
                    shortName.contains("Scan_Data_Quality")||   // we may need this tb 2018-01-16
                    shortName.contains("Land_Ocean_Flag_89")) {
                continue;
            }

            if (shortName.contains("Land_Ocean_Flag_6_to_36")) {
                final int[] shape = fileVariable.getShape();
                shape[0] = 1;   // pick a single layer
                final int[] origin = {0, 0, 0};
                final String variableNamePrefix = "Land_Ocean_Flag_";
                for (int i = 0; i < LAND_OCEAN_FLAG_EXTENSIONS.length; i++) {
                    origin[0] = i;
                    final Section section = new Section(origin, shape);
                    final Variable channelVariable = fileVariable.section(section);
                    channelVariable.setName(variableNamePrefix + LAND_OCEAN_FLAG_EXTENSIONS[i]);
                    variables.add(channelVariable);
                }
                continue;
            }

            if (shortName.contains("Pixel_Data_Quality_6_to_36")) {
                final PixelDataQualityVariable qualityVariable = new PixelDataQualityVariable(fileVariable);
                variables.add(qualityVariable);
                continue;
            }

            variables.add(fileVariable);
        }

        return variables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getLongitudeVariableName() {
        return LON_VARIABLE_NAME;
    }

    @Override
    public String getLatitudeVariableName() {
        return LAT_VARIABLE_NAME;
    }

    // package access for testing only tb 2018-01-15
    static ProductData.UTC getUtcDate(Attribute attribute) throws IOException {
        final String stringValue = attribute.getStringValue();

        final String utcString = stringValue.substring(0, stringValue.length() - 1);
        final String utcWithMicros = utcString.concat("000");
        try {
            return ProductData.UTC.parse(utcWithMicros, "yyyy-MM-dd'T'HH:mm:ss");
        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        }
    }

    private void setSensingTimes(AcquisitionInfo acquisitionInfo) throws IOException {
        final Attribute startDateTime = NetCDFUtils.getGlobalAttributeSafe("ObservationStartDateTime", netcdfFile);
        final Attribute endDateTime = NetCDFUtils.getGlobalAttributeSafe("ObservationEndDateTime", netcdfFile);
        acquisitionInfo.setSensingStart(getUtcDate(startDateTime).getAsDate());
        acquisitionInfo.setSensingStop(getUtcDate(endDateTime).getAsDate());
    }

    private BoundingPolygonCreator getBoundingPolygonCreator() {
        if (boundingPolygonCreator == null) {
            // @todo 2 tb/tb move intervals to config 2018-01-15
            boundingPolygonCreator = new BoundingPolygonCreator(new Interval(20, 100), geometryFactory);
        }

        return boundingPolygonCreator;
    }

    private void setGeometries(AcquisitionInfo acquisitionInfo) throws IOException {
        final Array lonArray = arrayCache.get(LON_VARIABLE_NAME);
        final Array latArray = arrayCache.get(LAT_VARIABLE_NAME);

        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator();
        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(lonArray, latArray);
        if (!boundingGeometry.isValid()) {
            // @todo 2 tb/tb implement splitted polygon approach if we encounter failures here 2018-01-15
            throw new RuntimeException("Detected invalid bounding geometry");
        }
        acquisitionInfo.setBoundingGeometry(boundingGeometry);

        final Geometries geometries = new Geometries();
        geometries.setBoundingGeometry(boundingGeometry);
        final LineString timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(lonArray, latArray);
        geometries.setTimeAxesGeometry(timeAxisGeometry);
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);
    }
}
