package com.academy.services;

import com.academy.models.service.ServiceImage;
import com.academy.repositories.ServiceImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class UploadImagesService {
    @Lazy
    private final ServiceService serviceService;
    private final ServiceImageRepository serviceImageRepository;
    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${app.url:}")                 // optional absolute base (empty = use current request)
    private String appBaseUrl;
    private String imagesPublicPath = "/auth/uploads";

    private final MemberService memberService;

    public UploadImagesService(MemberService memberService, ServiceService serviceService, ServiceImageRepository serviceImageRepository) {
        this.memberService = memberService;
        this.serviceService = serviceService;
        this.serviceImageRepository = serviceImageRepository;
    }

    public Map<String, Object> uploadServiceImages(Long id, MultipartFile[] files) {
        deleteImagesFromService(id);
        List<String> savedFiles = new ArrayList<>();

        if (Objects.isNull(files)) {
            return Map.of("savedImages", savedFiles);
        }
        try {
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            int counter = 1;

            for (MultipartFile file : files) {
                String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
                String baseFilename = "service_" + id + "_" + counter++ + "." + extension;
                Path filePath = uploadPath.resolve(baseFilename);

                file.transferTo(filePath.toFile());
                savedFiles.add(baseFilename);
                ServiceImage image = new ServiceImage();
                image.setImage(baseFilename);
                image.setService(serviceService.getServiceEntityById(id));

                serviceImageRepository.save(image);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("savedImages", savedFiles);

            return response;

        } catch (IOException e) {
            return Map.of("error", e.getMessage());
        }
    }

    void deleteImagesFromService(Long serviceId) {
        serviceImageRepository.deleteServiceImageByServiceId(serviceId);
        File directory = new File(uploadDir);
        File[] files = directory.listFiles((dir, name) -> name.startsWith("service_" + serviceId + "_"));

        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }

    }

    public Map<String, String> uploadImage(Long id, MultipartFile file) {
        try {
            System.out.println("Uploading file: " + file.getOriginalFilename());
            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String filename = "profile_" + id + "." + extension;
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());
            memberService.saveProfilePic(id,filename);
            return Map.of("imageUrl", filename);
        } catch (IOException e) {
            return Map.of("error", e.getMessage());
        }
    }

    public ResponseEntity<Resource> getImage(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public String getImageUrl(String filename) {
        if (filename == null || filename.isBlank()) return null;
        String base = appBaseUrl.endsWith("/") ? appBaseUrl.substring(0, appBaseUrl.length()-1) : appBaseUrl;
        String path = imagesPublicPath.startsWith("/") ? imagesPublicPath : "/" + imagesPublicPath;
        if (!path.endsWith("/")) path += "/";
        return base + path + filename;
    }
}
