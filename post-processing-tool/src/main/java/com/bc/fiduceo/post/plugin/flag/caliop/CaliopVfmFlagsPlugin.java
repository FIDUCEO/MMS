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
package com.bc.fiduceo.post.plugin.flag.caliop;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.jdom.Element;

public class CaliopVfmFlagsPlugin implements PostProcessingPlugin {

    static final String TAG_POST_PROCESSING_NAME = "caliop-L2-vfm-flags";
//    static final String TAG_TARGET_FCF_VARIABLE_NAME = "target-fcf-variable-name";
//    static final String TAG_TARGET_FCF_DIMENSION_NAME = "target-fcf-dimension-name";
//    static final String TAG_SOURCE_FILE_VARIABE_NAME = "target-fcf-dimension-name";


    @Override
    public PostProcessing createPostProcessing(Element element) {
        return new CaliopVfmFlags();
    }

    @Override
    public String getPostProcessingName() {
        return TAG_POST_PROCESSING_NAME;
    }
}
