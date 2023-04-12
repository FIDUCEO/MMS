package com.bc.fiduceo.reader.insitu.tao.preproc;

class TAORecord {
    int date;
    String SSS;
    String SST;
    String AIRT;
    String RH;
    String WSPD;
    String WDIR;
    String BARO;
    String Q;
    String M;

    String toLine() {
        return date + " " + SSS + " " + SST + " " + AIRT + " " + RH + " " + WSPD + " " + WDIR + " " + BARO + " " +Q + " " + M;
    }
}
