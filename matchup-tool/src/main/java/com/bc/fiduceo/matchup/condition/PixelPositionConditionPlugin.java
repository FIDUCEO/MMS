package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.util.JDomUtils;
import org.esa.snap.core.util.StringUtils;
import org.jdom.Element;

import java.util.stream.Stream;

public class PixelPositionConditionPlugin implements ConditionPlugin {

    private static final String TAG_NAME_CONDITION_NAME = "pixel-position";

    @Override
    public Condition createCondition(Element element) {
        final PixelPositionCondition.Configuration configuration = parseConfig(element);
        return new PixelPositionCondition(configuration);
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

        if (configuration.minX >= configuration.maxX) {
            throw new IllegalArgumentException("Invalid pixel range for x.");
        }
        if (configuration.minY >= configuration.maxY) {
            throw new IllegalArgumentException("Invalid pixel range for y.");
        }

        final Element referenceElement = JDomUtils.getMandatoryChild(element, "reference");
        final String referenceValue = referenceElement.getValue();
        if ("PRIMARY".equalsIgnoreCase(referenceValue)) {
            configuration.isPrimary = true;
        } else if("SECONDARY".equalsIgnoreCase(referenceValue)) {
            configuration.isPrimary = false;
            final String namesFromAtt = JDomUtils.getValueFromNamesAttribute(referenceElement);
            if (StringUtils.isNotNullAndNotEmpty(namesFromAtt)) {
               configuration.secondaryNames =  Stream.of(namesFromAtt.split(",")).map(String::trim).filter(s -> s.length() > 0).toArray(String[]::new);
            }
        } else {
            throw new IllegalArgumentException("Invalid reference.");
        }
        return configuration;
    }
}
