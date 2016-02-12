package com.bc.fiduceo.reader;

public enum ReadersPlugin {
    METOP_B("M1"),
    METOP_A("M2"),
    TIROS_N("TN"),
    NOAA_6("NA"),
    NOAA_B("NB"),
    NOAA_7("NC"),
    NOAA_12("ND"),
    NOAA_8("NE"),
    NOAA_9("NF"),
    NOAA_10("NG"),
    NOAA_11("NH"),
    NOAA_14("NJ"),
    NOAA_15("NK"),
    NOAA_17("NM"),
    NOAA_16("NL"),
    NOAA_18("NN"),
    NOAA_19("NP"),
    EUMETSAT("EUMETSAT"),
    AIRS("AIRS");

    String type;

    ReadersPlugin(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

