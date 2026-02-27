package com.company.polizas.service;

import com.company.polizas.dto.CoreEventoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CoreMockService {

    private static final Logger log = LoggerFactory.getLogger(CoreMockService.class);

    public void registrarEvento(CoreEventoRequest request) {
        log.info("[CORE-MOCK] Evento recibido: evento={}, polizaId={}", request.evento(), request.polizaId());
    }

    public void intentarNotificarCore(String evento, Long polizaId) {
        log.info("Se intento enviar al CORE: evento={}, polizaId={}", evento, polizaId);
        registrarEvento(new CoreEventoRequest(evento, polizaId));
    }
}
