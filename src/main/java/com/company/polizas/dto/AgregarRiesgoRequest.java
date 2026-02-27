package com.company.polizas.dto;

import jakarta.validation.constraints.NotBlank;

public record AgregarRiesgoRequest(
        @NotBlank(message = "La descripcion es obligatoria")
        String descripcion
) {
}
