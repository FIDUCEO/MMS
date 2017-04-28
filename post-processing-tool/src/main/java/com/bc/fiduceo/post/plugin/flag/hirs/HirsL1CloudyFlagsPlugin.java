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
package com.bc.fiduceo.post.plugin.flag.hirs;

import static com.bc.fiduceo.util.JDomUtils.*;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.jdom.Element;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HirsL1CloudyFlagsPlugin implements PostProcessingPlugin {

    public static final String TAG_POST_PROCESSING_NAME = "hirs-l1-cloudy-flags";
    public static final String TAG_SENSOR_NAME = "hirs-sensor-name";
    public static final String TAG_VAR_NAME_SOURCE_FILE_NAME = "hirs-var-name-source-file-name";
    public static final String TAG_VAR_NAME_PROCESSING_VERSION = "hirs-var-name-processing-version";
    public static final String TAG_VAR_NAME_SOURCE_X = "hirs-var-name-source-x";
    public static final String TAG_VAR_NAME_SOURCE_Y = "hirs-var-name-source-y";
    public static final String TAG_VAR_NAME_SOURCE_BT_11_1_mM = "hirs-var-name-source-11_1-um";

    public static final String TAG_VAR_NAME_CLOUD_FLAGS = "hirs-var-name-cloud-flags";
    public static final String TAG_VAR_NAME_LATITUDE = "hirs-var-name-latitude";
    public static final String TAG_VAR_NAME_LONGITUDE = "hirs-var-name-longitude";
    public static final String TAG_VAR_NAME_BT_11_1_µM = "hirs-var-name-11_1-um";
    public static final String TAG_VAR_NAME_BT_6_5_µM = "hirs-var-name-6_5-um";
    public static final String TAG_DISTANCE_PRODUCT_FILE_PATH = "distance-product-file-path";
    private FileSystem fileSystem;

    @Override
    public PostProcessing createPostProcessing(Element element) {
        if (!getPostProcessingName().equals(element.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + getPostProcessingName() + "' expected.");
        }

        final Element bt11_1µmVarElem = getMandatoryChild(element, TAG_VAR_NAME_BT_11_1_µM);
        final String btVarName_11_1_µm = getMandatoryText(bt11_1µmVarElem);

        final Element bt6_5µmVarElem = getMandatoryChild(element, TAG_VAR_NAME_BT_6_5_µM);
        final String btVarName_6_5_µm = getMandatoryText(bt6_5µmVarElem);

        final Element flagsVarElem = getMandatoryChild(element, TAG_VAR_NAME_CLOUD_FLAGS);
        final String flagVarName = getMandatoryText(flagsVarElem);

        final Element latVarElem = getMandatoryChild(element, TAG_VAR_NAME_LATITUDE);
        final String latVarName = getMandatoryText(latVarElem);

        final Element lonVarElem = getMandatoryChild(element, TAG_VAR_NAME_LONGITUDE);
        final String lonVarName = getMandatoryText(lonVarElem);

        final Element sourceVarElem = getMandatoryChild(element, TAG_VAR_NAME_SOURCE_FILE_NAME);
        final String sourceFileVarName = getMandatoryText(sourceVarElem);

        final Element processingVersionElem = getMandatoryChild(element, TAG_VAR_NAME_PROCESSING_VERSION);
        final String processingVersionVarName = getMandatoryText(processingVersionElem);

        final Element sourceXElem = getMandatoryChild(element, TAG_VAR_NAME_SOURCE_X);
        final String sourceXVarName = getMandatoryText(sourceXElem);

        final Element sourceYElem = getMandatoryChild(element, TAG_VAR_NAME_SOURCE_Y);
        final String sourceYVarName = getMandatoryText(sourceYElem);

        final Element sensorNameElem = getMandatoryChild(element, TAG_SENSOR_NAME);
        final String sensorName = getMandatoryText(sensorNameElem);

        final Element sourceBt11_1µmElem = getMandatoryChild(element, TAG_VAR_NAME_SOURCE_BT_11_1_mM);
        final String sourceBt11_1µmVarName = getMandatoryText(sourceBt11_1µmElem);

        final Element distanceVarElem = getMandatoryChild(element, TAG_DISTANCE_PRODUCT_FILE_PATH);
        final String pathString = getMandatoryText(distanceVarElem);
        final Path distanceFilePath;
        if (fileSystem == null) {
            distanceFilePath = Paths.get(pathString);
        } else {
            distanceFilePath = fileSystem.getPath(pathString);
        }
        final DistanceToLandMap distanceToLandMap = new DistanceToLandMap(distanceFilePath);

        return new HirsL1CloudyFlags(sensorName, sourceFileVarName, sourceXVarName, sourceYVarName, processingVersionVarName, sourceBt11_1µmVarName,flagVarName, latVarName, lonVarName, btVarName_11_1_µm, btVarName_6_5_µm,
                                     distanceToLandMap);
    }

    @Override
    public String getPostProcessingName() {
        return TAG_POST_PROCESSING_NAME;
    }

    /**
     * for JUnit level tests only
     *
     * @param fileSystem
     */
    void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
}
