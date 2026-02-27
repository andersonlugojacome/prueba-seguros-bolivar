package com.company.polizas.service;

import com.company.polizas.domain.EstadoPoliza;
import com.company.polizas.domain.EstadoRiesgo;
import com.company.polizas.domain.Poliza;
import com.company.polizas.domain.Riesgo;
import com.company.polizas.domain.TipoPoliza;
import com.company.polizas.dto.AgregarRiesgoRequest;
import com.company.polizas.dto.PolizaResponse;
import com.company.polizas.dto.RiesgoResponse;
import com.company.polizas.exception.BusinessException;
import com.company.polizas.exception.NotFoundException;
import com.company.polizas.repository.PolizaRepository;
import com.company.polizas.repository.RiesgoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolizaService {

    private final PolizaRepository polizaRepository;
    private final RiesgoRepository riesgoRepository;
    private final CoreMockService coreMockService;

    @Value("${app.ipc:0.10}")
    private BigDecimal ipc;

    public PolizaService(PolizaRepository polizaRepository, RiesgoRepository riesgoRepository, CoreMockService coreMockService) {
        this.polizaRepository = polizaRepository;
        this.riesgoRepository = riesgoRepository;
        this.coreMockService = coreMockService;
    }

    @Transactional(readOnly = true)
    public List<PolizaResponse> listar(TipoPoliza tipo, EstadoPoliza estado) {
        List<Poliza> polizas;
        if (tipo != null && estado != null) {
            polizas = polizaRepository.findByTipoAndEstado(tipo, estado);
        } else if (tipo != null) {
            polizas = polizaRepository.findByTipo(tipo);
        } else if (estado != null) {
            polizas = polizaRepository.findByEstado(estado);
        } else {
            polizas = polizaRepository.findAll();
        }
        return polizas.stream().map(this::toPolizaResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RiesgoResponse> listarRiesgosPorPoliza(Long polizaId) {
        Poliza poliza = obtenerPoliza(polizaId);
        return poliza.getRiesgos().stream().map(this::toRiesgoResponse).toList();
    }

    @Transactional
    public PolizaResponse renovarPoliza(Long polizaId) {
        Poliza poliza = obtenerPoliza(polizaId);
        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new BusinessException("No se puede renovar una poliza cancelada");
        }

        BigDecimal canonNuevo = poliza.getCanonMensual()
                .multiply(BigDecimal.ONE.add(ipc))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal primaNueva = canonNuevo
                .multiply(BigDecimal.valueOf(poliza.getVigenciaMeses()))
                .setScale(2, RoundingMode.HALF_UP);

        LocalDate nuevoInicio = poliza.getFechaFin().plusDays(1);
        LocalDate nuevoFin = nuevoInicio.plusMonths(poliza.getVigenciaMeses());

        poliza.setCanonMensual(canonNuevo);
        poliza.setPrima(primaNueva);
        poliza.setFechaInicio(nuevoInicio);
        poliza.setFechaFin(nuevoFin);
        poliza.setEstado(EstadoPoliza.RENOVADA);

        coreMockService.intentarNotificarCore("ACTUALIZACION", poliza.getId());
        return toPolizaResponse(polizaRepository.save(poliza));
    }

    @Transactional
    public PolizaResponse cancelarPoliza(Long polizaId) {
        Poliza poliza = obtenerPoliza(polizaId);
        poliza.setEstado(EstadoPoliza.CANCELADA);
        poliza.getRiesgos().forEach(r -> r.setEstado(EstadoRiesgo.CANCELADO));

        coreMockService.intentarNotificarCore("ACTUALIZACION", poliza.getId());
        return toPolizaResponse(polizaRepository.save(poliza));
    }

    @Transactional
    public RiesgoResponse agregarRiesgo(Long polizaId, AgregarRiesgoRequest request) {
        Poliza poliza = obtenerPoliza(polizaId);
        if (poliza.getTipo() != TipoPoliza.COLECTIVA) {
            throw new BusinessException("Solo se pueden agregar riesgos a polizas colectivas");
        }

        Riesgo riesgo = new Riesgo();
        riesgo.setPoliza(poliza);
        riesgo.setEstado(EstadoRiesgo.ACTIVO);
        riesgo.setDescripcion(request.descripcion());

        Riesgo saved = riesgoRepository.save(riesgo);
        coreMockService.intentarNotificarCore("ACTUALIZACION", poliza.getId());
        return toRiesgoResponse(saved);
    }

    public Poliza obtenerPoliza(Long polizaId) {
        return polizaRepository.findById(polizaId)
                .orElseThrow(() -> new NotFoundException("Poliza no encontrada con id=" + polizaId));
    }

    private PolizaResponse toPolizaResponse(Poliza poliza) {
        return new PolizaResponse(
                poliza.getId(),
                poliza.getTipo(),
                poliza.getEstado(),
                poliza.getVigenciaMeses(),
                poliza.getFechaInicio(),
                poliza.getFechaFin(),
                poliza.getCanonMensual(),
                poliza.getPrima(),
                poliza.getRiesgos() != null ? poliza.getRiesgos().size() : 0
        );
    }

    private RiesgoResponse toRiesgoResponse(Riesgo riesgo) {
        return new RiesgoResponse(
                riesgo.getId(),
                riesgo.getPoliza().getId(),
                riesgo.getEstado(),
                riesgo.getDescripcion()
        );
    }
}
