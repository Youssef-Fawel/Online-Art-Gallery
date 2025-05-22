package com.epi.artgallery.service.impl;

import com.epi.artgallery.config.FileStorageConfig;
import com.epi.artgallery.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    private final FileStorageConfig fileStorageConfig;
    private Path root;

    @Autowired
    public FileStorageServiceImpl(FileStorageConfig fileStorageConfig) {
        this.fileStorageConfig = fileStorageConfig;
    }

    @PostConstruct
    @Override
    public void init() {
        try {
            String uploadDir = fileStorageConfig.getUploadDir();
            root = Paths.get(uploadDir);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
                logger.info("Created root upload directory: {}", root.toAbsolutePath());
            }

            // Create artists directory if it doesn't exist
            Path artistsDir = Paths.get(uploadDir, "artists");
            if (!Files.exists(artistsDir)) {
                Files.createDirectories(artistsDir);
                logger.info("Created artists upload directory: {}", artistsDir.toAbsolutePath());
            }

            // Create artworks directory if it doesn't exist
            Path artworksDir = Paths.get(uploadDir, "artworks");
            if (!Files.exists(artworksDir)) {
                Files.createDirectories(artworksDir);
                logger.info("Created artworks upload directory: {}", artworksDir.toAbsolutePath());
            }

            logger.info("Storage initialized at: {}", root.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Could not initialize storage location", e);
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file");
        }

        // Generate a unique filename to prevent overwriting
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + extension;

        // Create the target path and store the file
        Path destinationFile = root.resolve(Paths.get(filename)).normalize().toAbsolutePath();

        // Security check - make sure the file is being stored in the intended directory
        if (!destinationFile.getParent().equals(root.toAbsolutePath())) {
            throw new IOException("Cannot store file outside current directory");
        }

        Files.copy(file.getInputStream(), destinationFile);
        logger.info("Stored file: {}", filename);
        return filename;
    }

    @Override
    public Path load(String filename) {
        return root.resolve(filename);
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1)
                    .filter(path -> !path.equals(this.root))
                    .map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read stored files", e);
        }
    }

    @Override
    public boolean delete(String filename) {
        try {
            Path file = load(filename);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String filename, String directory) throws IOException {
        Path directoryPath = Paths.get(fileStorageConfig.getUploadDir(), directory);
        Path filePath = directoryPath.resolve(filename);
        logger.info("Attempting to delete file: {}", filePath);
        boolean deleted = Files.deleteIfExists(filePath);
        logger.info("File deleted: {}", deleted);
    }

    @Override
    public String storeFile(MultipartFile file, String directory) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file");
        }

        // Ensure the directory exists
        Path directoryPath = Paths.get(fileStorageConfig.getUploadDir(), directory);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // Generate a unique filename to prevent overwriting
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + extension;

        // Create the target path and store the file
        Path destinationFile = directoryPath.resolve(Paths.get(filename)).normalize().toAbsolutePath();

        // Security check - make sure the file is being stored in the intended directory
        if (!destinationFile.getParent().equals(directoryPath.toAbsolutePath())) {
            throw new IOException("Cannot store file outside current directory");
        }

        Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Stored file: {} in directory: {} at path: {}", filename, directory, destinationFile);
        return filename;
    }
}
