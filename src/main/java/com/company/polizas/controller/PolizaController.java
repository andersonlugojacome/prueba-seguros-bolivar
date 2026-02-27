package com.company.polizas.controller;

import com.company.polizas.domain.EstadoPoliza;
import com.company.polizas.domain.TipoPoliza;
import com.company.polizas.dto.AgregarRiesgoRequest;
import com.company.polizas.dto.PolizaResponse;
import com.company.polizas.dto.RiesgoResponse;
import com.company.polizas.service.PolizaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/polizas")
public class PolizaController {

    private final PolizaService polizaService;

    public PolizaController(PolizaService polizaService) {
        this.polizaService = polizaService;
    }

    @GetMapping
    public ResponseEntity<List<PolizaResponse>> listarPolizas(
            @RequestParam(required = false) TipoPoliza tipo,
            @RequestParam(required = false) EstadoPoliza estado) {
        return ResponseEntity.ok(polizaService.listar(tipo, estado));
    }

    @GetMapping("/{id}/riesgos")
    public ResponseEntity<List<RiesgoResponse>> listarRiesgos(@PathVariable Long id) {
        return ResponseEntity.ok(polizaService.listarRiesgosPorPoliza(id));
    }

    @PostMapping("/{id}/renovar")
    public ResponseEntity<PolizaResponse> renovar(@PathVariable Long id) {
        return ResponseEntity.ok(polizaService.renovarPoliza(id));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PolizaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(polizaService.cancelarPoliza(id));
    }

    @PostMapping("/{id}/riesgos")
    public ResponseEntity<RiesgoResponse> agregarRiesgo(@PathVariable Long id, @Valid @RequestBody AgregarRiesgoRequest request) {
        return ResponseEntity.ok(polizaService.agregarRiesgo(id, request));
    }
}
