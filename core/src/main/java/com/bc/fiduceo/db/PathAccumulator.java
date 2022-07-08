package com.bc.fiduceo.db;

import java.io.File;
import java.util.*;

class PathAccumulator {

    private final PathCount match;
    private final int numPathSegments;
    private final HashMap<String, PathCount> missedCollector;

    PathAccumulator(String pathToMatch, int numPathSegments) {
        match = new PathCount(pathToMatch, 0);
        missedCollector = new HashMap<>();

        this.numPathSegments = numPathSegments;
    }

    void addMatch() {
        match.addCount();
    }

    void addMiss(String path) {
        final String strippedPath = stripPath(path, numPathSegments);

        final PathCount pathCount = missedCollector.get(strippedPath);
        if (pathCount == null) {
            missedCollector.put(strippedPath, new PathCount(strippedPath, 1));
        } else {
            pathCount.addCount();
        }
    }

    PathCount getMatches() {
        return match;
    }

    List<PathCount> getMisses() {
        final Collection<PathCount> values = missedCollector.values();
        return new ArrayList<>(values);
    }

    // package access for testing only tb 2022-07-08
    static String stripPath(String path, int numElements) {
        int pos = 0;
        int n = 0;
        while (n < numElements && pos != -1) {
            pos = path.indexOf(File.separator, pos + 1);
            n++;
        }

        if (pos == -1) {
            return path;
        } else {
            return path.substring(0, pos);
        }
    }
}
