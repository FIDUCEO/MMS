/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.hdf;

import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.util.StringUtils;
import org.jdom2.Element;
import ucar.ma2.Array;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;

public class HdfEOSUtil {

    public static final String CORE_METADATA = "coremetadata";
    public static final String RANGE_BEGINNING_DATE = "RANGEBEGINNINGDATE";
    public static final String RANGE_ENDING_DATE = "RANGEENDINGDATE";
    public static final String RANGE_BEGINNING_TIME = "RANGEBEGINNINGTIME";
    public static final String RANGE_ENDING_TIME = "RANGEENDINGTIME";

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.S";

    public static String getEosMetadata(String name, Group eosGroup) throws IOException {
        final Variable structMetadataVar = eosGroup.findVariable(name);
        if (structMetadataVar == null) {
            return null;
        }
        final Array metadataArray = structMetadataVar.read();
        return metadataArray.toString();
    }

    public static Element getEosElement(String satelliteMeta) {
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

    public static String getElementValue(Element element, String attribute) {
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

    public static Date parseDate(String dateString, String timeString) {
        final String timeStringWithMillis = stripMicroSecs(timeString);
        final String rangeBeginningDate = dateString + " " + timeStringWithMillis;
        return TimeUtils.parse(rangeBeginningDate, DATE_FORMAT);
    }

    // @todo 4 tb/** add explicit tests - method is implicitely tested in the MODIS IO tests 2020-05-13
    public static void extractAcquisitionTimes(AcquisitionInfo acquisitionInfo, NetcdfFile netcdfFile) throws IOException {
        final Group rootGroup = netcdfFile.getRootGroup();
        final String coreMetaString = HdfEOSUtil.getEosMetadata("CoreMetadata.0", rootGroup);
        if (coreMetaString == null) {
            throw new IOException("'CoreMetadata.0' attribute not found");
        }
        final Element eosElement = HdfEOSUtil.getEosElement(coreMetaString);
        final String rangeBeginDateElement = HdfEOSUtil.getElementValue(eosElement, HdfEOSUtil.RANGE_BEGINNING_DATE);
        final String rangeBeginTimeElement = HdfEOSUtil.getElementValue(eosElement, HdfEOSUtil.RANGE_BEGINNING_TIME);
        final Date sensingStart = HdfEOSUtil.parseDate(rangeBeginDateElement, rangeBeginTimeElement);
        acquisitionInfo.setSensingStart(sensingStart);

        final String rangeEndDateElement = HdfEOSUtil.getElementValue(eosElement, HdfEOSUtil.RANGE_ENDING_DATE);
        final String rangeEndTimeElement = HdfEOSUtil.getElementValue(eosElement, HdfEOSUtil.RANGE_ENDING_TIME);
        final Date sensingStop = HdfEOSUtil.parseDate(rangeEndDateElement, rangeEndTimeElement);
        acquisitionInfo.setSensingStop(sensingStop);
    }

    // package access for testing only tb 2017-05-29
    static String stripMicroSecs(String timeString) {
        final int lastDotIndex = timeString.lastIndexOf('.');
        return timeString.substring(0, lastDotIndex + 4);
    }
}
