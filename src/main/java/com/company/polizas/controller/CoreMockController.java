package com.company.polizas.controller;

import com.company.polizas.dto.CoreEventoRequest;
import com.company.polizas.service.CoreMockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/core-mock")
public class CoreMockController {

    private final CoreMockService coreMockService;

    public CoreMockController(CoreMockService coreMockService) {
        this.coreMockService = coreMockService;
    }

    @PostMapping("/evento")
    public ResponseEntity<Void> recibirEvento(@Valid @RequestBody CoreEventoRequest request) {
        coreMockService.registrarEvento(request);
        return ResponseEntity.accepted().build();
    }
}
