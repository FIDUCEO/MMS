package com.bc.fiduceo.reader.slstr;

import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.reader.slstr.VariableType.*;

class VariableNames {

    private final List<String> nadir1kmNames;
    private final List<String> nadir500mNames;
    private final List<String> oblique1kmNames;
    private final List<String> oblique500mNames;

    VariableNames() {
        nadir500mNames = new ArrayList<>();
        nadir500mNames.add("latitude_tx");
        nadir500mNames.add("longitude_tx");
        nadir500mNames.add("sat_azimuth_tn");
        nadir500mNames.add("sat_zenith_tn");
        nadir500mNames.add("solar_azimuth_tn");
        nadir500mNames.add("solar_zenith_tn");
        nadir500mNames.add("S1_radiance_an");
        nadir500mNames.add("S2_radiance_an");
        nadir500mNames.add("S3_radiance_an");
        nadir500mNames.add("S4_radiance_an");
        nadir500mNames.add("S5_radiance_an");
        nadir500mNames.add("S6_radiance_an");
        nadir500mNames.add("S1_exception_an");
        nadir500mNames.add("S2_exception_an");
        nadir500mNames.add("S3_exception_an");
        nadir500mNames.add("S4_exception_an");
        nadir500mNames.add("S5_exception_an");
        nadir500mNames.add("S6_exception_an");

        nadir1kmNames = new ArrayList<>();
        nadir1kmNames.add("S7_BT_in");
        nadir1kmNames.add("S8_BT_in");
        nadir1kmNames.add("S9_BT_in");
        nadir1kmNames.add("S7_exception_in");
        nadir1kmNames.add("S8_exception_in");
        nadir1kmNames.add("S9_exception_in");
        nadir1kmNames.add("confidence_in");
        nadir1kmNames.add("pointing_in");
        nadir1kmNames.add("bayes_in");
        nadir1kmNames.add("cloud_in");

        oblique500mNames = new ArrayList<>();
        oblique500mNames.add("sat_azimuth_to");
        oblique500mNames.add("sat_zenith_to");
        oblique500mNames.add("solar_azimuth_to");
        oblique500mNames.add("solar_zenith_to");
        oblique500mNames.add("S1_radiance_ao");
        oblique500mNames.add("S2_radiance_ao");
        oblique500mNames.add("S3_radiance_ao");
        oblique500mNames.add("S4_radiance_ao");
        oblique500mNames.add("S5_radiance_ao");
        oblique500mNames.add("S6_radiance_ao");
        oblique500mNames.add("S1_exception_ao");
        oblique500mNames.add("S2_exception_ao");
        oblique500mNames.add("S3_exception_ao");
        oblique500mNames.add("S4_exception_ao");
        oblique500mNames.add("S5_exception_ao");
        oblique500mNames.add("S6_exception_ao");

        oblique1kmNames = new ArrayList<>();
        oblique1kmNames.add("S7_BT_io");
        oblique1kmNames.add("S8_BT_io");
        oblique1kmNames.add("S9_BT_io");
        oblique1kmNames.add("S7_exception_io");
        oblique1kmNames.add("S8_exception_io");
        oblique1kmNames.add("S9_exception_io");
        oblique1kmNames.add("bayes_io");
        oblique1kmNames.add("cloud_io");
    }

    boolean isValidName(String variableName) {
        return nadir500mNames.contains(variableName) ||
                nadir1kmNames.contains(variableName) ||
                oblique500mNames.contains(variableName) ||
                oblique1kmNames.contains(variableName);
    }

    VariableType getVariableType(String variableName) {
        if (nadir500mNames.contains(variableName)) {
            return NADIR_500m;
        } else if (nadir1kmNames.contains(variableName)) {
            return NADIR_1km;
        } else if (oblique500mNames.contains(variableName)) {
            return OBLIQUE_500m;
        } else if (oblique1kmNames.contains(variableName)) {
            return OBLIQUE_1km;
        }
        throw new RuntimeException("Requested variable not supported: " + variableName);
    }
}
