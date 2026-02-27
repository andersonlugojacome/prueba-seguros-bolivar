# Seguridad

## Reglas
- Header obligatorio: `x-api-key: 123456`.
- Si falta o es invalido: HTTP 401.

## Implementacion
- Filtro global que valida API key para todas las rutas.
