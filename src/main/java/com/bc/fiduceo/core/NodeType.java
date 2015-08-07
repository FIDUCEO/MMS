package com.bc.fiduceo.core;


public enum NodeType {
    ASCENDING,
    DESCENDING,
    UNDEFINED;

    public int toId() {
        return this.ordinal();
    }


    public static NodeType fromId(int id) {
        if (id == 0) {
            return ASCENDING;
        } else if (id == 1) {
            return DESCENDING;
        }

        return UNDEFINED;
    }
}


