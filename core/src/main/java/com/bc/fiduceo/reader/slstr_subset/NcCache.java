package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.store.Store;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.io.CsvReader;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import static ucar.nc2.NetcdfFiles.openInMemory;

class NcCache {

    private final Charset charset = StandardCharsets.UTF_8;
    private final char[] separators = new char[]{'|'};

    private Store store;
    private RasterInfo rasterInfo;
    private HashMap<String, String> dddBMap;
    private HashMap<String, NetcdfFile> ncFilesMap;
    private HashMap<String, Variable> variablesMap;
    private HashMap<String, Array> rawArraysMap;
    private HashMap<String, Array> scaledArraysMap;

    void open(Store store, RasterInfo rasterInfo) throws IOException {
        this.store = store;
        this.rasterInfo = rasterInfo;

        parseDDDB();

        ncFilesMap = new HashMap<>();
        variablesMap = new HashMap<>();
        rawArraysMap = new HashMap<>();
        scaledArraysMap = new HashMap<>();
    }

    void close() throws IOException {
        dddBMap.clear();
        dddBMap = null;

        rawArraysMap.clear();
        rawArraysMap = null;
        scaledArraysMap.clear();
        scaledArraysMap = null;

        variablesMap.clear();
        variablesMap = null;

        for (final NetcdfFile ncFile : ncFilesMap.values()) {
            ncFile.close();
        }
        ncFilesMap.clear();
        ncFilesMap = null;

        if (store != null) {
            store.close();
            store = null;
        }
    }

    private void parseDDDB() throws IOException {
        final InputStream inputStream = getClass().getResourceAsStream("slstr_subset_dddb_100.txt");
        if (inputStream == null) {
            throw new IllegalStateException("The internal resource file could not be read.");
        }

        final CsvReader reader = new CsvReader(new InputStreamReader(inputStream, charset), separators, true, "#");
        final List<String[]> recordList = reader.readStringRecords();
        dddBMap = new HashMap<>(recordList.size());
        for (String[] record : recordList) {
            dddBMap.put(record[0], record[1]);
        }
    }

    List<String> getVariableNames() {
        return new ArrayList<>(dddBMap.keySet());
    }

    Variable getVariable(String variableName) throws IOException {
        Variable variable = variablesMap.get(variableName);
        if (variable != null) {
            return variable;
        } else {
            final String fileName = dddBMap.get(variableName);
            if (StringUtils.isNullOrEmpty(fileName)) {
                throw new IOException("Invalid variable name requested: " + variableName);
            }

            final NetcdfFile ncFile = getNetcdfFile(fileName);
            variable = ncFile.findVariable(variableName);
            if (variable == null) {
                throw new IOException("Invalid variable name requested: " + variableName);
            }

            if (isTiePointVariable(variableName)) {
                final double subSampling = (double) rasterInfo.tiePointResolution / (double) rasterInfo.rasterResolution;
                final double offset = rasterInfo.rasterTrackOffset - rasterInfo.tiePointTrackOffset * subSampling;
                variable = new SlstrSubsetTiePointVariable(variable, rasterInfo.rasterWidth, rasterInfo.rasterHeight, offset, subSampling);
            }
            variablesMap.put(variableName, variable);
            return variable;
        }
    }

    Array getRawArray(String variableName) throws IOException {
        Array array = rawArraysMap.get(variableName);
        if (array == null) {
            final Variable variable = getVariable(variableName);
            array = variable.read();
            rawArraysMap.put(variableName, array);
        }
        return array;
    }

    Array getScaledArray(String variableName) throws IOException {
        Array array = scaledArraysMap.get(variableName);
        if (array == null) {
            final Array rawArray = getRawArray(variableName);
            Variable variable = getVariable(variableName);
            array = NetCDFUtils.scaleIfNecessary(variable, rawArray);
            scaledArraysMap.put(variableName, array);
        }
        return array;
    }

    // package access for testing only tb 2022-08-18
    static boolean isTiePointVariable(String variableName) {
        return variableName.endsWith("_tn") || variableName.endsWith("_to");
    }

    private NetcdfFile getNetcdfFile(String fileName) throws IOException {
        NetcdfFile ncFile = ncFilesMap.get(fileName);
        if (ncFile == null) {
            final TreeSet<String> keys = store.getKeysEndingWith(fileName);
            final byte[] bytes = store.getBytes(keys.first());
            ncFile = openInMemory(fileName, bytes);
            ncFilesMap.put(fileName, ncFile);
        }
        return ncFile;
    }
}
