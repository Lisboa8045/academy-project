package com.academy.controllers;

import com.academy.dtos.service.ServiceResponseDTO;
import com.academy.services.ServiceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    private final ServiceService serviceService;

    public ChatbotController(ServiceService serviceService){
        this.serviceService = serviceService;
    }

    @GetMapping("/services")
    public List<ServiceResponseDTO> getAllServices(){
        return serviceService.getAllEnabled();
    }
}
