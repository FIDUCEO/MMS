package com.bc.fiduceo.post.plugin.land_distance;


import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.jdom.Element;

public class AddLandDistancePlugin implements PostProcessingPlugin {

    static final String POST_PROCESSING_NAME = "add-distance-to-land";

    @Override
    public PostProcessing createPostProcessing(Element element) {
        final AddLandDistance.Configuration configuration = AddLandDistance.createConfiguration(element);
        return new AddLandDistance(configuration);
    }

    @Override
    public String getPostProcessingName() {
        return POST_PROCESSING_NAME;
    }
}
