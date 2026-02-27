# CORE Mock

## Endpoint
- `POST /core-mock/evento`
- Body:
```json
{
  "evento": "ACTUALIZACION",
  "polizaId": 555
}
```

## Comportamiento
- Registra en logs el intento de envio al CORE.
- Es invocado desde operaciones mutantes de poliza/riesgo.
