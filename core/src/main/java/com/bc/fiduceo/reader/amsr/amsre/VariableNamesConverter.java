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

package com.bc.fiduceo.reader.amsr.amsre;

import java.util.HashMap;
import java.util.Map;

class VariableNamesConverter {

    private final HashMap<String, String> namesMap;

    VariableNamesConverter() {
        namesMap = new HashMap<>();
        namesMap.put("6.9V_Res.1_TB", "6_9V_Res_1_TB");
        namesMap.put("6.9H_Res.1_TB", "6_9H_Res_1_TB");
        namesMap.put("10.7V_Res.1_TB", "10_7V_Res_1_TB");
        namesMap.put("10.7H_Res.1_TB", "10_7H_Res_1_TB");
        namesMap.put("18.7V_Res.1_TB", "18_7V_Res_1_TB");
        namesMap.put("18.7H_Res.1_TB", "18_7H_Res_1_TB");
        namesMap.put("23.8V_Res.1_TB", "23_8V_Res_1_TB");
        namesMap.put("23.8H_Res.1_TB", "23_8H_Res_1_TB");
        namesMap.put("36.5V_Res.1_TB", "36_5V_Res_1_TB");
        namesMap.put("36.5H_Res.1_TB", "36_5H_Res_1_TB");
        namesMap.put("89.0V_Res.1_TB", "89_0V_Res_1_TB");
        namesMap.put("89.0H_Res.1_TB", "89_0H_Res_1_TB");
    }

    String toMms(String variableName) {
        final String convertedName = namesMap.get(variableName);
        if (convertedName != null) {
            return convertedName;
        }

        return variableName;
    }

    String toHdf(String variableName) {
        if (!namesMap.containsValue(variableName)) {
            return variableName;
        }

        for (Map.Entry<String, String> entry : namesMap.entrySet()) {
            if (entry.getValue().equals(variableName)) {
                return entry.getKey();
            }
        }

        throw new RuntimeException("Invalid state, we should never create here!");
    }
}
