package com.company.polizas.dto;

import com.company.polizas.domain.EstadoRiesgo;

public record RiesgoResponse(
        Long id,
        Long polizaId,
        EstadoRiesgo estado,
        String descripcion
) {
}
