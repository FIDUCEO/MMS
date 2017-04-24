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

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HirsL1CloudyFlagsPlugin implements PostProcessingPlugin {

    public static final String TAG_NAME_POST_PROCESSING_NAME = "hirs-l1-cloudy-flags";
    public static final String TAG_NAME_BT_11_1_µM_VAR_NAME = "hirs-11_1-um-var-name";
    public static final String TAG_NAME_BT_6_5_µM_VAR_NAME = "hirs-6_5-um-var-name";
    public static final String TAG_NAME_FLAG_VAR_NAME = "hirs-cloud-flags-var-name";
    public static final String TAG_NAME_LATITUDE_VAR_NAME = "hirs-latitude-var-name";
    public static final String TAG_NAME_LONGITUDE_VAR_NAME = "hirs-longitude-var-name";
    public static final String TAG_NAME_DISTANCE_PRODUCT_FILE_PATH = "distance-product-file-path";
    private FileSystem fileSystem;

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

        final Element latVarElem = JDomUtils.getMandatoryChild(element, TAG_NAME_LATITUDE_VAR_NAME);
        final String latVarName = JDomUtils.getMandatoryText(latVarElem).trim();

        final Element lonVarElem = JDomUtils.getMandatoryChild(element, TAG_NAME_LONGITUDE_VAR_NAME);
        final String lonVarName = JDomUtils.getMandatoryText(lonVarElem).trim();

        final Element distanceVarElem = JDomUtils.getMandatoryChild(element, TAG_NAME_DISTANCE_PRODUCT_FILE_PATH);
        final String pathString = JDomUtils.getMandatoryText(distanceVarElem).trim();
        final Path distanceFilePath;
        if (fileSystem == null) {
            distanceFilePath = Paths.get(pathString);
        } else {
            distanceFilePath = fileSystem.getPath(pathString);
        }
        final DistanceToLandMap distanceToLandMap = new DistanceToLandMap(distanceFilePath);

        return new HirsL1CloudyFlags(btVarName_11_1_µm, btVarName_6_5_µm, flagVarName, latVarName, lonVarName, distanceToLandMap);
    }

    @Override
    public String getPostProcessingName() {
        return TAG_NAME_POST_PROCESSING_NAME;
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
