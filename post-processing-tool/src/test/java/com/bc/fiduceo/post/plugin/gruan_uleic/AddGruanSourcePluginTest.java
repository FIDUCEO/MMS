package com.bc.fiduceo.post.plugin.gruan_uleic;

import com.bc.fiduceo.post.PostProcessing;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AddGruanSourcePluginTest {

    private AddGruanSourcePlugin plugin;

    @Before
    public void setUp() {
        plugin = new AddGruanSourcePlugin();
    }

    @Test
    public void testGetPostProcessingName() {
        assertEquals("add-gruan-source", plugin.getPostProcessingName());
    }

    @Test
    public void testCreatePostProcessing() throws JDOMException, IOException {
        final Element rootElement = AddGruanSourceTest.createFullConfigElement();

        final PostProcessing postProcessing = plugin.createPostProcessing(rootElement);
        assertTrue(postProcessing instanceof AddGruanSource);
    }
}
