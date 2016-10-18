package com.bc.fiduceo.matchup.writer;

import ucar.ma2.Array;

public interface Target {

    void write(Array data, String variableName, int zIndex);

    void write(int data, String variableName, int zIndex);

    void write(float data, String variableName, int zIndex);

    void write(String data, String variableName, int zIndex);
}
