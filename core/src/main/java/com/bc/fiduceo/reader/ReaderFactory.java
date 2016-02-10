package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.ServicesUtils;
import org.esa.snap.core.util.io.WildcardMatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;


public class ReaderFactory<T, V> {

    public Reader createReader(String sensor) {
        ServicesUtils<Reader> servicesUtils = new ServicesUtils<>();
        return servicesUtils.getServices(Reader.class, sensor);
    }

    public File[] getSearchResult(File systemConfig, String search) throws IOException {
        String archiveRoot = systemConfig.getAbsolutePath();
        File[] glob = null;
        String regex = null;
        String[] sen_sat = search.split(" ");
        if (sen_sat.length > 1) {
            String type = ReadersPlugin.valueOf(sen_sat[1].toUpperCase()).getType();
            if (sen_sat[0].contains("amsu")) {
                glob = WildcardMatcher.glob(archiveRoot + File.separator + "*.h5");
                regex = "'*[A-Z].+[AMBX]." + type + ".D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.+[GC|WI|MM].h5";
            } else if (search.contains("mhs")) {
                glob = WildcardMatcher.glob(archiveRoot + File.separator + "*.h5");
                regex = "'*[A-Z].*MHSX." + type + ".D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.+[GC|WI|MM].h5";
            }
        } else {
            glob = WildcardMatcher.glob(archiveRoot + File.separator + "*.hdf");
            regex = "AIRS.\\d{4}.\\d{2}.\\d{2}.\\d{3}.L1B.*.hdf";
        }
        if (Objects.isNull(glob)) {
            return null;
        }
        List<File> inputFileList = new ArrayList<>();
        for (File file : glob) {
            if (file.getCanonicalFile().getName().matches(regex)) {
                inputFileList.add(file);
            }
        }
        return inputFileList.toArray(new File[inputFileList.size()]);
    }

    //todo mb: to initialze injection
    HashMap<T, V> initializeInjection(String search) throws IOException {
        HashMap<File[], Reader> vHashMap = new LinkedHashMap<>();
        File[] searchResult = getSearchResult(null, search);
        String[] split = search.split(" ");
        Reader reader = createReader(split[0]);
        vHashMap.put(searchResult, reader);
        return (HashMap<T, V>) vHashMap;
    }
}
