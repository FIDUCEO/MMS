package com.bc.fiduceo.db;

class PathCount {

    final String path;
    int count;

    public PathCount(String path, int count) {
        this.path = path;
        this.count = count;
    }

    public String getPath() {
        return path;
    }

    public int getCount() {
        return count;
    }

    public void addCount() {
        count += 1;
    }
}
