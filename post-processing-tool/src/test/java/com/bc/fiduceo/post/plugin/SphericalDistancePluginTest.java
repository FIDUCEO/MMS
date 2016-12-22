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

package com.bc.fiduceo.post.plugin;

import static com.bc.fiduceo.post.plugin.SphericalDistancePlugin.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.post.PostProcessing;
import org.jdom.Element;
import org.junit.*;

import java.util.Arrays;

public class SphericalDistancePluginTest {

    private SphericalDistancePlugin plugin;
    private Element element;

    @Before
    public void setUp() throws Exception {
        plugin = new SphericalDistancePlugin();
        element = new Element(TAG_NAME_SPHERICAL_DISTANCE).addContent(Arrays.asList(
                    new Element(TAG_NAME_TARGET).addContent(Arrays.asList(
                                new Element(TAG_NAME_VAR_NAME).addContent("post_sphere_distance"),
                                new Element(TAG_NAME_DIM_NAME).addContent("matchup_count"),
                                new Element(TAG_NAME_DATA_TYPE).addContent("Float")
                    )),
                    new Element("primary-lat-variable").addContent("p_lat").setAttribute("scaleAttrName", "S1").setAttribute("offsetAttrName", "o1"),
                    new Element("primary-lon-variable").addContent("p_lon").setAttribute("scaleAttrName", "S2").setAttribute("offsetAttrName", "o2"),
                    new Element("secondary-lat-variable").addContent("s_lat").setAttribute("scaleAttrName", "S3").setAttribute("offsetAttrName", "o3"),
                    new Element("secondary-lon-variable").addContent("s_lon").setAttribute("scaleAttrName", "S4").setAttribute("offsetAttrName", "o4")
        ));
    }

    @Test
    public void testCreatePostProcessing() {

        final PostProcessing postProcessing = plugin.createPostProcessing(element);

        assertNotNull(postProcessing);
        assertEquals("com.bc.fiduceo.post.plugin.SphericalDistance", postProcessing.getClass().getTypeName());

        final SphericalDistance sphericalDistance = (SphericalDistance) postProcessing;

        assertEquals("post_sphere_distance", sphericalDistance.targetVarName);
        assertEquals("matchup_count", sphericalDistance.targetDimName);
        assertEquals("Float", sphericalDistance.targetDataType);
        assertEquals("p_lat", sphericalDistance.primLatVar);
        assertEquals("S1", sphericalDistance.primLatScaleAttrName);
        assertEquals("o1", sphericalDistance.primLatOffsetAttrName);
        assertEquals("p_lon", sphericalDistance.primLonVar);
        assertEquals("S2", sphericalDistance.primLonScaleAttrName);
        assertEquals("o2", sphericalDistance.primLonOffsetAttrName);
        assertEquals("s_lat", sphericalDistance.secoLatVar);
        assertEquals("S3", sphericalDistance.secoLatScaleAttrName);
        assertEquals("o3", sphericalDistance.secoLatOffsetAttrName);
        assertEquals("s_lon", sphericalDistance.secoLonVar);
        assertEquals("S4", sphericalDistance.secoLonScaleAttrName);
        assertEquals("o4", sphericalDistance.secoLonOffsetAttrName);
    }

    @Test
    public void testCreatePostProcessing_wrongElementName() throws Exception {
        element.setName("wrong_name");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Illegal XML Element. Tagname 'spherical-distance' expected.", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_targetElementIsMissing() throws Exception {
        element.removeChild(TAG_NAME_TARGET);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element 'target' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_targetVarNameElementIsMissing() throws Exception {
        element.getChild(TAG_NAME_TARGET).removeChild(TAG_NAME_VAR_NAME);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element 'var-name' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_targetVarNameElement_valueIsMissing() throws Exception {
        element.getChild(TAG_NAME_TARGET).getChild(TAG_NAME_VAR_NAME).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'var-name' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_targetDimNameElementIsMissing() throws Exception {
        element.getChild(TAG_NAME_TARGET).removeChild(TAG_NAME_DIM_NAME);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element 'dim-name' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_targetDimNameElement_valueIsMissing() throws Exception {
        element.getChild(TAG_NAME_TARGET).getChild(TAG_NAME_DIM_NAME).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'dim-name' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_targetDataTypeElementIsMissing() throws Exception {
        element.getChild(TAG_NAME_TARGET).removeChild(TAG_NAME_DATA_TYPE);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element 'data-type' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_targetDataTypeElement_valueIsMissing() throws Exception {
        element.getChild(TAG_NAME_TARGET).getChild(TAG_NAME_DATA_TYPE).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'data-type' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_primaryLatElemElementIsMissing() throws Exception {
        element.removeChild(TAG_NAME_PRIM_LAT_VAR);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element 'primary-lat-variable' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_primaryLatElemElement_valueIsMissing() throws Exception {
        element.getChild(TAG_NAME_PRIM_LAT_VAR).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'primary-lat-variable' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_primaryLonElemElementIsMissing() throws Exception {
        element.removeChild(TAG_NAME_PRIM_LON_VAR);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element 'primary-lon-variable' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_primaryLonElemElement_valueIsMissing() throws Exception {
        element.getChild(TAG_NAME_PRIM_LON_VAR).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'primary-lon-variable' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_secundaryLatElemElementIsMissing() throws Exception {
        element.removeChild(TAG_NAME_SECO_LAT_VAR);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element 'secondary-lat-variable' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_secundaryLatElemElement_valueIsMissing() throws Exception {
        element.getChild(TAG_NAME_SECO_LAT_VAR).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'secondary-lat-variable' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_secundaryLonElemElementIsMissing() throws Exception {
        element.removeChild(TAG_NAME_SECO_LON_VAR);

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Child element 'secondary-lon-variable' expected", expected.getMessage());
        }
    }

    @Test
    public void testCreatePostProcessing_secundaryLonElemElement_valueIsMissing() throws Exception {
        element.getChild(TAG_NAME_SECO_LON_VAR).setText("");

        try {
            plugin.createPostProcessing(element);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of element 'secondary-lon-variable' expected", expected.getMessage());
        }
    }
}
