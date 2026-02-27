# Decisiones de Arquitectura

## Decision 1
Uso de arquitectura por capas (`controller`, `service`, `repository`) para separar responsabilidades.

## Decision 2
Persistencia H2 en memoria para entorno autocontenido y prueba rapida.

## Decision 3
Seguridad minima por `x-api-key` con filtro global.

## Decision 4
Correlation ID por request para trazabilidad en logs.

## Decision 5
IPC configurable por `app.ipc` para renovacion de polizas.
