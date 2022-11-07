package com.bc.fiduceo.reader.insitu.sic_cci;

import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.List;

abstract class AbstractSectionParser {

    static final int[] SCALAR = new int[1];

    abstract List<Variable> getVariables();

    abstract Section parse(String[] tokens) throws ParseException;
}
