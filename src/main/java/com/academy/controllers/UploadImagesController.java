package com.academy.controllers;

import com.academy.models.service.ServiceImage;
import com.academy.services.MemberService;
import com.academy.services.ServiceService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

@RestController
@RequestMapping("/auth/uploads")
public class UploadImagesController {

    private final ServiceService serviceService;
    private final ServiceImageRepository serviceImageRepository;
    @Value("${file.upload-dir}")
    private String uploadDir;

    private final MemberService memberService;

    public UploadImagesController(MemberService memberService, ServiceService serviceService, ServiceImageRepository serviceImageRepository) {
        this.memberService = memberService;
        this.serviceService = serviceService;
        this.serviceImageRepository = serviceImageRepository;
    }

    @PostMapping("/service-image")
    @Transactional
    public ResponseEntity<Map<String, Object>> uploadServiceImages(@RequestParam("id") Long id, @RequestParam(name = "files", required = false) MultipartFile[] files) {
        deleteImagesFromService(id);
        List<String> savedFiles = new ArrayList<>();

        if (Objects.isNull(files)) {
            return ResponseEntity.ok().body(Map.of("savedImages", savedFiles));
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

            return ResponseEntity.ok().body(response);

        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
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

    @PostMapping("/profile-picture")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("id") Long id, @RequestBody MultipartFile file) {
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
            return ResponseEntity.ok().body(Map.of("imageUrl", filename));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
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
}
