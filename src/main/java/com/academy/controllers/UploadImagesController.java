package com.academy.controllers;

import com.academy.services.MemberService;
import com.academy.services.ServiceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController

@RequestMapping("/auth/uploads")

public class UploadImagesController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final MemberService memberService;

    private final ServiceService serviceService;

    public UploadImagesController(MemberService memberService,  ServiceService serviceService) {

        this.memberService = memberService;
        this.serviceService = serviceService;
    }

//    @PostMapping("/{id}/upload-images")
//    public ResponseEntity<Map<String, Object>> uploadServiceImages(@PathVariable Long id, @RequestParam("files") MultipartFile[] files) {
//        List<ServiceImages> imageFilenames = new ArrayList<>();
//
//        try {
//            Path uploadPath = Paths.get(uploadDir + "/services/" + id);
//            if (!Files.exists(uploadPath)) {
//                Files.createDirectories(uploadPath);
//            }
//            for (MultipartFile file : files) {
//                String originalName = file.getOriginalFilename();
//                String extension = StringUtils.getFilenameExtension(originalName);
//                String filename = "service_" + id + "_" + DateFormat.getDateInstance() + "." + extension;
//
//                Path filePath = uploadPath.resolve(filename);
//                file.transferTo(filePath.toFile());
//
//                imageFilenames.add("/uploads/services/" + id + "/" + filename);
//            }
//            serviceService.saveImages(id,imageFilenames);
//
//            return ResponseEntity.ok(Map.of("uploadedImages", imageFilenames));
//
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Erro ao fazer upload: " + e.getMessage()));
//        }
//
//    }
//
//    @PostMapping("/profile-picture")
//
//    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("id") Long id, @RequestBody MultipartFile file) {
//
//        try {
//
//            System.out.println("Uploading file: " + file.getOriginalFilename());
//
//            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
//
//            String filename = "profile_" + id + "." + extension;
//
//            Path uploadPath = Paths.get(uploadDir);
//
//            if (!Files.exists(uploadPath)) {
//
//                Files.createDirectories(uploadPath);
//
//            }
//
//            Path filePath = uploadPath.resolve(filename);
//
//            file.transferTo(filePath.toFile());
//
//            memberService.saveProfilePic(id,filename);
//
//            return ResponseEntity.ok().body(Map.of("imageUrl", filename));
//
//        } catch (IOException e) {
//
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
//
//        }
//
//    }

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

