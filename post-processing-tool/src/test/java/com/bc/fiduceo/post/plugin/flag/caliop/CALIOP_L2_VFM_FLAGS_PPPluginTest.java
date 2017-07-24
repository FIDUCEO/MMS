package com.bc.fiduceo.post.plugin.flag.caliop;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import com.bc.fiduceo.post.PostProcessing;
import org.jdom.Element;
import org.junit.*;

/**
 * Created by Sabine on 21.07.2017.
 */
public class CALIOP_L2_VFM_FLAGS_PPPluginTest {

    private CALIOP_L2_VFM_FLAGS_PPPlugin ppPlugin;

    @Before
    public void setUp() throws Exception {
        ppPlugin = new CALIOP_L2_VFM_FLAGS_PPPlugin();
    }

    @Test
    public void createPostProcessing() throws Exception {
        final PostProcessing postProcessing = ppPlugin.createPostProcessing(new Element("caliop-level2-vfm-flags"));
        assertNotNull(postProcessing);
        assertEquals(CALIOP_L2_VFM_FLAGS_PP.class,  postProcessing.getClass());
    }

    @Test
    public void getPostProcessingName() throws Exception {
        assertEquals("caliop-level2-vfm-flags", ppPlugin.getPostProcessingName());
    }

}