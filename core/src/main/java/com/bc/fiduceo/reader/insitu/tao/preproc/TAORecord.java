package com.bc.fiduceo.reader.insitu.tao.preproc;

class TAORecord {
    int date;
    String SSS;
    String SST;
    String AIRT;
    String RH;
    String Q;
    String M;

    String toLine() {
        return date + " " + SSS + " " + SST + " " + AIRT + " " + RH + " " + Q + " " + M;
    }
}
