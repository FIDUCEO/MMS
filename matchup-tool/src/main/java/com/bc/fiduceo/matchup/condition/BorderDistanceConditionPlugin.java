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
 *
 */

package com.bc.fiduceo.matchup.condition;

import static com.bc.fiduceo.util.JDomUtils.*;
import static org.esa.snap.core.util.StringUtils.isNullOrEmpty;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/* The XML template for this condition class looks like:

Single secondary sensor mode:

    <border-distance>
        <primary>
            <nx>2</nx>
            <ny>3</ny>
        </primary>
        <secondary>
            <nx>5</nx>
            <ny>4</ny>
        </secondary>
    </border-distance>

or

Multiple secondary sensor mode:

    <border-distance>
        <primary>
            <nx>2</nx>
            <ny>3</ny>
        </primary>
        <secondary names="name">
            <nx>5</nx>
            <ny>4</ny>
        </secondary>
        <secondary names="nameA,nameB">
            <nx>5</nx>
            <ny>4</ny>
        </secondary>
    </border-distance>
 */

public class BorderDistanceConditionPlugin implements ConditionPlugin {

    @Override
    public Condition createCondition(Element element) {
        final List<BorderDistanceCondition.Configuration> configurations = parseConfiguration(element);

        return new BorderDistanceCondition(configurations);
    }

    @Override
    public String getConditionName() {
        return "border-distance";
    }

    List<BorderDistanceCondition.Configuration> parseConfiguration(Element element) {
        if (!getConditionName().equals(element.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + getConditionName() + "' expected.");
        }

        final List primaryElements = element.getChildren("primary");
        if (primaryElements.size() > 1) {
            throw new RuntimeException("Illegal XML Element. Tag name 'primary'. Only one 'primary' definition allowed.");
        }
        final ArrayList<BorderDistanceCondition.Configuration> configurations = new ArrayList<>();
        if (primaryElements.size() == 1) {
            final Element primaryElement = (Element) primaryElements.get(0);
            final String nx = getMandatoryChildTextTrim(primaryElement, "nx");
            final String ny = getMandatoryChildTextTrim(primaryElement, "ny");
            final BorderDistanceCondition.Configuration configuration = new BorderDistanceCondition.Configuration();
            configuration.usePrimary = true;
            configuration.primary_x = Integer.parseInt(nx);
            configuration.primary_y = Integer.parseInt(ny);
            configurations.add(configuration);
        }

        boolean namedSecondary = false;
        int unnamedSecondaryCount = 0;
        final Set<String> namesSet = new HashSet<>();

        final List secondaryElements = element.getChildren("secondary");
        for (Object elemObj : secondaryElements) {
            final Element secondaryElement = (Element) elemObj;
            final String nxS = getMandatoryChildTextTrim(secondaryElement, "nx");
            final String nyS = getMandatoryChildTextTrim(secondaryElement, "ny");
            final int nx = Integer.parseInt(nxS);
            final int ny = Integer.parseInt(nyS);

            final String namesVal = getValueFromNamesAttribute(secondaryElement);

            if (isNullOrEmpty(namesVal)) {
                final BorderDistanceCondition.Configuration configuration = new BorderDistanceCondition.Configuration();
                configuration.useSecondary = true;
                configuration.secondary_x = nx;
                configuration.secondary_y = ny;
                configurations.add(configuration);
                unnamedSecondaryCount++;
            } else {
                final String[] names = Stream.of(namesVal.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
                for (String name : names) {
                    if (!namesSet.add(name)) {
                        throw new RuntimeException("It is not allowed to use a secondary name twice.");
                    }
                    final BorderDistanceCondition.Configuration configuration = new BorderDistanceCondition.Configuration();
                    configuration.useSecondary = true;
                    configuration.secondaryName = name;
                    configuration.secondary_x = nx;
                    configuration.secondary_y = ny;
                    configurations.add(configuration);
                    namedSecondary = true;
                }
            }
        }

        if (unnamedSecondaryCount > 1) {
            throw new RuntimeException("Forbidden to define two unnamed 'secondary' tags.");
        }
        if (namedSecondary && unnamedSecondaryCount == 1) {
            throw new RuntimeException("It is not allowed to mix 'secondary' tags with and without 'names' attribute.");
        }

        return configurations;
    }
}
