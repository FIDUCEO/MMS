package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.store.FileSystemStore;
import com.bc.fiduceo.store.Store;
import com.bc.fiduceo.store.ZipStore;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipFile;

import static ucar.nc2.NetcdfFiles.openInMemory;

public class SlstrRegriddedSubsetReader implements Reader {

    private final ReaderContext readerContext;
    private final boolean nadirView;
    private Store store;
    private TreeMap<String, NetcdfFile> ncFiles;
    private ArrayList<Variable> variables;

    public SlstrRegriddedSubsetReader(ReaderContext readerContext, boolean nadirView) {
        this.readerContext = readerContext;
        this.nadirView = nadirView;
    }

    @Override
    public void open(File file) throws IOException {
        if (isZipFile(file)) {
            store = new ZipStore(file.toPath());
        } else {
            if (!file.isDirectory()) {
                file = file.getParentFile();
            }
            store = new FileSystemStore(file.toPath());
        }
        openNcFiles();
    }

    @Override
    public void close() throws IOException {
        store.close();
        store = null;
        ncFiles.clear();
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo info = new AcquisitionInfo();
//        info.
        return info;
    }

    @Override
    public String getRegEx() {
        return null;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        return null;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        return null;
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        return null;
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        return new int[0];
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return null;
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return null;
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        return null;
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        variables = new ArrayList<>();
        for (NetcdfFile netcdfFile : ncFiles.values()) {
            final int numRows = netcdfFile.findDimension("rows").getLength();
            final ucar.nc2.Dimension colsDim = netcdfFile.findDimension("columns");
            if (colsDim == null) {
                continue;
            }
            final int numCols = colsDim.getLength();
            final List<Variable> vars = netcdfFile.getVariables();
            for (Variable var : vars) {
                if (var.getShortName().contains("orphan")) {
                    continue;
                }
                final int[] shape = var.getShape();
                if (shape.length != 2 || shape[0] != numRows || shape[1] < 130) {
                    continue;
                }
                variables.add(var);
//                final String optT = shape[1] == 130 ? "\t\t" : "\t";
                System.out.println(var.getShortName());
//                System.out.print("    " + Arrays.toString(shape));
//                System.out.print(optT + (netcdfFile.findGlobalAttribute("resolution").getValues()));
//                System.out.print("\t" + (netcdfFile.findGlobalAttribute("track_offset").getValues()));
//                System.out.print(optT + (netcdfFile.findGlobalAttribute("start_offset").getValues()));
//                System.out.print("\t" + (netcdfFile.findGlobalAttribute("start_time").getStringValue()));
//                System.out.print("\t" + (netcdfFile.findGlobalAttribute("stop_time").getStringValue()));
//                System.out.println();
            }
        }
        return variables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        return null;
    }

    @Override
    public String getLongitudeVariableName() {
        return null;
    }

    @Override
    public String getLatitudeVariableName() {
        return null;
    }

    private boolean isZipFile(File file) {
        // Try with resource block to automatically close the ZipFile if it does not throw an exception
        try (ZipFile zipFile = new ZipFile(file)) {
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private void openNcFiles() throws IOException {
        final String suffix;
        if (nadirView) {
            suffix = "n.nc";
        } else {
            suffix = "o.nc";
        }
        final TreeSet<String> keys = store.getKeysEndingWith(suffix);
        final TreeSet<String> geoTx = store.getKeysEndingWith("geodetic_tx.nc");
        keys.addAll(geoTx);
        ncFiles = new TreeMap<>();
        for (String key : keys) {
            final byte[] bytes = store.getBytes(key);
            final String name = extractName(key);
            final NetcdfFile netcdfFile = openInMemory(key, bytes);
            ncFiles.put(name, netcdfFile);
        }
    }

    // package instead of private for testing purposes
    static String extractName(String key) {
        if (key.contains("\\")) {
            return key.substring(key.lastIndexOf("\\") + 1);
        } else {
            return key.substring(key.lastIndexOf("/") + 1);
        }
    }
}
