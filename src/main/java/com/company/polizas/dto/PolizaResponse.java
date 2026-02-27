package com.company.polizas.dto;

import com.company.polizas.domain.EstadoPoliza;
import com.company.polizas.domain.TipoPoliza;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PolizaResponse(
        Long id,
        TipoPoliza tipo,
        EstadoPoliza estado,
        int vigenciaMeses,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal canonMensual,
        BigDecimal prima,
        int totalRiesgos
) {
}
