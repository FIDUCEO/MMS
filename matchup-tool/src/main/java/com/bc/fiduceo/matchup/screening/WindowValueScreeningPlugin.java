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

import org.jdom.Element;

public class WindowValueScreeningPlugin implements ScreeningPlugin {

    public static final String ROOT_TAG_NAME = "window-value";
    public static final String TAG_NAME_PRIMARY_EXPRESSION = "primary-expression";
    public static final String TAG_NAME_PRIMARY_PERCENTAGE = "primary-percentage";
    public static final String TAG_NAME_SECONDARY_EXPRESSION = "secondary-expression";
    public static final String TAG_NAME_SECONDARY_PERCENTAGE = "secondary-percentage";

    @Override
    public Screening createScreening(Element element) {
        final WindowValueScreening.Configuration configuration = createConfiguration(element);
        return new WindowValueScreening(configuration);
    }

    @Override
    public String getScreeningName() {
        return ROOT_TAG_NAME;
    }

    static WindowValueScreening.Configuration createConfiguration(Element rootElement) {
        final String rootName = rootElement.getName();
        if (!ROOT_TAG_NAME.endsWith(rootName)) {
            throw new RuntimeException("Illegal root element name '" + rootName + "'. Expected root element name is '" + ROOT_TAG_NAME + "'.");
        }

        String primaryExpression = null;
        Double primaryPercentage = null;
        String secondaryExpression = null;
        Double secondaryPercentage = null;

        Element element = rootElement.getChild(TAG_NAME_PRIMARY_EXPRESSION);
        if (element != null) {
            primaryExpression = element.getValue();
        }

        element = rootElement.getChild(TAG_NAME_PRIMARY_PERCENTAGE);
        if (element != null) {
            final String value = element.getValue();
            try {
                primaryPercentage = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid primary percentage '" + value + "'", e);
            }
        }

        element = rootElement.getChild(TAG_NAME_SECONDARY_EXPRESSION);
        if (element != null) {
            secondaryExpression = element.getValue();
        }

        element = rootElement.getChild(TAG_NAME_SECONDARY_PERCENTAGE);
        if (element != null) {
            final String value = element.getValue();
            try {
                secondaryPercentage = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid secondary percentage '" + value + "'", e);
            }
        }

        if (primaryExpression != null && primaryPercentage == null) {
            throw new RuntimeException("Primary percentage is missing.");
        }

        if (primaryExpression == null && primaryPercentage != null) {
            throw new RuntimeException("Primary expression is missing.");
        }

        if (secondaryExpression != null && secondaryPercentage == null) {
            throw new RuntimeException("Secondary percentage is missing.");
        }

        if (secondaryExpression == null && secondaryPercentage != null) {
            throw new RuntimeException("Secondary expression is missing.");
        }

        if (primaryExpression == null && secondaryExpression == null) {
            throw new RuntimeException("At least primary or secondary expression must be implemented.");
        }

        final WindowValueScreening.Configuration configuration = new WindowValueScreening.Configuration();
        configuration.primaryExpression = primaryExpression;
        configuration.primaryPercentage = primaryPercentage;
        configuration.secondaryExpression = secondaryExpression;
        configuration.secondaryPercentage = secondaryPercentage;
        return configuration;
    }
}
