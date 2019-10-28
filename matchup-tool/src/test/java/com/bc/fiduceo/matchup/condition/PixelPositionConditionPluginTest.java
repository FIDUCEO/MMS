package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.TestUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
                "</pixel-position>";
        final Element element = TestUtil.createDomElement(XML);

        final Condition condition = plugin.createCondition(element);
        assertNotNull(condition);
    }

    @Test
    public void testParseConfig() throws JDOMException, IOException {
        final String XML = "<pixel-position>" +
                "    <minX>11</minX>" +
                "    <maxX>12</maxX>" +
                "    <minY>13</minY>" +
                "    <maxY>14</maxY>" +
                "</pixel-position>";
        final Element element = TestUtil.createDomElement(XML);

        final PixelPositionCondition.Configuration config = plugin.parseConfig(element);
        assertEquals(11, config.minX);
        assertEquals(12, config.maxX);
        assertEquals(13, config.minY);
        assertEquals(14, config.maxY);
    }
}
