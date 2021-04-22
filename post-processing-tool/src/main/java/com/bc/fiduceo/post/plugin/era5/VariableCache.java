package com.bc.fiduceo.post.plugin.era5;

import org.esa.snap.core.util.io.FileUtils;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class VariableCache {

    private final Era5Archive archive;
    private final HashMap<String, CacheContainer> cache;
    private final int cacheSize;


    VariableCache(Era5Archive archive, int cacheSize) {
        this.archive = archive;
        this.cacheSize = cacheSize;
        cache = new HashMap<>();
    }

    CacheEntry get(String variableKey, int era5TimeStamp) throws IOException {
        final CacheContainer cacheContainer = getCacheContainer(variableKey, era5TimeStamp);
        return cacheContainer.cacheEntry;
    }

    private CacheContainer getCacheContainer(String variableKey, int era5TimeStamp) throws IOException {
        final String filePath = archive.get(variableKey, era5TimeStamp);
        final String variableName = getVariableName(variableKey);
        final String key = FileUtils.getFilenameWithoutExtension(new File(filePath));

        CacheContainer cacheContainer = cache.get(key);
        if (cacheContainer == null) {
            final NetcdfFile netcdfFile = NetcdfFile.open(filePath);
            final Variable variable = netcdfFile.findVariable(variableName);
            if (variable == null) {
                throw new IOException("variable not found: " + variableName + "  " + filePath);
            }
            if (cache.size() == cacheSize) {
                removeOldest();
            }
            final Array array = variable.read().reduce();

            cacheContainer = new CacheContainer(variable, netcdfFile, array, System.currentTimeMillis());
            cache.put(key, cacheContainer);
        }

        cacheContainer.lastAccess = System.currentTimeMillis();
        return cacheContainer;
    }

    void close() throws IOException {
        final Collection<CacheContainer> cacheEntries = cache.values();
        for (CacheContainer cacheContainer : cacheEntries) {
            cacheContainer.netcdfFile.close();
            cacheContainer.netcdfFile = null;
        }

        cache.clear();
    }

    private String getVariableName(String variableKey) {
        final int cutPoint = variableKey.lastIndexOf("_");
        return variableKey.substring(cutPoint + 1, variableKey.length());
    }

    private void removeOldest() throws IOException {
        long minTime = Long.MAX_VALUE;
        String toRemove = null;
        CacheContainer entryToRemove = null;
        final Set<Map.Entry<String, CacheContainer>> cacheEntries = cache.entrySet();
        for (Map.Entry<String, CacheContainer> cacheMapEntry : cacheEntries) {
            final CacheContainer cacheContainer = cacheMapEntry.getValue();
            if (cacheContainer.lastAccess < minTime) {
                minTime = cacheContainer.lastAccess;
                toRemove = cacheMapEntry.getKey();
                entryToRemove = cacheContainer;
            }
        }

        if (entryToRemove != null) {
            entryToRemove.cacheEntry = null;
            entryToRemove.netcdfFile.close();

            cache.remove(toRemove);
        }
    }

    static class CacheEntry {

        final Variable variable;
        final Array array;

        CacheEntry(Variable variable, Array array) {
            this.variable = variable;
            this.array = array;
        }
    }

    private static class CacheContainer {
        CacheEntry cacheEntry;
        NetcdfFile netcdfFile;
        long lastAccess;

        CacheContainer(Variable variable, NetcdfFile netcdfFile, Array array, long lastAccess) {
            this.cacheEntry = new CacheEntry(variable, array);
            this.netcdfFile = netcdfFile;
            this.lastAccess = lastAccess;
        }
    }
}
