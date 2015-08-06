package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.parse.ParseReader;
import org.jdom2.Element;
import ucar.ma2.Array;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.StringTokenizer;

public class AIRS_L1B_Reader implements Reader {
    private static final String CORE_METADATA = "coremetadata";
    private Element eosElement;
    private static volatile String value;


    private NetcdfFile netcdfFile;

    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
        final Group eosGroup = netcdfFile.getRootGroup();
        final String coreMateString = getEosMetadata(CORE_METADATA, eosGroup);

        eosElement = getEosElement(coreMateString);
        readVariable();
    }

    public void close() throws IOException {
        netcdfFile.close();
    }

    public SatelliteObservation read() throws IOException, ParseException {
        String rangeBeginningDate = getElementValue(eosElement, "RANGEBEGINNINGDATE") + " " + getElementValue(eosElement, "RANGEBEGINNINGTIME");
        String rangeEndingDate = getElementValue(eosElement, "RANGEENDINGDATE") + " " + getElementValue(eosElement, "RANGEENDINGTIME");

        final SatelliteObservation satelliteObservation = new SatelliteObservation();
        satelliteObservation.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rangeBeginningDate));
        satelliteObservation.setStopTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rangeEndingDate));

        Sensor sensor = new Sensor();
        sensor.setName(getElementValue(eosElement, "ASSOCIATEDSENSORSHORTNAME"));
        satelliteObservation.setSensor(sensor);
        return satelliteObservation;
    }

    private void readVariable() throws IOException {
        List<Variable> variables = netcdfFile.getVariables();
        for (Variable variable : variables) {
            if (variable.getShortName().startsWith("sat_lon")) {
                variable.findAttribute("flag_band");
                variable.getElementSize();
                variable.getDescription();
            }
        }
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

        final ParseReader parser = new ParseReader();
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
