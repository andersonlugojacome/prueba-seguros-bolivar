# API Endpoints

## Endpoints implementados
- `GET /polizas?tipo=&estado=`
- `GET /polizas/{id}/riesgos`
- `POST /polizas/{id}/renovar`
- `POST /polizas/{id}/cancelar`
- `POST /polizas/{id}/riesgos`
- `POST /riesgos/{id}/cancelar`
- `POST /core-mock/evento`

## Notas
- Todas requieren `x-api-key: 123456`.
- Errores manejados de forma consistente (400/401/404).
