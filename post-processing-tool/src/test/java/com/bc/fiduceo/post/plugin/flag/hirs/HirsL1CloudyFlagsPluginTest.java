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

import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_DISTANCE_PRODUCT_FILE_PATH;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_POST_PROCESSING_NAME;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_SENSOR_NAME;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_VAR_NAME_BT_11_1_uM;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_VAR_NAME_BT_6_5_uM;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_VAR_NAME_CLOUD_FLAGS;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_VAR_NAME_LATITUDE;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_VAR_NAME_LONGITUDE;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_VAR_NAME_PROCESSING_VERSION;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_VAR_NAME_SOURCE_BT_11_1_mM;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_VAR_NAME_SOURCE_FILE_NAME;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_VAR_NAME_SOURCE_X;
import static com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlagsPlugin.TAG_VAR_NAME_SOURCE_Y;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.util.DistanceToLandMap;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.jdom.Element;
import org.junit.*;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class HirsL1CloudyFlagsPluginTest {

    private HirsL1CloudyFlagsPlugin plugin;
    private Element element;
    private FileSystem virtualFS;

    @Before
    public void setUp() throws Exception {
        plugin = new HirsL1CloudyFlagsPlugin();
        element = new Element(TAG_POST_PROCESSING_NAME).addContent(Arrays.asList(
                    new Element(TAG_SENSOR_NAME).addContent("hirs-n18"),
                    new Element(TAG_VAR_NAME_SOURCE_FILE_NAME).addContent("hirs-n18_file_name"),
                    new Element(TAG_VAR_NAME_SOURCE_X).addContent("hirs-n18_x"),
                    new Element(TAG_VAR_NAME_SOURCE_Y).addContent("hirs-n18_y"),
                    new Element(TAG_VAR_NAME_PROCESSING_VERSION).addContent("hirs-n18_processing_version"),
                    new Element(TAG_VAR_NAME_SOURCE_BT_11_1_mM).addContent("bt_ch08"),

                    new Element(TAG_VAR_NAME_CLOUD_FLAGS).addContent("hirs-n18_cloudy_flags"),
                    new Element(TAG_VAR_NAME_LATITUDE).addContent("hirs-n18_lat"),
                    new Element(TAG_VAR_NAME_LONGITUDE).addContent("hirs-n18_lon"),
                    new Element(TAG_VAR_NAME_BT_11_1_uM).addContent("hirs-n18_bt_ch08"),
                    new Element(TAG_VAR_NAME_BT_6_5_uM).addContent("hirs-n18_bt_ch12"),
                    new Element(TAG_DISTANCE_PRODUCT_FILE_PATH).addContent("/path/to/the/point_distance-NetCDF-file.nc")
        ));
        virtualFS = Jimfs.newFileSystem(Configuration.unix());
        final Path file = virtualFS.getPath("/path/to/the/point_distance-NetCDF-file.nc");
        Files.createDirectories(file.getParent());
        Files.write(file, "Da steht was drin".getBytes());
    }

    @Test
    public void testCreatePostProcessing() {
        plugin.setFileSystem(virtualFS);
        final PostProcessing postProcessing = plugin.createPostProcessing(element);

        assertNotNull(postProcessing);
        assertEquals("com.bc.fiduceo.post.plugin.flag.hirs.HirsL1CloudyFlags", postProcessing.getClass().getTypeName());

        final HirsL1CloudyFlags hirsL1CloudyFlags = (HirsL1CloudyFlags) postProcessing;

        assertEquals("hirs-n18", hirsL1CloudyFlags.sensorName);
        assertEquals("hirs-n18_file_name", hirsL1CloudyFlags.sourceFileVarName);
        assertEquals("hirs-n18_x", hirsL1CloudyFlags.sourceXVarName);
        assertEquals("hirs-n18_y", hirsL1CloudyFlags.sourceYVarName);
        assertEquals("hirs-n18_processing_version", hirsL1CloudyFlags.processingVersionVarName);
        assertEquals("bt_ch08", hirsL1CloudyFlags.sourceBt_11_1_um_VarName);

        assertEquals("hirs-n18_cloudy_flags", hirsL1CloudyFlags.flagVarName);
        assertEquals("hirs-n18_lat", hirsL1CloudyFlags.latVarName);
        assertEquals("hirs-n18_lon", hirsL1CloudyFlags.lonVarName);
        assertEquals("hirs-n18_bt_ch08", hirsL1CloudyFlags.bt_11_1_um_VarName);
        assertEquals("hirs-n18_bt_ch12", hirsL1CloudyFlags.bt_6_5_um_VarName);
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
    public void testCreatePostProcessing_11_1_um_VarNameTextIsMissing() throws Exception {
        element.getChild(TAG_VAR_NAME_BT_11_1_uM).setText(null);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_BT_11_1_uM + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_11_1_um_VarNameTextIsEmpty() throws Exception {
        element.getChild(TAG_VAR_NAME_BT_11_1_uM).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_BT_11_1_uM + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_11_1_um_VarNameElementIsMissing() throws Exception {
        element.removeChild(TAG_VAR_NAME_BT_11_1_uM);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element '" + TAG_VAR_NAME_BT_11_1_uM + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_6_5_um_VarNameTextIsMissing() throws Exception {
        element.getChild(TAG_VAR_NAME_BT_6_5_uM).setText(null);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_BT_6_5_uM + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_6_5_um_VarNameTextIsEmpty() throws Exception {
        element.getChild(TAG_VAR_NAME_BT_6_5_uM).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_BT_6_5_uM + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_6_5_um_VarNameElementIsMissing() throws Exception {
        element.removeChild(TAG_VAR_NAME_BT_6_5_uM);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element '" + TAG_VAR_NAME_BT_6_5_uM + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_FlagVarNameTextIsMissing() throws Exception {
        element.getChild(TAG_VAR_NAME_CLOUD_FLAGS).setText(null);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_CLOUD_FLAGS + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_FlagVarNameTextIsEmpty() throws Exception {
        element.getChild(TAG_VAR_NAME_CLOUD_FLAGS).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_CLOUD_FLAGS + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_FlagVarNameElementIsMissing() throws Exception {
        element.removeChild(TAG_VAR_NAME_CLOUD_FLAGS);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element '" + TAG_VAR_NAME_CLOUD_FLAGS + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_DistanceFilePathIsMissing() throws Exception {
        element.getChild(TAG_DISTANCE_PRODUCT_FILE_PATH).setText(null);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'point_distance-product-file-path' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_DistanceFilePathTextIsEmpty() throws Exception {
        element.getChild(TAG_DISTANCE_PRODUCT_FILE_PATH).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'point_distance-product-file-path' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_DistanceFilePathElementIsMissing() throws Exception {
        element.removeChild(TAG_DISTANCE_PRODUCT_FILE_PATH);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element 'point_distance-product-file-path' expected", expected.getMessage());
        }
    }


    @Test
    public void testCreatePostProcessing_LatitudeVarNameTextIsMissing() throws Exception {
        element.getChild(TAG_VAR_NAME_LATITUDE).setText(null);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_LATITUDE + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_LatitudeVarNameTextIsEmpty() throws Exception {
        element.getChild(TAG_VAR_NAME_LATITUDE).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_LATITUDE + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_LatitudeVarNameElementIsMissing() throws Exception {
        element.removeChild(TAG_VAR_NAME_LATITUDE);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element '" + TAG_VAR_NAME_LATITUDE + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_LongitudeVarNameTextIsMissing() throws Exception {
        element.getChild(TAG_VAR_NAME_LONGITUDE).setText(null);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_LONGITUDE + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_LongitudeVarNameTextIsEmpty() throws Exception {
        element.getChild(TAG_VAR_NAME_LONGITUDE).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_LONGITUDE + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_LongitudeVarNameElementIsMissing() throws Exception {
        element.removeChild(TAG_VAR_NAME_LONGITUDE);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element '" + TAG_VAR_NAME_LONGITUDE + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_SourceFileNameVarNameTextIsMissing() throws Exception {
        element.getChild(TAG_VAR_NAME_SOURCE_FILE_NAME).setText(null);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_SOURCE_FILE_NAME + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_SourceFileNameVarNameTextIsEmpty() throws Exception {
        element.getChild(TAG_VAR_NAME_SOURCE_FILE_NAME).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_SOURCE_FILE_NAME + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_SourceFileNameVarNameElementIsMissing() throws Exception {
        element.removeChild(TAG_VAR_NAME_SOURCE_FILE_NAME);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element '" + TAG_VAR_NAME_SOURCE_FILE_NAME + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_ProcessingVersionVarNameTextIsMissing() throws Exception {
        element.getChild(TAG_VAR_NAME_PROCESSING_VERSION).setText(null);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_PROCESSING_VERSION + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_ProcessingVersionVarNameTextIsEmpty() throws Exception {
        element.getChild(TAG_VAR_NAME_PROCESSING_VERSION).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_PROCESSING_VERSION + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_ProcessingVersionVarNameElementIsMissing() throws Exception {
        element.removeChild(TAG_VAR_NAME_PROCESSING_VERSION);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element '" + TAG_VAR_NAME_PROCESSING_VERSION + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_XVarNameTextIsMissing() throws Exception {
        element.getChild(TAG_VAR_NAME_SOURCE_X).setText(null);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_SOURCE_X + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_XVarNameTextIsEmpty() throws Exception {
        element.getChild(TAG_VAR_NAME_SOURCE_X).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_SOURCE_X + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_XVarNameElementIsMissing() throws Exception {
        element.removeChild(TAG_VAR_NAME_SOURCE_X);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element '" + TAG_VAR_NAME_SOURCE_X + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_YVarNameTextIsMissing() throws Exception {
        element.getChild(TAG_VAR_NAME_SOURCE_Y).setText(null);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_SOURCE_Y + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_YVarNameTextIsEmpty() throws Exception {
        element.getChild(TAG_VAR_NAME_SOURCE_Y).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_SOURCE_Y + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_YrocessingVersionVarNameElementIsMissing() throws Exception {
        element.removeChild(TAG_VAR_NAME_SOURCE_Y);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element '" + TAG_VAR_NAME_SOURCE_Y + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_SensorNameTextIsMissing() throws Exception {
        element.getChild(TAG_SENSOR_NAME).setText(null);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_SENSOR_NAME + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_SensorNameTextIsEmpty() throws Exception {
        element.getChild(TAG_SENSOR_NAME).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_SENSOR_NAME + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_SensorNameElementIsMissing() throws Exception {
        element.removeChild(TAG_SENSOR_NAME);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element '" + TAG_SENSOR_NAME + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_SourceBtVarNameTextIsMissing() throws Exception {
        element.getChild(TAG_VAR_NAME_SOURCE_BT_11_1_mM).setText(null);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_SOURCE_BT_11_1_mM + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_SourceBtVarNameTextIsEmpty() throws Exception {
        element.getChild(TAG_VAR_NAME_SOURCE_BT_11_1_mM).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element '" + TAG_VAR_NAME_SOURCE_BT_11_1_mM + "' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_SourceBtVarNameElementIsMissing() throws Exception {
        element.removeChild(TAG_VAR_NAME_SOURCE_BT_11_1_mM);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element '" + TAG_VAR_NAME_SOURCE_BT_11_1_mM + "' expected", expected.getMessage());
        }
    }
}
