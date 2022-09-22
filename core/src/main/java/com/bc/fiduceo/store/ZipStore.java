package com.bc.fiduceo.store;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ZipStore implements Store {
    private final FileSystem zfs;
    private final Path internalRoot;

    public ZipStore(Path zipFilePath) throws IOException {
        final HashMap<String, String> zipParams = new HashMap<>();
        if (!Files.exists(zipFilePath)) {
            zipParams.put("create", "true");
        }
        final URI uri = URI.create("jar:file:" + zipFilePath.toUri().getPath());
        zfs = getFileSystem(zipParams, uri);
        internalRoot = zfs.getRootDirectories().iterator().next();
    }

    @Override
    public byte[] getBytes(String key) throws IOException {
        final Path path = internalRoot.resolve(key);
        if (Files.isReadable(path)) {
            return Files.readAllBytes(path);
        }
        return null;
    }

    @Override
    public TreeSet<String> getKeysEndingWith(String suffix) throws IOException {
        return Files.walk(internalRoot)
                .filter(path -> path.toString().endsWith(suffix))
                .map(path -> internalRoot.relativize(path).toString())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public void close() throws IOException {
        zfs.close();
    }

    // Gets or creates a FileSystem
    private FileSystem getFileSystem(HashMap<String, String> zipParams, URI uri) throws IOException {
        try {
            return FileSystems.newFileSystem(uri, zipParams);
        } catch (FileSystemAlreadyExistsException e) {
            return FileSystems.getFileSystem(uri);
        }
    }
}
