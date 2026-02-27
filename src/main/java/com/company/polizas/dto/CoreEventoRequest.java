package com.company.polizas.dto;

import jakarta.validation.constraints.NotNull;

public record CoreEventoRequest(
        @NotNull(message = "evento es obligatorio")
        String evento,
        @NotNull(message = "polizaId es obligatorio")
        Long polizaId
) {
}
