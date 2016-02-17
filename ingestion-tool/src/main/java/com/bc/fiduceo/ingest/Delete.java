package com.bc.fiduceo.ingest;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class Delete {
    public static void main(String... arg) throws IOException {
        File file = new File("D:\\Data\\fiduceo\\fiduceo_H5");
        FileFinder fileFinder = new FileFinder("'?[A-Z].+[AMBX|MHSX].+[NK|M1].D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.+[MM|WI|GC].h5");
        Files.walkFileTree(file.toPath(), fileFinder);
        System.out.println("fileFinder = " + fileFinder.getFileList());

    }

    private static class FileFinder extends SimpleFileVisitor<Path> {
        private final PathMatcher matcher;
        List<File> fileList = new ArrayList<>();

        public FileFinder(String pattern) {
            matcher = FileSystems.getDefault().getPathMatcher("regex:" + pattern);
        }

        void find(Path file) {
            Path name = file.getFileName();
            if (name != null && matcher.matches(name)) {
                fileList.add(file.toFile());
            }
        }

        public List<File> getFileList() {
            return fileList;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            find(file);
            return FileVisitResult.CONTINUE;


        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            System.err.println(exc);
            return FileVisitResult.CONTINUE;
        }
    }
}
