package com.bc.fiduceo.post.plugin.gruan_uleic;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.jdom.Element;

public class AddGruanSourcePlugin implements PostProcessingPlugin {

    @Override
    public PostProcessing createPostProcessing(Element element) {
        return new AddGruanSource(new AddGruanSource.Configuration());
    }

    @Override
    public String getPostProcessingName() {
        return "add-gruan-source";
    }
}
