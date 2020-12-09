package com.bc.fiduceo.post.plugin.era5;

import org.esa.snap.core.util.io.FileUtils;
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
    private final HashMap<String, CacheEntry> cache;
    private final int cacheSize;


    VariableCache(Era5Archive archive, int cacheSize) {
        this.archive = archive;
        this.cacheSize = cacheSize;
        cache = new HashMap<>();
    }

    Variable get(String variableKey, int era5TimeStamp) throws IOException {
        final String filePath = archive.get(variableKey, era5TimeStamp);
        final String variableName = getVariableName(variableKey);
        final String key = FileUtils.getFilenameWithoutExtension(new File(filePath));

        CacheEntry cacheEntry = cache.get(key);
        if (cacheEntry == null) {
            final NetcdfFile netcdfFile = NetcdfFile.open(filePath);
            final Variable variable = netcdfFile.findVariable(variableName);
            if (variable == null) {
                throw new IOException("variable not found: " + variableName + "  " + filePath);
            }
            if (cache.size() == cacheSize) {
                removeOldest();
            }
            cacheEntry = new CacheEntry(variable, netcdfFile, System.currentTimeMillis());
            cache.put(key, cacheEntry);
        }

        cacheEntry.lastAccess = System.currentTimeMillis();
        return cacheEntry.variable;
    }



    void close() throws IOException {
        final Collection<CacheEntry> cacheEntries = cache.values();
        for (CacheEntry cacheEntry : cacheEntries) {
            cacheEntry.netcdfFile.close();
            cacheEntry.netcdfFile = null;
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
        CacheEntry entryToRemove = null;
        final Set<Map.Entry<String, CacheEntry>> cacheEntries = cache.entrySet();
        for (Map.Entry<String, CacheEntry> cacheMapEntry : cacheEntries) {
            final CacheEntry cacheEntry = cacheMapEntry.getValue();
            if (cacheEntry.lastAccess < minTime) {
                minTime = cacheEntry.lastAccess;
                toRemove = cacheMapEntry.getKey();
                entryToRemove = cacheEntry;
            }
        }

        if (entryToRemove != null) {
            System.out.println("close = " + entryToRemove.netcdfFile.getLocation());
            entryToRemove.netcdfFile.close();

            cache.remove(toRemove);
        }
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
