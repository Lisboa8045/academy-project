package com.academy.controllers;

import com.academy.services.UploadImagesService;
import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/auth/uploads")
public class UploadImagesController {
    private final UploadImagesService uploadImagesService;

    public UploadImagesController(UploadImagesService uploadImagesService) {
        this.uploadImagesService = uploadImagesService;
    }

    @PostMapping("/service-image")
    @Transactional
    public ResponseEntity<Map<String, Object>> uploadServiceImages(@RequestParam("id") Long id,
                                                                   @RequestParam(name = "files", required = false) MultipartFile[] files) {
        Map<String, Object> response = uploadImagesService.uploadServiceImages(id, files);
        if (response.containsKey("error")) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("id") Long id, @RequestBody MultipartFile file) {
        Map<String, String> response = uploadImagesService.uploadImage(id, file);
        if (response.containsKey("error")) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        return uploadImagesService.getImage(filename);
    }
}
