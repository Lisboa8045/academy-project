package com.academy.controllers;

import com.academy.models.service.ServiceImage;
import com.academy.services.MemberService;
import com.academy.services.ServiceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> uploadServiceImages(@RequestParam("id") Long id, @RequestParam("files") MultipartFile[] files) {

        List<String> savedFiles = new ArrayList<>();
        List<String> skippedFiles = new ArrayList<>();
        List<ServiceImage> serviceImages = new ArrayList<>();

        try {
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            int counter = 1;

            for (MultipartFile file : files) {
                String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
                String baseFilename = "service_" + id + "_" + counter + "." + extension;
                Path filePath = uploadPath.resolve(baseFilename);

                if (Files.exists(filePath)) {
                    skippedFiles.add(baseFilename);
                    counter++;
                    continue;
                }

                file.transferTo(filePath.toFile());
                savedFiles.add(baseFilename);
                ServiceImage image = new ServiceImage();
                image.setImage(baseFilename);
                image.setService(serviceService.getServiceEntityById(id));

                serviceImages.add(serviceImageRepository.save(image));
                counter++;
            }
            serviceService.saveImages(id, serviceImages);
            Map<String, Object> response = new HashMap<>();
            response.put("savedImages", savedFiles);
            response.put("skippedImages", skippedFiles);

            return ResponseEntity.ok().body(response);

        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
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
