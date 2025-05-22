package com.epi.artgallery.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileStorageService {
    void init();

    String store(MultipartFile file) throws IOException;

    Path load(String filename);

    void deleteAll();

    Stream<Path> loadAll();

    boolean delete(String filename);

    // Change the parameter type from boolean to String
    void deleteFile(String filename, String directory) throws IOException;

    String storeFile(MultipartFile file, String directory) throws IOException;
}
