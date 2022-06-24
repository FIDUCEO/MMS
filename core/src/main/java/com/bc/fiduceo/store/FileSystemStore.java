package com.bc.fiduceo.store;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class FileSystemStore implements Store {

    private final Path internalRoot;

    public FileSystemStore(String path, FileSystem fileSystem) {
        if (fileSystem == null) {
            internalRoot = Paths.get(path);
        } else {
            internalRoot = fileSystem.getPath(path);
        }
    }

    public FileSystemStore(Path rootPath) {
        internalRoot = rootPath;
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
}
