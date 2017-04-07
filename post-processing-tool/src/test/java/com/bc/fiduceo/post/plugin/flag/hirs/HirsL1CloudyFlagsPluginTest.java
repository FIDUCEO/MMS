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

package com.bc.fiduceo.post.plugin.flag.hirs;

import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_NAME_BT_11_1_µM_VAR_NAME;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_NAME_BT_6_5_µM_VAR_NAME;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_NAME_FLAG_VAR_NAME;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_NAME_HIRS_L1_CLOUDY_FLAGS;
import static org.junit.Assert.*;

import com.bc.fiduceo.post.PostProcessing;
import org.jdom.Element;
import org.junit.*;

import java.util.Arrays;

public class HirsL1CloudyFlagsPluginTest {

    private HirsL1CloudyFlagsPlugin plugin;
    private Element element;

    @Before
    public void setUp() throws Exception {
        plugin = new HirsL1CloudyFlagsPlugin();
        element = new Element(TAG_NAME_HIRS_L1_CLOUDY_FLAGS).addContent(Arrays.asList(
                    new Element(TAG_NAME_BT_11_1_µM_VAR_NAME).addContent("hirs-n18_bt_ch08"),
                    new Element(TAG_NAME_BT_6_5_µM_VAR_NAME).addContent("hirs-n18_bt_ch12"),
                    new Element(TAG_NAME_FLAG_VAR_NAME).addContent("hirs-n18_cloudy_flags")
        ));
    }

    @Test
    public void testCreatePostProcessing() {
        final PostProcessing postProcessing = plugin.createPostProcessing(element);

        assertNotNull(postProcessing);
        assertEquals("com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlags", postProcessing.getClass().getTypeName());

        final HirsL1CloudyFlags hirsL1CloudyFlags = (HirsL1CloudyFlags) postProcessing;

        assertEquals("hirs-n18_bt_ch08", hirsL1CloudyFlags.btVarName_11_1_µm);
        assertEquals("hirs-n18_bt_ch12", hirsL1CloudyFlags.btVarName_6_5_µm);
        assertEquals("hirs-n18_cloudy_flags", hirsL1CloudyFlags.flagVarName);
    }

    @Test
    public void testCreatePostProcessing_wrongElementName() throws Exception {
        element.setName("wrong_name");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Illegal XML Element. Tagname 'hirs-l1-cloudy-flags' expected.", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_11_1_µm_VarNameTextIsMissing() throws Exception {
        element.getChild(TAG_NAME_BT_11_1_µM_VAR_NAME).setText(null);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'hirs-11_1-um-var-name' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_11_1_µm_VarNameTextIsEmpty() throws Exception {
        element.getChild(TAG_NAME_BT_11_1_µM_VAR_NAME).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'hirs-11_1-um-var-name' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_11_1_µm_VarNameElementIsMissing() throws Exception {
        element.removeChild(TAG_NAME_BT_11_1_µM_VAR_NAME);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element 'hirs-11_1-um-var-name' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_6_5_µm_VarNameTextIsMissing() throws Exception {
        element.getChild(TAG_NAME_BT_6_5_µM_VAR_NAME).setText(null);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'hirs-6_5-um-var-name' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_6_5_µm_VarNameTextIsEmpty() throws Exception {
        element.getChild(TAG_NAME_BT_6_5_µM_VAR_NAME).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'hirs-6_5-um-var-name' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_6_5_µm_VarNameElementIsMissing() throws Exception {
        element.removeChild(TAG_NAME_BT_6_5_µM_VAR_NAME);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element 'hirs-6_5-um-var-name' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_FlagVarNameTextIsMissing() throws Exception {
        element.getChild(TAG_NAME_FLAG_VAR_NAME).setText(null);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'hirs-cloud-flags-var-name' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_FlagVarNameTextIsEmpty() throws Exception {
        element.getChild(TAG_NAME_FLAG_VAR_NAME).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'hirs-cloud-flags-var-name' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_FlagVarNameElementIsMissing() throws Exception {
        element.removeChild(TAG_NAME_FLAG_VAR_NAME);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element 'hirs-cloud-flags-var-name' expected", expected.getMessage());
        }
    }
}
