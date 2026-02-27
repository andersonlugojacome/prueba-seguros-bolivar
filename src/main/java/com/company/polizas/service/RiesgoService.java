package com.company.polizas.service;

import com.company.polizas.domain.EstadoRiesgo;
import com.company.polizas.domain.Riesgo;
import com.company.polizas.dto.RiesgoResponse;
import com.company.polizas.exception.NotFoundException;
import com.company.polizas.repository.RiesgoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiesgoService {

    private final RiesgoRepository riesgoRepository;
    private final CoreMockService coreMockService;

    public RiesgoService(RiesgoRepository riesgoRepository, CoreMockService coreMockService) {
        this.riesgoRepository = riesgoRepository;
        this.coreMockService = coreMockService;
    }

    @Transactional
    public RiesgoResponse cancelarRiesgo(Long riesgoId) {
        Riesgo riesgo = riesgoRepository.findById(riesgoId)
                .orElseThrow(() -> new NotFoundException("Riesgo no encontrado con id=" + riesgoId));

        riesgo.setEstado(EstadoRiesgo.CANCELADO);
        Riesgo saved = riesgoRepository.save(riesgo);
        coreMockService.intentarNotificarCore("ACTUALIZACION", saved.getPoliza().getId());

        return new RiesgoResponse(
                saved.getId(),
                saved.getPoliza().getId(),
                saved.getEstado(),
                saved.getDescripcion()
        );
    }
}
