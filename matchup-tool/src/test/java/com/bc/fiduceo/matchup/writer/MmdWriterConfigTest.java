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

package com.bc.fiduceo.matchup.writer;


import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import static com.bc.fiduceo.matchup.writer.MmdWriterFactory.NetcdfType.N3;
import static com.bc.fiduceo.matchup.writer.MmdWriterFactory.NetcdfType.N4;
import static org.junit.Assert.*;

public class MmdWriterConfigTest {

    private MmdWriterConfig config;

    @Before
    public void setUp() {
        config = new MmdWriterConfig();
    }

    @Test
    public void testSetIsOverwrite() {
        config.setOverwrite(true);
        assertTrue(config.isOverwrite());

        config.setOverwrite(false);
        assertFalse(config.isOverwrite());
    }

    @Test
    public void testSetGetCacheSize() {
        final int size_1 = 125;
        final int size_2 = 1098;

        config.setCacheSize(size_1);
        assertEquals(size_1, config.getCacheSize());

        config.setCacheSize(size_2);
        assertEquals(size_2, config.getCacheSize());
    }

    @Test
    public void testSetGetNetcdfFormat() {
        config.setNetcdfFormat("N3");
        assertEquals(N3, config.getNetcdfFormat());

        config.setNetcdfFormat("N4");
        assertEquals(N4, config.getNetcdfFormat());
    }

    @Test
    public void setNetcdfFormat_illegalString() {
        try {
            config.setNetcdfFormat("nasenmann");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testDefaultValues() {
        assertFalse(config.isOverwrite());
        assertEquals(2048, config.getCacheSize());
        assertEquals(N4, config.getNetcdfFormat());

        final VariablesConfiguration variablesConfiguration = config.getVariablesConfiguration();
        assertNotNull(variablesConfiguration);
    }

    @Test
    public void testLoad_overwrite() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <overwrite>true</overwrite>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        final MmdWriterConfig loadedConfig = MmdWriterConfig.load(inputStream);
        assertTrue(loadedConfig.isOverwrite());

        assertEquals(2048, loadedConfig.getCacheSize());
        assertEquals(N4, loadedConfig.getNetcdfFormat());
    }

    @Test
    public void testLoad_cacheSize() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <cache-size>119</cache-size>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        final MmdWriterConfig loadedConfig = MmdWriterConfig.load(inputStream);
        assertEquals(119, loadedConfig.getCacheSize());

        assertFalse(loadedConfig.isOverwrite());
        assertEquals(N4, loadedConfig.getNetcdfFormat());
    }

    @Test
    public void testLoad_netcdfFormat() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <netcdf-format>N3</netcdf-format>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        final MmdWriterConfig loadedConfig = MmdWriterConfig.load(inputStream);
        assertEquals(N3, loadedConfig.getNetcdfFormat());
    }

    @Test
    public void testLoad_emptyVariablesConfiguration() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration/>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        final MmdWriterConfig loadedConfig = MmdWriterConfig.load(inputStream);

        final VariablesConfiguration variablesConfiguration = loadedConfig.getVariablesConfiguration();
        assertNotNull(variablesConfiguration);
    }

    @Test
    public void testLoad_variables_oneSensorSet_renames() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <sensors names = \"hirs-n09\">" +
                                 "            <rename source-name = \"firlefanz\" target-name = \"harlekin\" />" +
                                 "            <rename source-name = \"jacke\" target-name = \"hose\" />" +
                                 "        </sensors>" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        final MmdWriterConfig loadedConfig = MmdWriterConfig.load(inputStream);

        final VariablesConfiguration variablesConfiguration = loadedConfig.getVariablesConfiguration();
        assertNotNull(variablesConfiguration);

        final Map<String, String> renames = variablesConfiguration.getRenames("hirs-n09");
        assertEquals(2, renames.size());
        assertEquals("harlekin", renames.get("firlefanz"));
        assertEquals("hose", renames.get("jacke"));
    }

    @Test
    public void testLoad_variables_oneSensorSet_threeSensors_excludes_and_renames() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <sensors names = \"avhrr-m01, avhrr-m02, avhrr-n19\">" +
                                 "            <exclude source-name = \"dont_need\" />" +
                                 "            <exclude source-name = \"ignore\" />" +
                                 "            <rename source-name = \"stupid\" target-name = \"cool\" />" +
                                 "            <rename source-name = \"boring\" target-name = \"exciting\" />" +
                                 "        </sensors>" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        final MmdWriterConfig loadedConfig = MmdWriterConfig.load(inputStream);

        final VariablesConfiguration variablesConfiguration = loadedConfig.getVariablesConfiguration();
        assertNotNull(variablesConfiguration);

        final List<String> excludes = variablesConfiguration.getExcludes("avhrr-m01");
        assertEquals(2, excludes.size());
        assertEquals("dont_need", excludes.get(0));
        assertEquals("ignore", excludes.get(1));

        final Map<String, String> renames = variablesConfiguration.getRenames("avhrr-n19");
        assertEquals(2, renames.size());
        assertEquals("cool", renames.get("stupid"));
        assertEquals("exciting", renames.get("boring"));
    }

    @Test
    public void testLoad_variables_oneSensorSet_excludes() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <sensors names = \"hirs-n10\">" +
                                 "            <exclude source-name = \"useless\" />" +
                                 "            <exclude source-name = \"boring\" />" +
                                 "        </sensors>" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        final MmdWriterConfig loadedConfig = MmdWriterConfig.load(inputStream);

        final VariablesConfiguration variablesConfiguration = loadedConfig.getVariablesConfiguration();
        assertNotNull(variablesConfiguration);

        final List<String> excludes = variablesConfiguration.getExcludes("hirs-n10");
        assertEquals(2, excludes.size());
        assertEquals("useless", excludes.get(0));
        assertEquals("boring", excludes.get(1));
    }

    @Test
    public void testLoad_variables_twoSensorSet_excludes() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <sensors names = \"hirs-n17\">" +
                                 "            <exclude source-name = \"useless\" />" +
                                 "        </sensors>" +
                                 "        <sensors names = \"aatsr-en\">" +
                                 "            <exclude source-name = \"whatever\" />" +
                                 "        </sensors>" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        final MmdWriterConfig loadedConfig = MmdWriterConfig.load(inputStream);

        final VariablesConfiguration variablesConfiguration = loadedConfig.getVariablesConfiguration();
        assertNotNull(variablesConfiguration);

        List<String> excludes = variablesConfiguration.getExcludes("hirs-n17");
        assertEquals(1, excludes.size());
        assertEquals("useless", excludes.get(0));

        excludes = variablesConfiguration.getExcludes("aatsr-en");
        assertEquals(1, excludes.size());
        assertEquals("whatever", excludes.get(0));
    }

    @Test
    public void testLoad_variables_missingSensorNames() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <sensors names = \"hirs-n17\">" +
                                 "            <exclude source-name = \"useless\" />" +
                                 "        </sensors>" +
                                 "        <sensors>" +
                                 "            <exclude source-name = \"whatever\" />" +
                                 "        </sensors>" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        try {
            MmdWriterConfig.load(inputStream);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Unable to initialize use case configuration: Missing attribute: names", expected.getMessage());
        }
    }

    @Test
    public void testLoad_variables_emptySensorNames() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <sensors names = \"\">" +
                                 "            <exclude source-name = \"useless\" />" +
                                 "        </sensors>" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        try {
            MmdWriterConfig.load(inputStream);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Unable to initialize use case configuration: Empty attribute: names", expected.getMessage());
        }
    }

    @Test
    public void testLoad_variables_exclude_missingNames() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <sensors names = \"a_sensor\">" +
                                 "            <exclude />" +
                                 "        </sensors>" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        try {
            MmdWriterConfig.load(inputStream);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Unable to initialize use case configuration: Missing attribute: source-name", expected.getMessage());
        }
    }

    @Test
    public void testLoad_variables_exclude_emptyNames() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <sensors names = \"a_sensor\">" +
                                 "            <exclude source-name = \"\"/>" +
                                 "        </sensors>" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        try {
            MmdWriterConfig.load(inputStream);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Unable to initialize use case configuration: Empty attribute: source-name", expected.getMessage());
        }
    }

    @Test
    public void testLoad_variables_rename_missingNames() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <sensors names = \"a_sensor\">" +
                                 "            <rename />" +
                                 "        </sensors>" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        try {
            MmdWriterConfig.load(inputStream);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Unable to initialize use case configuration: Missing attribute: source-name", expected.getMessage());
        }
    }

    @Test
    public void testLoad_variables_rename_emptyNames() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <sensors names = \"a_sensor\">" +
                                 "            <rename source-name = \"bla\" target-name = \"\" />" +
                                 "        </sensors>" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        try {
            MmdWriterConfig.load(inputStream);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Unable to initialize use case configuration: Empty attribute: target-name", expected.getMessage());
        }
    }

    @Test
    public void testLoad_sensor_rename() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <sensor-rename source-name = \"a_sensor\" target-name = \"a_couch\" />" +
                                 "        <sensors names = \"a_sensor\">" +
                                 "        </sensors>" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        final MmdWriterConfig config = MmdWriterConfig.load(inputStream);
        final VariablesConfiguration variablesConfiguration = config.getVariablesConfiguration();
        final Map<String, String> renames = variablesConfiguration.getSensorRenames();
        assertNotNull(renames);
        assertEquals(1, renames.size());
        assertEquals("a_couch", renames.get("a_sensor"));
    }

    @Test
    public void testLoad_separator() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <separator sensor-name = \"sensor\" separator = \"._.\" />" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        final MmdWriterConfig config = MmdWriterConfig.load(inputStream);
        final VariablesConfiguration variablesConfiguration = config.getVariablesConfiguration();
        final String separator = variablesConfiguration.getSeparator("sensor");
        assertNotNull(separator);
        assertEquals("._.", separator);
    }

    @Test
    public void testLoad_separator_for_sensor_already_set() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <separator sensor-name = \"sensor\" separator = \"._.\" />" +
                                 "        <separator sensor-name = \"sensor\" separator = \"o^_^o\" />" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        try {
            MmdWriterConfig.load(inputStream);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Unable to initialize use case configuration: Separator for sensor 'sensor' is already set.", expected.getMessage());
        }
    }

    @Test
    public void testLoad_separator_missingSeparatorAttribute() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <separator sensor-name = \"sensor\"  />" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        try {
            MmdWriterConfig.load(inputStream);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Unable to initialize use case configuration: Missing attribute: separator", expected.getMessage());
        }
    }

    @Test
    public void testLoad_separator_missingSensorNameAttribute() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <separator separator = \"o^_^o\" />" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        try {
            MmdWriterConfig.load(inputStream);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Unable to initialize use case configuration: Missing attribute: sensor-name", expected.getMessage());
        }
    }

    @Test
    public void testLoad_separator_missingSeparatorValue() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <separator sensor-name = \"sensor\" separator = \"\" />" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        try {
            MmdWriterConfig.load(inputStream);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Unable to initialize use case configuration: Empty attribute: separator", expected.getMessage());
        }
    }

    @Test
    public void testLoad_separator_missingSensorNameValue() {
        final String configXml = "<mmd-writer-config>" +
                                 "    <variables-configuration>" +
                                 "        <separator sensor-name = \"\" separator = \"o^_^o\" />" +
                                 "    </variables-configuration>" +
                                 "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        try {
            MmdWriterConfig.load(inputStream);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Unable to initialize use case configuration: Empty attribute: sensor-name", expected.getMessage());
        }
    }
}
