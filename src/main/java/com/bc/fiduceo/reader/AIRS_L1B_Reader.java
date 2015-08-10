package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.esa.snap.framework.datamodel.ProductData;
import org.jdom2.Element;
import ucar.ma2.Array;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

public class AIRS_L1B_Reader implements Reader {
    private static final String RANGE_BEGINNING_DATE = "RANGEBEGINNINGDATE";
    private static final String RANGE_ENDING_DATE = "RANGEENDINGDATE";

    private static final String RANGE_BEGINNING_TIME = "RANGEBEGINNINGTIME";
    private static final String RANGE_ENDING_TIME = "RANGEENDINGTIME";

    private static final String CORE_METADATA = "coremetadata";
    private static final String ASSOCIATED_SENSORSHORT_NAME = "ASSOCIATEDSENSORSHORTNAME";
    private Element eosElement;
    private static volatile String value;
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

    public SatelliteObservation read() throws IOException, ParseException {
        String rangeBeginningDate = getElementValue(eosElement, RANGE_BEGINNING_DATE) + " " + getElementValue(eosElement, RANGE_BEGINNING_TIME);
        String rangeEndingDate = getElementValue(eosElement, RANGE_ENDING_DATE) + " " + getElementValue(eosElement, RANGE_ENDING_TIME);

        final SatelliteObservation satelliteObservation = new SatelliteObservation();
        DateFormat dateFormat = ProductData.UTC.createDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

        satelliteObservation.setStartTime(dateFormat.parse(rangeBeginningDate));
        satelliteObservation.setStopTime(dateFormat.parse(rangeEndingDate));

        Sensor sensor = new Sensor();
        sensor.setName(getElementValue(eosElement, ASSOCIATED_SENSORSHORT_NAME));
        satelliteObservation.setSensor(sensor);

        satelliteObservation.setGeoBounds(getGeoCoordinate());
        return satelliteObservation;
    }


    private Geometry getGeoCoordinate() throws IOException {
        Array arrayLat = null;
        Array arrayLon = null;
        List<Variable> variables = netcdfFile.getVariables();
        for (Variable variable : variables) {
            if (variable.getShortName().startsWith("sat_lat")) {
                arrayLat = variable.read();
            }

            if (variable.getShortName().startsWith("sat_lon")) {
                arrayLon = variable.read();
            }
        }

        double[] geoLat = (double[]) Objects.requireNonNull(arrayLat).get1DJavaArray(double.class);
        double[] geoLon = (double[]) Objects.requireNonNull(arrayLon).get1DJavaArray(double.class);

        List<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < geoLat.length; i++) {
            coordinates.add(new Coordinate(geoLat[i], geoLon[i]));
        }
        return new GeometryFactory().createMultiPoint(coordinates.toArray(new Coordinate[coordinates.size()]));
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
