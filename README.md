
---

## Implementacion Entregada - API de Gestion de Polizas

### Stack tecnico
- Java 17
- Spring Boot 3.x
- Maven
- Spring Data JPA + H2 en memoria
- Spring Validation
- Spring Boot Test + MockMvc

### Requisitos
- JDK 17+
- Maven 3.9+

### Como correr
```bash
mvn spring-boot:run
```

### Seguridad
Todas las rutas del API requieren header:
```text
x-api-key: 123456
```
Si falta o es incorrecto, retorna `401 Unauthorized`.

### Configuracion relevante
Archivo: `src/main/resources/application.yml`
- `app.ipc: 0.10` (10% por defecto para renovacion)
- `app.api-key: 123456`
- H2 en memoria (`create-drop`)

### Endpoints implementados
- `GET /polizas?tipo=&estado=`
- `GET /polizas/{id}/riesgos`
- `POST /polizas/{id}/renovar`
- `POST /polizas/{id}/cancelar`
- `POST /polizas/{id}/riesgos`
- `POST /riesgos/{id}/cancelar`
- `POST /core-mock/evento` (mock obligatorio)

### Reglas de negocio cubiertas
- Poliza `INDIVIDUAL` solo soporta 1 riesgo.
- No se puede renovar una poliza `CANCELADA`.
- Cancelar poliza cancela todos sus riesgos.
- Agregar riesgo solo para polizas `COLECTIVA`.
- Operaciones mutantes intentan notificar al CORE mock y dejan log.

### Datos iniciales (CommandLineRunner)
- 1 poliza individual vigente con 1 riesgo activo.
- 1 poliza colectiva vigente con 2 riesgos activos.
- 1 poliza cancelada (para validar no renovable).

### Ejemplos curl
Listar polizas filtradas:
```bash
curl -X GET 'http://localhost:8080/polizas?tipo=COLECTIVA&estado=VIGENTE' \
  -H 'x-api-key: 123456'
```

Listar riesgos de una poliza:
```bash
curl -X GET 'http://localhost:8080/polizas/2/riesgos' \
  -H 'x-api-key: 123456'
```

Renovar poliza:
```bash
curl -X POST 'http://localhost:8080/polizas/2/renovar' \
  -H 'x-api-key: 123456'
```

Cancelar poliza:
```bash
curl -X POST 'http://localhost:8080/polizas/2/cancelar' \
  -H 'x-api-key: 123456'
```

Agregar riesgo a poliza colectiva:
```bash
curl -X POST 'http://localhost:8080/polizas/2/riesgos' \
  -H 'x-api-key: 123456' \
  -H 'Content-Type: application/json' \
  -d '{
    "descripcion": "Riesgo de impago"
  }'
```

Cancelar riesgo:
```bash
curl -X POST 'http://localhost:8080/riesgos/3/cancelar' \
  -H 'x-api-key: 123456'
```

Mock de evento CORE:
```bash
curl -X POST 'http://localhost:8080/core-mock/evento' \
  -H 'x-api-key: 123456' \
  -H 'Content-Type: application/json' \
  -d '{
    "evento": "ACTUALIZACION",
    "polizaId": 555
  }'
```

### Pruebas automatizadas incluidas
Archivo: `src/test/java/com/company/polizas/PolizasApiIntegrationTest.java`
Casos minimos:
1. Sin `x-api-key` retorna 401.
2. Renovar poliza cancelada retorna 400.
3. Agregar riesgo a poliza individual retorna 400.
4. Cancelar poliza deja poliza y riesgos en estado cancelado.
5. Filtro de `GET /polizas` funciona por tipo y estado.

### Ejecucion de pruebas
```bash
mvn test
```

### Notas de decisiones
- Se uso arquitectura por capas: `controller`, `service`, `repository`.
- Correlation ID por request: si no llega `x-correlation-id`, se genera UUID y se devuelve en response header.
- El envio al CORE se modela como intento interno registrado por `CoreMockService`, y expuesto en endpoint `/core-mock/evento`.

### Cliente React de prueba
- Se incluyo un cliente simple en `docs/react-test-client/App.tsx`.
- Instrucciones de uso: `docs/react-test-client/README.md`.

### UI React integrada en /ui
Proyecto React + Vite + TypeScript listo dentro de `ui/`.

Backend:
```bash
mvn spring-boot:run
```

Frontend (nueva terminal):
```bash
cd ui
npm install
npm run dev
```

Abrir en navegador:
- `http://localhost:5173`
