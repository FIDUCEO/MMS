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

package com.bc.fiduceo.post.plugin.caliop.sst_wp100;

import static com.bc.fiduceo.util.JDomUtils.getMandatoryChildMandatoryTextTrim;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.jdom.Element;

public class CALIOP_SST_WP100_CLay_PPPlugin implements PostProcessingPlugin {

    static final String TAG_POST_PROCESSING_NAME = "caliop-sst-wp100-clay";
    static final String TAG_MMD_SOURCE_FILE_VARIABE_NAME = "mmd-source-file-variable-name";
    static final String TAG_MMD_PROCESSING_VERSION = "processing-version";
    static final String TAG_MMD_Y_VARIABE_NAME = "mmd-y-variable-name";
    static final String TAG_TARGET_VARIABE_PREFIX = "target-variable-prefix";


    @Override
    public PostProcessing createPostProcessing(Element element) {
        if (!getPostProcessingName().equals(element.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + getPostProcessingName() + "' expected.");
        }

        final String srcVariableName_fileName = getMandatoryChildMandatoryTextTrim(element, TAG_MMD_SOURCE_FILE_VARIABE_NAME);
        final String processingVersion = getMandatoryChildMandatoryTextTrim(element, TAG_MMD_PROCESSING_VERSION);
        final String y_variableName = getMandatoryChildMandatoryTextTrim(element, TAG_MMD_Y_VARIABE_NAME);
        final String variablePrefix = getMandatoryChildMandatoryTextTrim(element, TAG_TARGET_VARIABE_PREFIX);

        return new CALIOP_SST_WP100_CLay_PP(srcVariableName_fileName, y_variableName, processingVersion, variablePrefix);
    }

    @Override
    public String getPostProcessingName() {
        return TAG_POST_PROCESSING_NAME;
    }
}
