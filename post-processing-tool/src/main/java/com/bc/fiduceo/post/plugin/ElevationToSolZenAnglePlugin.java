package com.bc.fiduceo.post.plugin;


import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.jdom.Element;

public class ElevationToSolZenAnglePlugin implements PostProcessingPlugin {

    @Override
    public PostProcessing createPostProcessing(Element element) {
        return new ElevationToSolZenAngle();
    }

    @Override
    public String getPostProcessingName() {
        return "elevation-to-solzen-angle";
    }
}
