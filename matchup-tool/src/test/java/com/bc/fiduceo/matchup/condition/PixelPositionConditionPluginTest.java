package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.TestUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class PixelPositionConditionPluginTest {

    private PixelPositionConditionPlugin plugin;

    @Before
    public void setUp() {
        plugin = new PixelPositionConditionPlugin();
    }

    @Test
    public void testGetConditionName() {
        assertEquals("pixel-position", plugin.getConditionName());
    }

    @Test
    public void testCreateCondition() throws JDOMException, IOException {
        final String XML = "<pixel-position>" +
                "    <minX>11</minX>" +
                "    <maxX>12</maxX>" +
                "    <minY>13</minY>" +
                "    <maxY>14</maxY>" +
                "    <reference>PRIMARY</reference>" +
                "</pixel-position>";
        final Element element = TestUtil.createDomElement(XML);

        final Condition condition = plugin.createCondition(element);
        assertNotNull(condition);
    }

    @Test
    public void testParseConfig_primary() throws JDOMException, IOException {
        final String XML = "<pixel-position>" +
                "    <minX>11</minX>" +
                "    <maxX>12</maxX>" +
                "    <minY>13</minY>" +
                "    <maxY>14</maxY>" +
                "    <reference>PRIMARY</reference>" +
                "</pixel-position>";
        final Element element = TestUtil.createDomElement(XML);

        final PixelPositionCondition.Configuration config = plugin.parseConfig(element);
        assertEquals(11, config.minX);
        assertEquals(12, config.maxX);
        assertEquals(13, config.minY);
        assertEquals(14, config.maxY);
        assertTrue(config.isPrimary);
        assertEquals(0, config.secondaryNames.length);
    }

    @Test
    public void testParseConfig_oneSecondary() throws JDOMException, IOException {
        final String XML = "<pixel-position>" +
                "    <minX>11</minX>" +
                "    <maxX>12</maxX>" +
                "    <minY>13</minY>" +
                "    <maxY>14</maxY>" +
                "    <reference>SECONDARY</reference>" +
                "</pixel-position>";
        final Element element = TestUtil.createDomElement(XML);

        final PixelPositionCondition.Configuration config = plugin.parseConfig(element);
        assertEquals(11, config.minX);
        assertEquals(12, config.maxX);
        assertEquals(13, config.minY);
        assertEquals(14, config.maxY);
        assertFalse(config.isPrimary);
        assertEquals(0, config.secondaryNames.length);
    }

    @Test
    public void testParseConfig_twoSecondary() throws JDOMException, IOException {
        final String XML = "<pixel-position>" +
                "    <minX>11</minX>" +
                "    <maxX>12</maxX>" +
                "    <minY>13</minY>" +
                "    <maxY>14</maxY>" +
                "    <reference names=\"one_band,two_band\">SECONDARY</reference>" +
                "</pixel-position>";
        final Element element = TestUtil.createDomElement(XML);

        final PixelPositionCondition.Configuration config = plugin.parseConfig(element);
        assertEquals(11, config.minX);
        assertEquals(12, config.maxX);
        assertEquals(13, config.minY);
        assertEquals(14, config.maxY);
        assertFalse(config.isPrimary);
        assertEquals(2, config.secondaryNames.length);
        assertEquals("one_band", config.secondaryNames[0]);
        assertEquals("two_band", config.secondaryNames[1]);
    }

    @Test
    public void testParseConfig_missingElements() throws JDOMException, IOException {
        final String XML = "<pixel-position>" +
                "    <maxX>12</maxX>" +
                "    <minY>13</minY>" +
                "    <reference>PRIMARY</reference>" +
                "</pixel-position>";
        final Element element = TestUtil.createDomElement(XML);

        final PixelPositionCondition.Configuration config = plugin.parseConfig(element);
        assertEquals(Integer.MIN_VALUE, config.minX);
        assertEquals(12, config.maxX);
        assertEquals(13, config.minY);
        assertEquals(Integer.MAX_VALUE, config.maxY);
    }

    @Test
    public void testParseConfig_invalidXRange() throws JDOMException, IOException {
        final String XML = "<pixel-position>" +
                "    <minX>14</minX>" +
                "    <maxX>12</maxX>" +
                "    <minY>13</minY>" +
                "    <maxY>14</maxY>" +
                "    <reference>PRIMARY</reference>" +
                "</pixel-position>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.parseConfig(element);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testParseConfig_invalidYRange() throws JDOMException, IOException {
        final String XML = "<pixel-position>" +
                "    <minX>11</minX>" +
                "    <maxX>12</maxX>" +
                "    <minY>100</minY>" +
                "    <maxY>14</maxY>" +
                "    <reference>PRIMARY</reference>" +
                "</pixel-position>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.parseConfig(element);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testParseConfig_invalidReference() throws JDOMException, IOException {
        final String XML = "<pixel-position>" +
                "    <minX>11</minX>" +
                "    <maxX>12</maxX>" +
                "    <minY>13</minY>" +
                "    <maxY>14</maxY>" +
                "    <reference>WTF_INVALID</reference>" +
                "</pixel-position>";
        final Element element = TestUtil.createDomElement(XML);

        try {
            plugin.parseConfig(element);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }
}
