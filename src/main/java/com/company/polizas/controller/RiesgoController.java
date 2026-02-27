package com.company.polizas.controller;

import com.company.polizas.dto.RiesgoResponse;
import com.company.polizas.service.RiesgoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/riesgos")
public class RiesgoController {

    private final RiesgoService riesgoService;

    public RiesgoController(RiesgoService riesgoService) {
        this.riesgoService = riesgoService;
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<RiesgoResponse> cancelarRiesgo(@PathVariable Long id) {
        return ResponseEntity.ok(riesgoService.cancelarRiesgo(id));
    }
}
