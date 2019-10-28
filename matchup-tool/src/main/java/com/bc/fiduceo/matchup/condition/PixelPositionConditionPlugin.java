package com.bc.fiduceo.matchup.condition;

import org.jdom.Element;

public class PixelPositionConditionPlugin implements ConditionPlugin {

    private static final String TAG_NAME_CONDITION_NAME = "pixel-position";

    @Override
    public Condition createCondition(Element element) {
        return new PixelPositionCondition();
    }

    @Override
    public String getConditionName() {
        return TAG_NAME_CONDITION_NAME;
    }

    PixelPositionCondition.Configuration parseConfig(Element element) {
        final PixelPositionCondition.Configuration configuration = new PixelPositionCondition.Configuration();
        final Element minXElement = element.getChild("minX");
        if (minXElement != null) {
            final String value = minXElement.getValue();
            configuration.minX = Integer.parseInt(value);
        }
        final Element maxXElement = element.getChild("maxX");
        if (maxXElement != null) {
            final String value = maxXElement.getValue();
            configuration.maxX = Integer.parseInt(value);
        }
        final Element minYElement = element.getChild("minY");
        if (minYElement != null) {
            final String value = minYElement.getValue();
            configuration.minY = Integer.parseInt(value);
        }
        final Element maxYElement = element.getChild("maxY");
        if (maxYElement != null) {
            final String value = maxYElement.getValue();
            configuration.maxY = Integer.parseInt(value);
        }

        return configuration;
    }
}
