package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.util.io.FileUtils;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static com.bc.fiduceo.post.plugin.era5.Era5Archive.mapVariable;

class VariableCache {

    private final Era5Archive archive;
    private final HashMap<String, CacheEntry> cache;


    VariableCache(Era5Archive archive) {
        this.archive = archive;
        cache = new HashMap<>();
    }

    Variable get(String variableKey, int era5TimeStamp) throws IOException {
        final String filePath = archive.get(variableKey, era5TimeStamp);

        final int cutPoint = variableKey.lastIndexOf("_");
        String variableName = variableKey.substring(cutPoint + 1, variableKey.length());
        variableName = mapVariable(variableName);

        final File file = new File(filePath);
        final String key = FileUtils.getFilenameWithoutExtension(file);
        CacheEntry cacheEntry = cache.get(key);
        if (cacheEntry == null) {
            final NetcdfFile netcdfFile = NetcdfFile.open(filePath);
            final Variable variable = netcdfFile.findVariable(variableName);
            if (variable == null) {
                throw new IOException("variable not found: " + variableName + "  " + filePath);
            }
            cacheEntry = new CacheEntry(variable, netcdfFile, System.currentTimeMillis());
            cache.put(key, cacheEntry);
        }

        return cacheEntry.variable;
    }

    private class CacheEntry {
        Variable variable;
        NetcdfFile netcdfFile;
        long lastAccess;

        CacheEntry(Variable variable, NetcdfFile netcdfFile, long lastAccess) {
            this.variable = variable;
            this.netcdfFile = netcdfFile;
            this.lastAccess = lastAccess;
        }
    }
}
