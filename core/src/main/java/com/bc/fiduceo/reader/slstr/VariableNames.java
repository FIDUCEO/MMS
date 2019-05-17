package com.bc.fiduceo.reader.slstr;

import java.util.ArrayList;
import java.util.List;

class VariableNames {

    private final List<String> variableNames;

    VariableNames() {
        variableNames = new ArrayList<>();
        variableNames.add("latitude_tx");
        variableNames.add("longitude_tx");
        variableNames.add("sat_azimuth_tn");
        variableNames.add("sat_zenith_tn");
        variableNames.add("solar_azimuth_tn");
        variableNames.add("solar_zenith_tn");
        variableNames.add("S7_BT_in");
        variableNames.add("S8_BT_in");
        variableNames.add("S9_BT_in");
        variableNames.add("S7_exception_in");
        variableNames.add("S8_exception_in");
        variableNames.add("S9_exception_in");
        variableNames.add("S1_radiance_an");
        variableNames.add("S2_radiance_an");
        variableNames.add("S3_radiance_an");
        variableNames.add("S4_radiance_an");
        variableNames.add("S5_radiance_an");
        variableNames.add("S6_radiance_an");
        variableNames.add("S1_exception_an");
        variableNames.add("S2_exception_an");
        variableNames.add("S3_exception_an");
        variableNames.add("S4_exception_an");
        variableNames.add("S5_exception_an");
        variableNames.add("S6_exception_an");
        variableNames.add("sat_azimuth_to");
        variableNames.add("sat_zenith_to");
        variableNames.add("solar_azimuth_to");
        variableNames.add("solar_zenith_to");
        variableNames.add("S7_BT_io");
        variableNames.add("S8_BT_io");
        variableNames.add("S9_BT_io");
        variableNames.add("S7_exception_io");
        variableNames.add("S8_exception_io");
        variableNames.add("S9_exception_io");
        variableNames.add("S1_radiance_ao");
        variableNames.add("S2_radiance_ao");
        variableNames.add("S3_radiance_ao");
        variableNames.add("S4_radiance_ao");
        variableNames.add("S5_radiance_ao");
        variableNames.add("S6_radiance_ao");
        variableNames.add("S1_exception_ao");
        variableNames.add("S2_exception_ao");
        variableNames.add("S3_exception_ao");
        variableNames.add("S4_exception_ao");
        variableNames.add("S5_exception_ao");
        variableNames.add("S6_exception_ao");
        variableNames.add("confidence_in");
        variableNames.add("pointing_in");
        variableNames.add("bayes_in");
        variableNames.add("cloud_in");
        variableNames.add("bayes_io");
        variableNames.add("cloud_io");
    }

    boolean isValidName(String variableName) {
        return variableNames.contains(variableName);
    }
}
