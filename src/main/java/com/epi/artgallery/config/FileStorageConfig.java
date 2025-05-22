package com.epi.artgallery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String getUploadDir() {
        return uploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Make sure the path ends with a slash for proper resource resolution
        String path = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";

        // This maps the URL path /uploads/** to the physical location where files are stored
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./" + path);

        System.out.println("Configured resource handler: /uploads/** -> file:./" + path);
    }
}
