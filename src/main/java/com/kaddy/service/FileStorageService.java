package com.kaddy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IOException("File must have a name");
        }

        originalFilename = StringUtils.cleanPath(originalFilename);

        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        try {
            if (originalFilename.contains("..")) {
                throw new IOException("Filename contains invalid path sequence " + originalFilename);
            }

            Path uploadPath = Paths.get(uploadDir, subDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path targetLocation = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return subDirectory + "/" + uniqueFilename;
        } catch (IOException ex) {
            throw new IOException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }

    public void deleteFile(String filePath) throws IOException {
        try {
            Path file = Paths.get(uploadDir, filePath);
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            throw new IOException("Could not delete file " + filePath, ex);
        }
    }

    public Path loadFile(String filePath) {
        return Paths.get(uploadDir, filePath);
    }

    public boolean fileExists(String filePath) {
        Path file = Paths.get(uploadDir, filePath);
        return Files.exists(file);
    }
}
