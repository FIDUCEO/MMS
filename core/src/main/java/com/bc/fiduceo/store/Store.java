package com.bc.fiduceo.store;

import java.io.Closeable;
import java.io.IOException;
import java.util.TreeSet;

public interface Store extends Closeable {

    byte[] getBytes(String key) throws IOException;

    TreeSet<String> getKeysEndingWith(String suffix) throws IOException;

    @Override
    default void close() throws IOException {
    }
}
