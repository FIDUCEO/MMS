package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.esa.snap.framework.datamodel.ProductData;
import org.jdom2.Element;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class AIRS_L1B_Reader implements Reader {
    private static final String RANGE_BEGINNING_DATE = "RANGEBEGINNINGDATE";
    private static final String RANGE_ENDING_DATE = "RANGEENDINGDATE";
    private static final String RANGE_BEGINNING_TIME = "RANGEBEGINNINGTIME";
    private static final String RANGE_ENDING_TIME = "RANGEENDINGTIME";
    private static volatile String value;
    private static final String CORE_METADATA = "coremetadata";
    private static final String ASSOCIATED_SENSORSHORT_NAME = "ASSOCIATEDSENSORSHORTNAME";

    private Element eosElement;
    private NetcdfFile netcdfFile;

    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
        final Group eosGroup = netcdfFile.getRootGroup();
        final String coreMateString = getEosMetadata(CORE_METADATA, eosGroup);
        eosElement = getEosElement(coreMateString);
    }


    public void close() throws IOException {
        netcdfFile.close();
    }

    public SatelliteObservation read() throws IOException, ParseException, com.vividsolutions.jts.io.ParseException {
        String rangeBeginningDate = getElementValue(eosElement, RANGE_BEGINNING_DATE) + " " + getElementValue(eosElement, RANGE_BEGINNING_TIME);
        String rangeEndingDate = getElementValue(eosElement, RANGE_ENDING_DATE) + " " + getElementValue(eosElement, RANGE_ENDING_TIME);
        final SatelliteObservation satelliteObservation = new SatelliteObservation();
        DateFormat dateFormat = ProductData.UTC.createDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

        satelliteObservation.setStartTime(dateFormat.parse(rangeBeginningDate));
        satelliteObservation.setStopTime(dateFormat.parse(rangeEndingDate));
        satelliteObservation.setGeoBounds(getCreatePolygon(netcdfFile));

        Sensor sensor = new Sensor();
        sensor.setName(getElementValue(eosElement, ASSOCIATED_SENSORSHORT_NAME));
        satelliteObservation.setSensor(sensor);
        satelliteObservation.setNodeType(readNodeType());
        return satelliteObservation;
    }


    private NodeType readNodeType() {
        String nodeType = null;
        List<Group> groups = netcdfFile.getRootGroup().getGroups().get(0).getGroups();
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
        assert nodeType != null;
        return NodeType.fromId(nodeType.equals("Ascending") ? 0 : 1);
    }


    private Geometry getCreatePolygon(NetcdfFile netcdfFile) throws IOException, com.vividsolutions.jts.io.ParseException {
        List<Group> groups = netcdfFile.getRootGroup().getGroups().get(0).getGroups();
        int geoXTrack = netcdfFile.findDimension("L1B_AMSU_GeoXTrack").getLength() - 1;
        int geoTrack = netcdfFile.findDimension("L1B_AMSU_GeoTrack").getLength() - 1;

        ArrayDouble.D2 arrayLatitude = null;
        ArrayDouble.D2 arrayLongitude = null;


        for (Group group : groups) {
            if (group.getShortName().equals("Geolocation_Fields")) {
                List<Variable> variables = group.getVariables();
                for (Variable variable : variables) {
                    if (variable.getShortName().startsWith("Latitude")) {
                        arrayLatitude = (ArrayDouble.D2) variable.read();
                    }

                    if (variable.getShortName().startsWith("Longitude")) {
                        arrayLongitude = (ArrayDouble.D2) variable.read();
                    }
                }
            }
        }
        int interval = 4;
        assert arrayLongitude != null;
        assert arrayLatitude != null;
        List<Coordinate> coordinates = new ArrayList<>();
        for (int x = 1; x < geoXTrack; x += interval) {
            coordinates.add(new Coordinate(arrayLongitude.get(0, x), arrayLatitude.get(0, x)));
        }
        for (int y = 0; y <= geoTrack; y += interval) {
            coordinates.add(new Coordinate(arrayLongitude.get(y, geoXTrack), arrayLatitude.get(y, geoXTrack)));
        }
        for (int x = geoXTrack - 1; x > 0; x -= interval) {
            coordinates.add(new Coordinate(arrayLongitude.get(geoTrack, x), arrayLatitude.get(geoTrack, x)));
        }
        for (int y = geoTrack; y >= 0; y -= interval) {
            coordinates.add(new Coordinate(arrayLongitude.get(y, 0), arrayLatitude.get(y, 0)));
        }
        coordinates.add(coordinates.get(0));
        return new GeometryFactory().createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));
    }


    static String getElementValue(Element element, String attribute) {
        for (Element subElement : element.getChildren()) {
            if (subElement.getName().equals(attribute)) {
                value = subElement.getChild("VALUE").getValue();
                break;
            } else {
                getElementValue(subElement, attribute);
            }
        }
        return value;
    }


    // package access for testing only tb 2015-08-05
    static Element getEosElement(String satelliteMeta) throws IOException {
        String localSmmeta = satelliteMeta.replaceAll("\\s+=\\s+", "=");
        localSmmeta = localSmmeta.replaceAll("\\?", "_");

        final StringBuilder sb = new StringBuilder(localSmmeta.length());
        final StringTokenizer lineFinder = new StringTokenizer(localSmmeta, "\t\n\r\f");
        while (lineFinder.hasMoreTokens()) {
            final String line = lineFinder.nextToken().trim();
            sb.append(line);
            sb.append("\n");
        }
        final EosCoreMetaParser parser = new EosCoreMetaParser();
        return parser.parseFromString(sb.toString());
    }

    // package access for testing only tb 2015-08-05
    static String getEosMetadata(String name, Group eosGroup) throws IOException {
        final Variable structMetadataVar = eosGroup.findVariable(name);
        if (structMetadataVar == null) {
            return null;
        }
        final Array metadataArray = structMetadataVar.read();
        return metadataArray.toString();
    }
}
