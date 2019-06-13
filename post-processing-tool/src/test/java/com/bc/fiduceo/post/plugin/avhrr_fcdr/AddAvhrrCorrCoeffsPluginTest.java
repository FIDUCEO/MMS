package com.bc.fiduceo.post.plugin.avhrr_fcdr;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.post.PostProcessing;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AddAvhrrCorrCoeffsPluginTest {

    private AddAvhrrCorrCoeffsPlugin plugin;

    @Before
    public void setUp() {
        plugin = new AddAvhrrCorrCoeffsPlugin();
    }

    @Test
    public void testGetPostProcessingName() {
        assertEquals("add-avhrr-corr-coeffs", plugin.getPostProcessingName());
    }

    @Test
    public void testCreatePostProcessing() throws JDOMException, IOException {
        final Element element = TestUtil.createDomElement("<add-avhrr-corr-coeffs/>");

        final PostProcessing postProcessing = plugin.createPostProcessing(element);
        assertNotNull(postProcessing);
        assertTrue(postProcessing instanceof AddAvhrrCorrCoeffs);
    }
}
