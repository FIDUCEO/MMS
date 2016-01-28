package com.bc.fiduceo.reader;

public enum ReadersPlugin {
    METOP_B("M1"),
    METOP_A("M2"),
    TIROS_N("TN"),
    N6("NA"),
    NB("NB"),
    N7("NC"),
    N12("ND"),
    N8("NE"),
    N9("NF"),
    N10("NG"),
    N11("NH"),
    N14("NJ"),
    N15("NK"),
    N17("NM"),
    N16("NL"),
    N18("NN"),
    N19("NP");

    String type;

    ReadersPlugin(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

