package com.academy.controllers;

import com.academy.services.MemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.academy.models.Member;

import java.io.*;
import java.nio.file.*;
import java.util.Map;

@RestController
@RequestMapping("/auth/uploads")
public class UploadImagesController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final MemberService memberService;

    public UploadImagesController(MemberService memberService) {
        this.memberService = memberService;
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
