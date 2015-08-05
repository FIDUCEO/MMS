package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.SatelliteObservation;
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
import java.util.Date;
import java.util.Iterator;
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
    }

    public void close() throws IOException {
        netcdfFile.close();
    }

    public SatelliteObservation read() throws IOException, ParseException {
        String dateRange = getElementValue(eosElement, "RANGEENDINGDATE");
        final SatelliteObservation satelliteObservation = new SatelliteObservation();
        satelliteObservation.setStopTime(new SimpleDateFormat("yyyy-MM-dd").parse(dateRange));
        return satelliteObservation;
    }


    static String getElementValue(Element element, String attribute) {
        Iterator children = element.getChildren().iterator();
        while (children.hasNext()) {
            Element subElement = (Element) children.next();
            if (subElement.getName().equals(attribute)) {
                value = subElement.getChild("VALUE").getValue();
            } else {
                getElementValue(subElement, attribute);
            }
        }
        return value;
    }


    // package access for testing only tb 2015-08-05
    static Element getEosElement(String satelliteMeta) throws IOException {
        String localSmmeta = satelliteMeta.replaceAll("\\s+=\\s+", "=");
        localSmmeta = localSmmeta.replaceAll("\\?", "_"); // XML names cannot contain the character "?".

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
