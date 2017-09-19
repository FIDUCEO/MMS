/*
 * Copyright (C) 2016 Brockmann Consult GmbH
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

package com.bc.fiduceo.post.plugin.hirs.flag;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import com.bc.fiduceo.post.util.DistanceToLandMap;
import org.jdom.Element;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import static com.bc.fiduceo.util.JDomUtils.getMandatoryChild;
import static com.bc.fiduceo.util.JDomUtils.getMandatoryText;

public class HirsL1CloudyFlagsPlugin implements PostProcessingPlugin {

    static final String TAG_POST_PROCESSING_NAME = "hirs-l1-cloudy-flags";
    static final String TAG_SENSOR_NAME = "hirs-sensor-name";
    static final String TAG_VAR_NAME_SOURCE_FILE_NAME = "hirs-var-name-source-file-name";
    static final String TAG_VAR_NAME_PROCESSING_VERSION = "hirs-var-name-processing-version";
    static final String TAG_VAR_NAME_SOURCE_X = "hirs-var-name-source-x";
    static final String TAG_VAR_NAME_SOURCE_Y = "hirs-var-name-source-y";
    static final String TAG_VAR_NAME_SOURCE_BT_11_1_mM = "hirs-var-name-source-11_1-um";

    static final String TAG_VAR_NAME_CLOUD_FLAGS = "hirs-var-name-cloud-flags";
    static final String TAG_VAR_NAME_LATITUDE = "hirs-var-name-latitude";
    static final String TAG_VAR_NAME_LONGITUDE = "hirs-var-name-longitude";
    static final String TAG_VAR_NAME_BT_11_1_uM = "hirs-var-name-11_1-um";
    static final String TAG_VAR_NAME_BT_6_5_uM = "hirs-var-name-6_5-um";
    static final String TAG_DISTANCE_PRODUCT_FILE_PATH = "point_distance-product-file-path";

    private FileSystem fileSystem;

    @Override
    public PostProcessing createPostProcessing(Element element) {
        if (!getPostProcessingName().equals(element.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + getPostProcessingName() + "' expected.");
        }

        final Element bt11_1umVarElem = getMandatoryChild(element, TAG_VAR_NAME_BT_11_1_uM);
        final String btVarName_11_1_um = getMandatoryText(bt11_1umVarElem);

        final Element bt6_5umVarElem = getMandatoryChild(element, TAG_VAR_NAME_BT_6_5_uM);
        final String btVarName_6_5_um = getMandatoryText(bt6_5umVarElem);

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

        final Element sourceBt11_1umElem = getMandatoryChild(element, TAG_VAR_NAME_SOURCE_BT_11_1_mM);
        final String sourceBt11_1umVarName = getMandatoryText(sourceBt11_1umElem);

        final Element distanceVarElem = getMandatoryChild(element, TAG_DISTANCE_PRODUCT_FILE_PATH);
        final String pathString = getMandatoryText(distanceVarElem);
        final Path distanceFilePath;
        final DistanceToLandMap distanceToLandMap;
        if (isTestMode()) {
            distanceToLandMap = null;
        } else {
            distanceFilePath = fileSystem.getPath(pathString);
            distanceToLandMap = new DistanceToLandMap(distanceFilePath);
        }


        return new HirsL1CloudyFlags(sensorName, sourceFileVarName, sourceXVarName, sourceYVarName, processingVersionVarName, sourceBt11_1umVarName, flagVarName, latVarName, lonVarName, btVarName_11_1_um, btVarName_6_5_um,
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

    private boolean isTestMode() {
        return fileSystem != null;
    }
}
