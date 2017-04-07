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

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import com.bc.fiduceo.util.JDomUtils;
import org.jdom.Element;

public class HirsL1CloudyFlagsPlugin implements PostProcessingPlugin {

    public static final String TAG_NAME_HIRS_L1_CLOUDY_FLAGS = "hirs-l1-cloudy-flags";
    public static final String TAG_NAME_BT_11_1_µM_VAR_NAME = "hirs-11_1-um-var-name";
    public static final String TAG_NAME_BT_6_5_µM_VAR_NAME = "hirs-6_5-um-var-name";
    public static final String TAG_NAME_FLAG_VAR_NAME = "hirs-cloud-flags-var-name";

    @Override
    public PostProcessing createPostProcessing(Element element) {
        if (!getPostProcessingName().equals(element.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + getPostProcessingName() + "' expected.");
        }

        final Element bt11_1µmVarElem = JDomUtils.getMandatoryChild(element, TAG_NAME_BT_11_1_µM_VAR_NAME);
        final String btVarName_11_1_µm = JDomUtils.getMandatoryText(bt11_1µmVarElem).trim();

        final Element bt6_5µmVarElem = JDomUtils.getMandatoryChild(element, TAG_NAME_BT_6_5_µM_VAR_NAME);
        final String btVarName_6_5_µm = JDomUtils.getMandatoryText(bt6_5µmVarElem).trim();

        final Element flagsVarElem = JDomUtils.getMandatoryChild(element, TAG_NAME_FLAG_VAR_NAME);
        final String flagVarName = JDomUtils.getMandatoryText(flagsVarElem).trim();

        return new HirsL1CloudyFlags(btVarName_11_1_µm, btVarName_6_5_µm, flagVarName);
    }

    @Override
    public String getPostProcessingName() {
        return TAG_NAME_HIRS_L1_CLOUDY_FLAGS;
    }
}
