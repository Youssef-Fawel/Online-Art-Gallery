package com.epi.artgallery.config;

import com.epi.artgallery.config.FileStorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageConfig fileStorageConfig) {
        this.fileStorageLocation = Paths.get(fileStorageConfig.getUploadDir()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, String category) {
        try {
            if (file.isEmpty()) {
                return null;
            }

            // Generate a unique filename
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName != null && originalFileName.contains(".")
                    ? originalFileName.substring(originalFileName.lastIndexOf("."))
                    : "";

            String fileName = UUID.randomUUID().toString() + fileExtension;

            // Create category folder if not exists
            Path categoryFolder = this.fileStorageLocation.resolve(category);
            if (!Files.exists(categoryFolder)) {
                Files.createDirectories(categoryFolder);
            }

            // Copy file to the target location inside category folder
            Path targetLocation = categoryFolder.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return category + "/" + fileName; // Store the relative path (e.g., "artists/abc123.jpg")
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path fullPath = this.fileStorageLocation.resolve(filePath);
            Files.deleteIfExists(fullPath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file: " + filePath, ex);
        }
    }

    public Path getFilePath(String fileName) {
        return this.fileStorageLocation.resolve(fileName);
    }
}
