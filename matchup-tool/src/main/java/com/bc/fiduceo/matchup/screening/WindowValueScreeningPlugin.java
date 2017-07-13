/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package com.bc.fiduceo.matchup.screening;

import static com.bc.fiduceo.matchup.screening.WindowValueScreening.Evaluate.EntireWindow;

import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.matchup.screening.WindowValueScreening.Configuration;
import com.bc.fiduceo.matchup.screening.WindowValueScreening.Evaluate;
import com.bc.fiduceo.matchup.screening.WindowValueScreening.SecondaryConfiguration;
import com.bc.fiduceo.util.JDomUtils;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class WindowValueScreeningPlugin implements ScreeningPlugin {

    public static final String ROOT_TAG_NAME = "window-value";
    public static final String TAG_NAME_PRIMARY = "primary";
    public static final String TAG_NAME_SECONDARY = "secondary";
    public static final String TAG_NAME_EXPRESSION = "expression";
    public static final String TAG_NAME_PERCENTAGE = "percentage";
    public static final String TAG_NAME_EVALUATE = "evaluate";

    @Override
    public Screening createScreening(Element element) {
        final Configuration configuration = createConfiguration(element);
        return new WindowValueScreening(configuration);
    }

    @Override
    public String getScreeningName() {
        return ROOT_TAG_NAME;
    }

    static Configuration createConfiguration(Element rootElement) {
        final String rootName = rootElement.getName();
        if (!ROOT_TAG_NAME.endsWith(rootName)) {
            throw new RuntimeException("Illegal root element name '" + rootName + "'. Expected root element name is '" + ROOT_TAG_NAME + "'.");
        }

        final Configuration configuration = new Configuration();

        final Element primaryElement = rootElement.getChild(TAG_NAME_PRIMARY);
        if (primaryElement != null) {
            String primaryExpression = null;
            Double primaryPercentage = null;
            Evaluate primaryEvaluate = EntireWindow;

            final Element expressionElem = primaryElement.getChild(TAG_NAME_EXPRESSION);
            if (expressionElem != null) {
                primaryExpression = expressionElem.getTextTrim();
            }

            final Element percentageElem = primaryElement.getChild(TAG_NAME_PERCENTAGE);
            if (percentageElem != null) {
                final String value = percentageElem.getTextTrim();
                try {
                    primaryPercentage = Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid primary percentage '" + value + "'", e);
                }
            }

            final Element evaluateElem = primaryElement.getChild(TAG_NAME_EVALUATE);
            if (evaluateElem != null) {
                final String value = evaluateElem.getTextTrim();
                primaryEvaluate = Evaluate.valueOf(value);
            }

            if (primaryExpression != null && primaryPercentage == null) {
                throw new RuntimeException("Primary percentage is missing.");
            }

            if (primaryExpression == null && primaryPercentage != null) {
                throw new RuntimeException("Primary expression is missing.");
            }

            configuration.primaryExpression = primaryExpression;
            configuration.primaryPercentage = primaryPercentage;
            configuration.primaryEvaluate = primaryEvaluate;
        }

        boolean secondaryWithoutName = false;

        @SuppressWarnings("unchecked") final List<Element> secondaryElements = rootElement.getChildren(TAG_NAME_SECONDARY);
        final ArrayList<SecondaryConfiguration> secondaryConfigurations = new ArrayList<>();
        for (Element secondaryElement : secondaryElements) {

            String secondaryExpression = null;
            final Element expressionElem = secondaryElement.getChild(TAG_NAME_EXPRESSION);
            if (expressionElem != null) {
                secondaryExpression = expressionElem.getTextTrim();
            }

            Double secondaryPercentage = null;
            final Element percentageElem = secondaryElement.getChild(TAG_NAME_PERCENTAGE);
            if (percentageElem != null) {
                final String value = percentageElem.getTextTrim();
                try {
                    secondaryPercentage = Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid secondary percentage '" + value + "'", e);
                }
            }

            Evaluate secondaryEvaluate = EntireWindow;
            final Element evaluateElem = secondaryElement.getChild(TAG_NAME_EVALUATE);
            if (evaluateElem != null) {
                final String value = evaluateElem.getTextTrim();
                secondaryEvaluate = Evaluate.valueOf(value);
            }

            if (secondaryExpression != null && secondaryPercentage == null) {
                throw new RuntimeException("Secondary percentage is missing.");
            }

            if (secondaryExpression == null && secondaryPercentage != null) {
                throw new RuntimeException("Secondary expression is missing.");
            }

            final String secSensorNameAttVal = JDomUtils.getValueFromNamesAttribute(secondaryElement);
            final String[] secondarySensorNames;
            if (secSensorNameAttVal == null || secSensorNameAttVal.trim().length() == 0) {
                secondaryWithoutName = true;
                secondarySensorNames = new String[]{SampleSet.getOnlyOneSecondaryKey()};
            } else {
                secondarySensorNames = Stream.of(secSensorNameAttVal.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
            }
            for (String secondarySensorName : secondarySensorNames) {
                final SecondaryConfiguration secondaryConfiguration;
                secondaryConfiguration = new SecondaryConfiguration();
                secondaryConfiguration.secondarySensorName = secondarySensorName;
                secondaryConfiguration.secondaryExpression = secondaryExpression;
                secondaryConfiguration.secondaryPercentage = secondaryPercentage;
                secondaryConfiguration.secondaryEvaluate = secondaryEvaluate;
                secondaryConfigurations.add(secondaryConfiguration);
            }
        }

        if (configuration.primaryExpression == null && secondaryConfigurations.size() == 0) {
            throw new RuntimeException("At least primary or secondary expression must be implemented.");
        }

        if (secondaryConfigurations.size() > 0) {
            if (secondaryWithoutName && secondaryConfigurations.size() != 1) {
                throw new RuntimeException("It is not allowed to mix '" + TAG_NAME_SECONDARY + "' tags with and without 'names' attribute.");
            }
            final SecondaryConfiguration[] array = new SecondaryConfiguration[secondaryConfigurations.size()];
            configuration.secondaryConfigurations = secondaryConfigurations.toArray(array);
        }
        return configuration;
    }
}
