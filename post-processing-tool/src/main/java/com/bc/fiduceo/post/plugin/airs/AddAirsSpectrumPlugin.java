/*
 * Copyright (C) 2018 Brockmann Consult GmbH
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
package com.bc.fiduceo.post.plugin.airs;

import static com.bc.fiduceo.util.JDomUtils.getMandatoryChildMandatoryTextTrim;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.jdom.Element;

public class AddAirsSpectrumPlugin implements PostProcessingPlugin {

    static final String TAG_POST_PROCESSING_ELEMENT_NAME = "add-airs-channel-data";
    static final String TAG_MMD_SOURCE_FILE_VARIABE_NAME = "mmd-source-file-variable-name";
    static final String TAG_MMD_PROCESSING_VERSION_VARIABE_NAME = "mmd-processing-version-variable-name";
    static final String TAG_MMD_X_VARIABE_NAME = "mmd-x-variable-name";
    static final String TAG_MMD_Y_VARIABE_NAME = "mmd-y-variable-name";
    static final String TAG_MMD_VARIABLE_NAME_CUT_OUT_REFERENCE = "mmd-variable-name-cut-out-reference";
    static final String TAG_TARGET_VARIABLE_NAME_RADIANCES = "target-variable-name-radiances";
    static final String TAG_TARGET_VARIABLE_NAME_CalFlag = "target_variable_name_CalFlag";
    static final String TAG_TARGET_VARIABLE_NAME_SpaceViewDelta = "target_variable_name_SpaceViewDelta";



//    (GeoTrack=135, Channel=2378);

    @Override
    public PostProcessing createPostProcessing(Element element) {
        if (!getPostProcessingName().equals(element.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + getPostProcessingName() + "' expected.");
        }


        final String srcVariableName_fileName = getMandatoryChildMandatoryTextTrim(element, TAG_MMD_SOURCE_FILE_VARIABE_NAME);
        final String srcVariableName_processingVersion = getMandatoryChildMandatoryTextTrim(element, TAG_MMD_PROCESSING_VERSION_VARIABE_NAME);
        final String srcVariableName_x = getMandatoryChildMandatoryTextTrim(element, TAG_MMD_X_VARIABE_NAME);
        final String srcVariableName_y = getMandatoryChildMandatoryTextTrim(element, TAG_MMD_Y_VARIABE_NAME);
        final String srcVariableName_cutOutReference = getMandatoryChildMandatoryTextTrim(element, TAG_MMD_VARIABLE_NAME_CUT_OUT_REFERENCE);
        final String targetRadiancesVariableName = getMandatoryChildMandatoryTextTrim(element, TAG_TARGET_VARIABLE_NAME_RADIANCES);
        final String targetCalFlagVariableName = getMandatoryChildMandatoryTextTrim(element, TAG_TARGET_VARIABLE_NAME_CalFlag);
        final String targetSpaceViewDeltaVariableName = getMandatoryChildMandatoryTextTrim(element, TAG_TARGET_VARIABLE_NAME_SpaceViewDelta);

        return new AddAirsSpectrum(srcVariableName_fileName, srcVariableName_processingVersion,
                                   srcVariableName_x,srcVariableName_y, srcVariableName_cutOutReference,
                                   targetRadiancesVariableName, targetCalFlagVariableName,
                                   targetSpaceViewDeltaVariableName);
    }

    @Override
    public String getPostProcessingName() {
        return TAG_POST_PROCESSING_ELEMENT_NAME;
    }
}
