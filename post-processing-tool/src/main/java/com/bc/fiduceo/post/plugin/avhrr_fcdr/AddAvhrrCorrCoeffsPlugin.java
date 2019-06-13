package com.bc.fiduceo.post.plugin.avhrr_fcdr;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.jdom.Element;

public class AddAvhrrCorrCoeffsPlugin implements PostProcessingPlugin {

    @Override
    public PostProcessing createPostProcessing(Element element) {
        return new AddAvhrrCorrCoeffs();
    }

    @Override
    public String getPostProcessingName() {
        return "add-avhrr-corr-coeffs";
    }
}
