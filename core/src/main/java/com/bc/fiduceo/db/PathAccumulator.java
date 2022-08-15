package com.bc.fiduceo.db;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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

    /**
     * Retrieves a list of database satellite paths not matching the search path,
     * list is ordered by number of paths, descending
     *
     * @return the list
     */
    List<PathCount> getMisses() {
        final Collection<PathCount> values = missedCollector.values();
        final ArrayList<PathCount> missesArray = new ArrayList<>(values);
        missesArray.sort((o1, o2) -> o2.getCount() - o1.getCount());
        return missesArray;
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
