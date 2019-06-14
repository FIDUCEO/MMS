package com.bc.fiduceo.post.plugin.avhrr_fcdr;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.jdom.Element;

public class AddAvhrrCorrCoeffsPlugin implements PostProcessingPlugin {

    @Override
    public PostProcessing createPostProcessing(Element element) {
        final AddAvhrrCorrCoeffs.Configuration configuration = AddAvhrrCorrCoeffs.createConfiguration(element);
        return new AddAvhrrCorrCoeffs(configuration);
    }

    @Override
    public String getPostProcessingName() {
        return "add-avhrr-corr-coeffs";
    }
}
