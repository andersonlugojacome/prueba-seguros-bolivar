# Modulo 1 - System Design

## Arquitectura de alto nivel
Plataforma compuesta por un API Gateway que enruta al Servicio de Polizas/Riesgos y al Servicio de Notificaciones. El Servicio de Polizas integra con un Adapter CORE para mantener el sistema legado sincronizado en operaciones mutantes.

## Patrones de arquitectura (3)
1. Hexagonal (Ports and Adapters): desacopla dominio de integraciones externas (CORE, notificaciones).
2. Event-Driven: creacion/renovacion/cancelacion publican eventos para notificaciones y auditoria asincrona.
3. API Gateway: punto unico para seguridad, rate limiting, versionamiento y observabilidad centralizada.

## Modelo de datos principal
- Poliza: id, tipo, estado, vigenciaMeses, fechaInicio, fechaFin, canonMensual, prima.
- Riesgo: id, estado, descripcion, polizaId.
- Relacion: Poliza 1..N Riesgo. Restriccion de negocio: INDIVIDUAL maximo 1 riesgo.

## Escalabilidad
- Escalado horizontal del servicio stateless.
- Cache de consultas frecuentes de polizas por filtros.
- Particionamiento/logical sharding por organizacion o zona geografica si crece volumen.

## Logs y observabilidad
- Correlation ID por request propagado en headers y logs.
- Logs estructurados con evento funcional y resultado de integracion CORE.
- Metricas: latencia p95/p99 por endpoint, errores 4xx/5xx, tasa de renovaciones/cancelaciones.

## Tolerancia a fallos
- Reintentos con backoff para integracion CORE.
- Circuit breaker para aislar fallas del legado.
- Cola de eventos para garantizar entrega eventual de notificaciones.

## Versionamiento de API
- Versionado en URI (`/v1/polizas`) o header.
- Contratos backward-compatible en cambios menores.
- Estrategia de deprecacion con ventana de migracion y monitoreo de consumo.

## Diagrama ASCII
```text
[Cliente Web/Mobile]
        |
        v
   [API Gateway]
        |
        +------------------------------+
        |                              |
        v                              v
[Servicio Polizas/Riesgos]      [Servicio Notificaciones]
        |
        +-------> [Adapter CORE] -------> [CORE Legado]
        |
        v
     [Base de Datos]
```
