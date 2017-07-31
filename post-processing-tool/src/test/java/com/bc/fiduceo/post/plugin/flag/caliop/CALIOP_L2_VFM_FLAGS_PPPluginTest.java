package com.bc.fiduceo.post.plugin.flag.caliop;

import static com.bc.fiduceo.post.plugin.flag.caliop.CALIOP_L2_VFM_FLAGS_PPPlugin.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.post.PostProcessing;
import org.jdom.Element;
import org.junit.*;

/**
 * Created by Sabine on 21.07.2017.
 */
@SuppressWarnings("Duplicates")
public class CALIOP_L2_VFM_FLAGS_PPPluginTest {

    private CALIOP_L2_VFM_FLAGS_PPPlugin ppPlugin;

    @Before
    public void setUp() throws Exception {
        ppPlugin = new CALIOP_L2_VFM_FLAGS_PPPlugin();
    }

    @Test
    public void tagNames() throws Exception {
        assertEquals("caliop-level2-vfm-flags", TAG_POST_PROCESSING_NAME);
        assertEquals("mmd-source-file-variable-name", TAG_MMD_SOURCE_FILE_VARIABE_NAME);
        assertEquals("mmd-processing-version-variable-name", TAG_MMD_PROCESSING_VERSION_VARIABE_NAME);
        assertEquals("mmd-y-variable-name", TAG_MMD_Y_VARIABE_NAME);
        assertEquals("target-fcf-variable-name", TAG_TARGET_FCF_VARIABLE_NAME);
    }

    @Test
    public void getPostProcessingName() throws Exception {
        assertEquals("caliop-level2-vfm-flags", ppPlugin.getPostProcessingName());
    }

    @Test
    public void createPostProcessing() throws Exception {
        final PostProcessing pp = ppPlugin.createPostProcessing(createValidRootElement());
        assertNotNull(pp);
        assertEquals(CALIOP_L2_VFM_FLAGS_PP.class, pp.getClass());
        final CALIOP_L2_VFM_FLAGS_PP cfpp = (CALIOP_L2_VFM_FLAGS_PP) pp;
        assertEquals("caliop_vfm-cal_file_name", cfpp.srcVariableName_fileName);
        assertEquals("caliop_vfm-cal_processing_version", cfpp.srcVariableName_processingVersion);
        assertEquals("caliop_vfm-cal_y", cfpp.srcVariableName_y);
        assertEquals("caliop_vfm-cal_Center_Feature_Classification_Flags", cfpp.targetVariableName_centerFCF);
    }

    @Test
    public void createPostProcessing_wrongRootTag() throws Exception {
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
    public void createPostProcessing_sourceFileVarName_missingElement() throws Exception {
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
    public void createPostProcessing_sourceFileVarName_empty() throws Exception {
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
    public void createPostProcessing_processingVersionVarName_missingElement() throws Exception {
        final Element rootElement = createValidRootElement();
        rootElement.removeChild(TAG_MMD_PROCESSING_VERSION_VARIABE_NAME);
        try {
            ppPlugin.createPostProcessing(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals(RuntimeException.class.getTypeName(), expected.getClass().getTypeName());
            assertEquals("Child element '" + TAG_MMD_PROCESSING_VERSION_VARIABE_NAME + "' expected", expected.getMessage());
        }
    }

    @Test
    public void createPostProcessing_processingVersionVarName_empty() throws Exception {
        final Element rootElement = createValidRootElement();
        rootElement.getChild(TAG_MMD_PROCESSING_VERSION_VARIABE_NAME).setText("   ");
        try {
            ppPlugin.createPostProcessing(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals(RuntimeException.class.getTypeName(), expected.getClass().getTypeName());
            assertEquals("Value of element '" + TAG_MMD_PROCESSING_VERSION_VARIABE_NAME + "' expected", expected.getMessage());
        }
    }

    @Test
    public void createPostProcessing_yVarName_missingElement() throws Exception {
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
    public void createPostProcessing_yVarName_empty() throws Exception {
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
    public void createPostProcessing_targetVarName_missingElement() throws Exception {
        final Element rootElement = createValidRootElement();
        rootElement.removeChild(TAG_TARGET_FCF_VARIABLE_NAME);
        try {
            ppPlugin.createPostProcessing(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals(RuntimeException.class.getTypeName(), expected.getClass().getTypeName());
            assertEquals("Child element '" + TAG_TARGET_FCF_VARIABLE_NAME + "' expected", expected.getMessage());
        }
    }

    @Test
    public void createPostProcessing_targetVarName_empty() throws Exception {
        final Element rootElement = createValidRootElement();
        rootElement.getChild(TAG_TARGET_FCF_VARIABLE_NAME).setText("   ");
        try {
            ppPlugin.createPostProcessing(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals(RuntimeException.class.getTypeName(), expected.getClass().getTypeName());
            assertEquals("Value of element '" + TAG_TARGET_FCF_VARIABLE_NAME + "' expected", expected.getMessage());
        }
    }

    private Element createValidRootElement() {
        final Element root = new Element(TAG_POST_PROCESSING_NAME);
        root.addContent(new Element(TAG_MMD_SOURCE_FILE_VARIABE_NAME).setText("caliop_vfm-cal_file_name"));
        root.addContent(new Element(TAG_MMD_PROCESSING_VERSION_VARIABE_NAME).setText("caliop_vfm-cal_processing_version"));
        root.addContent(new Element(TAG_MMD_Y_VARIABE_NAME).setText("caliop_vfm-cal_y"));
        root.addContent(new Element(TAG_TARGET_FCF_VARIABLE_NAME).setText("caliop_vfm-cal_Center_Feature_Classification_Flags"));
        return root;
    }

}