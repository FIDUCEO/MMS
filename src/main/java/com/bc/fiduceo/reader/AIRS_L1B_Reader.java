package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import org.esa.snap.framework.datamodel.ProductData;
import org.esa.snap.util.StringUtils;
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
import java.util.List;
import java.util.StringTokenizer;

public class AIRS_L1B_Reader implements Reader {

    private static final String RANGE_BEGINNING_DATE = "RANGEBEGINNINGDATE";
    private static final String RANGE_ENDING_DATE = "RANGEENDINGDATE";
    private static final String RANGE_BEGINNING_TIME = "RANGEBEGINNINGTIME";
    private static final String RANGE_ENDING_TIME = "RANGEENDINGTIME";
    private static final String CORE_METADATA = "coremetadata";
    private static final String ASSOCIATED_SENSORSHORT_NAME = "ASSOCIATEDSENSORSHORTNAME";
    private static final DateFormat DATEFORMAT = ProductData.UTC.createDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

    private static final int GEO_INTERVAL_X = 12;
    private static final int GEO_INTERVAL_Y = 12;

    private NetcdfFile netcdfFile;
    private BoundingPolygonCreator boundingPolygonCreator;

    public AIRS_L1B_Reader() {
        boundingPolygonCreator = new BoundingPolygonCreator(GEO_INTERVAL_X, GEO_INTERVAL_Y);
    }

    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
    }

    public void close() throws IOException {
        netcdfFile.close();
    }

    public AcquisitionInfo read() throws IOException {
        final Group rootGroup = netcdfFile.getRootGroup();
        final String coreMateString = getEosMetadata(CORE_METADATA, rootGroup);
        final Element eosElement = getEosElement(coreMateString);

        final String rangeBeginningDate = getElementValue(eosElement, RANGE_BEGINNING_DATE) + " " + getElementValue(eosElement, RANGE_BEGINNING_TIME);
        final String rangeEndingDate = getElementValue(eosElement, RANGE_ENDING_DATE) + " " + getElementValue(eosElement, RANGE_ENDING_TIME);
        final SatelliteObservation satelliteObservation = new SatelliteObservation();

        try {
            satelliteObservation.setStartTime(DATEFORMAT.parse(rangeBeginningDate));
            satelliteObservation.setStopTime(DATEFORMAT.parse(rangeEndingDate));
        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        }

        final Sensor sensor = new Sensor();
        sensor.setName(getElementValue(eosElement, ASSOCIATED_SENSORSHORT_NAME));
        satelliteObservation.setSensor(sensor);
        final NodeType nodeType = readNodeType();
        satelliteObservation.setNodeType(nodeType);

        final Group l1bAirsGroup = rootGroup.findGroup("L1B_AIRS_Science");
        if (l1bAirsGroup == null) {
            throw new IOException("'L1B_AIRS_Science' data group not found");
        }
        final Group geolocationFields = l1bAirsGroup.findGroup("Geolocation_Fields");
        final Variable latitudeVariable = geolocationFields.findVariable("Latitude");
        final Variable longitudeVariable = geolocationFields.findVariable("Longitude");
        final Array latitudes = latitudeVariable.read();
        final Array longitudes = longitudeVariable.read();

        return boundingPolygonCreator.createPixelCodedBoundingPolygon((ArrayDouble.D2) latitudes, (ArrayDouble.D2) longitudes, nodeType);
    }


    static String getElementValue(Element element, String attribute) {
        if (element.getName().equals(attribute)) {
            return element.getChild("VALUE").getValue();
        }
        for (Element subElement : element.getChildren()) {
            if (subElement.getName().equals(attribute)) {
                return subElement.getChild("VALUE").getValue();
            } else {
                final String elementValue = getElementValue(subElement, attribute);
                if (StringUtils.isNotNullAndNotEmpty(elementValue)) {
                    return elementValue;
                }
            }
        }
        return null;
    }

    // package access for testing only tb 2015-08-05
    static Element getEosElement(String satelliteMeta) throws IOException {
        String trimmedMetaString = satelliteMeta.replaceAll("\\s+=\\s+", "=");
        trimmedMetaString = trimmedMetaString.replaceAll("\\?", "_");

        final StringBuilder sb = new StringBuilder(trimmedMetaString.length());
        final StringTokenizer lineFinder = new StringTokenizer(trimmedMetaString, "\t\n\r\f");
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

    private NodeType readNodeType()  {
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
            // @todo 2 tb/tb add logging here 2015-09-07
            return NodeType.UNDEFINED;
        }

        return NodeType.fromId(nodeType.equals("Ascending") ? 0 : 1);
    }
}
