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

import com.bc.fiduceo.post.PostProcessing;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

import static com.bc.fiduceo.post.plugin.caliop.sst_wp100.CALIOP_SST_WP100_CLay_PPPlugin.*;
import static org.junit.Assert.*;

public class CALIOP_SST_WP100_CLay_PPPluginTest {

    private CALIOP_SST_WP100_CLay_PPPlugin ppPlugin;

    @Before
    public void setUp() {
        ppPlugin = new CALIOP_SST_WP100_CLay_PPPlugin();
    }

    @Test
    public void tagNames() {
        assertEquals("caliop-sst-wp100-clay", TAG_POST_PROCESSING_NAME);
        assertEquals("mmd-source-file-variable-name", TAG_MMD_SOURCE_FILE_VARIABE_NAME);
        assertEquals("processing-version", TAG_MMD_PROCESSING_VERSION);
        assertEquals("mmd-y-variable-name", TAG_MMD_Y_VARIABE_NAME);
        assertEquals("target-variable-prefix", TAG_TARGET_VARIABE_PREFIX);
    }

    @Test
    public void getPostProcessingName() {
        assertEquals("caliop-sst-wp100-clay", ppPlugin.getPostProcessingName());
    }

    @Test
    public void createPostProcessing() {
        final PostProcessing pp = ppPlugin.createPostProcessing(createValidRootElement());
        assertNotNull(pp);
        assertEquals(CALIOP_SST_WP100_CLay_PP.class, pp.getClass());
        final CALIOP_SST_WP100_CLay_PP cwp100_CLay_pp = (CALIOP_SST_WP100_CLay_PP) pp;
        assertEquals("caliop_vfm-cal_file_name", cwp100_CLay_pp.variableName_caliopVFM_fileName);
        assertEquals("4.10", cwp100_CLay_pp.processingVersion);
        assertEquals("caliop_vfm-cal_y", cwp100_CLay_pp.variableName_caliopVFM_y);
        assertEquals("caliop_clay.", cwp100_CLay_pp.variablePrefix);
    }

    @Test
    public void createPostProcessing_wrongRootTag() {
        final Element rootElement = createValidRootElement();
        try {
            ppPlugin.createPostProcessing(rootElement.setName("wrongRootName"));
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals(RuntimeException.class.getTypeName(), expected.getClass().getTypeName());
            assertEquals("Illegal XML Element. Tagname '" + TAG_POST_PROCESSING_NAME + "' expected.", expected.getMessage());
        }
    }

    @Test
    public void createPostProcessing_sourceFileVarName_missingElement() {
        final Element rootElement = createValidRootElement();
        rootElement.removeChild(TAG_MMD_SOURCE_FILE_VARIABE_NAME);
        try {
            ppPlugin.createPostProcessing(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals(RuntimeException.class.getTypeName(), expected.getClass().getTypeName());
            assertEquals("Child element '" + TAG_MMD_SOURCE_FILE_VARIABE_NAME + "' expected", expected.getMessage());
        }
    }

    @Test
    public void createPostProcessing_sourceFileVarName_empty() {
        final Element rootElement = createValidRootElement();
        rootElement.getChild(TAG_MMD_SOURCE_FILE_VARIABE_NAME).setText("   ");
        try {
            ppPlugin.createPostProcessing(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals(RuntimeException.class.getTypeName(), expected.getClass().getTypeName());
            assertEquals("Value of element '" + TAG_MMD_SOURCE_FILE_VARIABE_NAME + "' expected", expected.getMessage());
        }
    }


    @Test
    public void createPostProcessing_processingVersionVarName_missingElement() {
        final Element rootElement = createValidRootElement();
        rootElement.removeChild(TAG_MMD_PROCESSING_VERSION);
        try {
            ppPlugin.createPostProcessing(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals(RuntimeException.class.getTypeName(), expected.getClass().getTypeName());
            assertEquals("Child element '" + TAG_MMD_PROCESSING_VERSION + "' expected", expected.getMessage());
        }
    }

    @Test
    public void createPostProcessing_processingVersionVarName_empty() {
        final Element rootElement = createValidRootElement();
        rootElement.getChild(TAG_MMD_PROCESSING_VERSION).setText("   ");
        try {
            ppPlugin.createPostProcessing(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals(RuntimeException.class.getTypeName(), expected.getClass().getTypeName());
            assertEquals("Value of element '" + TAG_MMD_PROCESSING_VERSION + "' expected", expected.getMessage());
        }
    }

    @Test
    public void createPostProcessing_yVarName_missingElement() {
        final Element rootElement = createValidRootElement();
        rootElement.removeChild(TAG_MMD_Y_VARIABE_NAME);
        try {
            ppPlugin.createPostProcessing(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals(RuntimeException.class.getTypeName(), expected.getClass().getTypeName());
            assertEquals("Child element '" + TAG_MMD_Y_VARIABE_NAME + "' expected", expected.getMessage());
        }
    }

    @Test
    public void createPostProcessing_yVarName_empty() {
        final Element rootElement = createValidRootElement();
        rootElement.getChild(TAG_MMD_Y_VARIABE_NAME).setText("   ");
        try {
            ppPlugin.createPostProcessing(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals(RuntimeException.class.getTypeName(), expected.getClass().getTypeName());
            assertEquals("Value of element '" + TAG_MMD_Y_VARIABE_NAME + "' expected", expected.getMessage());
        }
    }

    @Test
    public void createPostProcessing_targetPrefix_missingElement() {
        final Element rootElement = createValidRootElement();
        rootElement.removeChild(TAG_TARGET_VARIABE_PREFIX);
        try {
            ppPlugin.createPostProcessing(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals(RuntimeException.class.getTypeName(), expected.getClass().getTypeName());
            assertEquals("Child element '" + TAG_TARGET_VARIABE_PREFIX + "' expected", expected.getMessage());
        }
    }

    @Test
    public void createPostProcessing_targetPrefix_empty() {
        final Element rootElement = createValidRootElement();
        rootElement.getChild(TAG_TARGET_VARIABE_PREFIX).setText("   ");
        try {
            ppPlugin.createPostProcessing(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals(RuntimeException.class.getTypeName(), expected.getClass().getTypeName());
            assertEquals("Value of element '" + TAG_TARGET_VARIABE_PREFIX + "' expected", expected.getMessage());
        }
    }

    private Element createValidRootElement() {
        final Element root = new Element(TAG_POST_PROCESSING_NAME);
        root.addContent(new Element(TAG_MMD_SOURCE_FILE_VARIABE_NAME).setText("caliop_vfm-cal_file_name"));
        root.addContent(new Element(TAG_MMD_PROCESSING_VERSION).setText("4.10"));
        root.addContent(new Element(TAG_MMD_Y_VARIABE_NAME).setText("caliop_vfm-cal_y"));
        root.addContent(new Element(TAG_TARGET_VARIABE_PREFIX).setText("caliop_clay."));
        return root;
    }
}