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
 */

package com.bc.fiduceo.post.plugin.distance;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import com.bc.fiduceo.util.JDomUtils;
import org.jdom.Element;

public class SphericalDistancePlugin implements PostProcessingPlugin {

    public static final String TAG_NAME_SPHERICAL_DISTANCE = "spherical-distance";
    public static final String TAG_NAME_TARGET = "target";
    public static final String TAG_NAME_DATA_TYPE = "data-type";
    public static final String TAG_NAME_VAR_NAME = "var-name";
    public static final String TAG_NAME_DIM_NAME = "dim-name";
    public static final String TAG_NAME_PRIM_LAT_VAR = "primary-lat-variable";
    public static final String TAG_NAME_PRIM_LON_VAR = "primary-lon-variable";
    public static final String TAG_NAME_SECO_LAT_VAR = "secondary-lat-variable";
    public static final String TAG_NAME_SECO_LON_VAR = "secondary-lon-variable";
    public static final String SCALE_ATTR_NAME = "scaleAttrName";
    public static final String OFFSET_ATTR_NAME = "offsetAttrName";

    @Override
    public PostProcessing createPostProcessing(final Element element) {
        if (!getPostProcessingName().equals(element.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + getPostProcessingName() + "' expected.");
        }
        final Element target = JDomUtils.getMandatoryChild(element, TAG_NAME_TARGET);
        final String targetVarName = JDomUtils.getMandatoryChildMandatoryTextTrim(target, TAG_NAME_VAR_NAME);
        final String targetDataType = JDomUtils.getMandatoryChildMandatoryTextTrim(target, TAG_NAME_DATA_TYPE);
        final String targetDimName = JDomUtils.getMandatoryChildMandatoryTextTrim(target, TAG_NAME_DIM_NAME);

        final Element primLatVar = JDomUtils.getMandatoryChild(element, TAG_NAME_PRIM_LAT_VAR);
        final String primLatVarName = JDomUtils.getMandatoryText(primLatVar).trim();
        final String primeLatScaleAttrName = primLatVar.getAttributeValue(SCALE_ATTR_NAME);
        final String primeLatOffsetAttrName = primLatVar.getAttributeValue(OFFSET_ATTR_NAME);

        final Element primLonVar = JDomUtils.getMandatoryChild(element, TAG_NAME_PRIM_LON_VAR);
        final String primLonVarName = JDomUtils.getMandatoryText(primLonVar).trim();
        final String primeLonScaleAttrName = primLonVar.getAttributeValue(SCALE_ATTR_NAME);
        final String primeLonOffsetAttrName = primLonVar.getAttributeValue(OFFSET_ATTR_NAME);

        final Element secoLatVar = JDomUtils.getMandatoryChild(element, TAG_NAME_SECO_LAT_VAR);
        final String secoLatVarName = JDomUtils.getMandatoryText(secoLatVar).trim();
        final String secoLatScaleAttrName = secoLatVar.getAttributeValue(SCALE_ATTR_NAME);
        final String secoLatOffsetAttrName = secoLatVar.getAttributeValue(OFFSET_ATTR_NAME);

        final Element secoLonVar = JDomUtils.getMandatoryChild(element, TAG_NAME_SECO_LON_VAR);
        final String secoLonVarName = JDomUtils.getMandatoryText(secoLonVar).trim();
        final String secoLonScaleAttrName = secoLonVar.getAttributeValue(SCALE_ATTR_NAME);
        final String secoLonOffsetAttrName = secoLonVar.getAttributeValue(OFFSET_ATTR_NAME);

        return new SphericalDistance(targetVarName, targetDataType, targetDimName,
                                     primLatVarName, primeLatScaleAttrName, primeLatOffsetAttrName,
                                     primLonVarName, primeLonScaleAttrName, primeLonOffsetAttrName,
                                     secoLatVarName, secoLatScaleAttrName, secoLatOffsetAttrName,
                                     secoLonVarName, secoLonScaleAttrName, secoLonOffsetAttrName);
    }

    @Override
    public String getPostProcessingName() {
        return TAG_NAME_SPHERICAL_DISTANCE;
    }
}
