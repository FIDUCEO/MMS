package com.bc.fiduceo.reader.insitu.tao.preproc;

class TAORecord {
    int date;
    String lon;
    String lat;
    String SSS;
    String SST;
    String AIRT;
    String RH;
    String WSPD;
    String WDIR;
    String BARO;
    String RAIN;
    String Q;
    String M;

    String toLine() {
        return date + " " + lon + " " + lat + " " + SSS + " " + SST + " " + AIRT + " " + RH + " " + WSPD + " " + WDIR + " " + BARO + " " + RAIN + " " + Q + " " + M;
    }
}
