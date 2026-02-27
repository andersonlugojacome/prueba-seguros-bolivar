package com.company.polizas;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.polizas.domain.EstadoPoliza;
import com.company.polizas.domain.Poliza;
import com.company.polizas.domain.TipoPoliza;
import com.company.polizas.repository.PolizaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PolizasApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PolizaRepository polizaRepository;

    @Test
    void requestSinApiKeyRetorna401() throws Exception {
        mockMvc.perform(get("/polizas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void renovarPolizaCanceladaRetorna400() throws Exception {
        Long polizaCanceladaId = polizaRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPoliza.CANCELADA)
                .map(Poliza::getId)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/polizas/{id}/renovar", polizaCanceladaId)
                        .header("x-api-key", "123456"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se puede renovar una poliza cancelada"));
    }

    @Test
    void agregarRiesgoAPolizaIndividualRetorna400() throws Exception {
        Long polizaIndividualId = polizaRepository.findAll().stream()
                .filter(p -> p.getTipo() == TipoPoliza.INDIVIDUAL && p.getEstado() != EstadoPoliza.CANCELADA)
                .map(Poliza::getId)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/polizas/{id}/riesgos", polizaIndividualId)
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descripcion": "Riesgo adicional invalido"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Solo se pueden agregar riesgos a polizas colectivas"));
    }

    @Test
    void cancelarPolizaCancelaTodosLosRiesgos() throws Exception {
        Long polizaVigenteId = polizaRepository.findAll().stream()
                .filter(p -> p.getTipo() == TipoPoliza.COLECTIVA && p.getEstado() == EstadoPoliza.VIGENTE)
                .map(Poliza::getId)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/polizas/{id}/cancelar", polizaVigenteId)
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        mockMvc.perform(get("/polizas/{id}/riesgos", polizaVigenteId)
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].estado").value("CANCELADO"))
                .andExpect(jsonPath("$[1].estado").value("CANCELADO"));
    }

    @Test
    void getPolizasFiltraCorrectamente() throws Exception {
        mockMvc.perform(get("/polizas")
                        .queryParam("tipo", "COLECTIVA")
                        .queryParam("estado", "VIGENTE")
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo").value("COLECTIVA"))
                .andExpect(jsonPath("$[0].estado").value("VIGENTE"));
    }
}
