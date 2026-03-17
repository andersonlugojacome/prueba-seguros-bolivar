# Codex Init - Guia de Estudio Profunda del Proyecto

## 1. Objetivo de este documento

Este archivo sirve como una guia de lectura tecnica para entender el proyecto de gestion de polizas con suficiente profundidad como para:

- explicar su arquitectura sin leer todo el codigo de memoria,
- ubicar rapidamente responsabilidades por capa,
- entender las reglas de negocio implementadas,
- detectar inconsistencias entre documentacion, datos semilla y pruebas,
- preparar una defensa tecnica del ejercicio,
- identificar mejoras de diseño, testing y mantenibilidad.

No reemplaza el codigo fuente. Lo complementa y lo ordena.

## 2. Resumen ejecutivo

El proyecto implementa una API REST en Spring Boot para administrar polizas y riesgos, con una UI React minima para pruebas manuales.

La solucion esta construida con una arquitectura por capas:

- `controller`: expone endpoints HTTP.
- `service`: contiene reglas de negocio y orquesta persistencia e integracion mock.
- `repository`: acceso a datos con Spring Data JPA.
- `domain`: entidades y enums del modelo.
- `dto`: contratos de entrada y salida.
- `config`: filtros HTTP, CORS y datos semilla.
- `exception`: manejo uniforme de errores.

Persistencia:

- H2 en memoria.
- JPA con `ddl-auto: create-drop`.

Seguridad:

- autenticacion simple por header `x-api-key`.

Observabilidad:

- correlation id por request con `x-correlation-id`.

Integracion externa:

- simulada con `CoreMockService`.

## 3. Stack tecnologico real

Backend:

- Java 17
- Spring Boot 3.3.8
- Spring Web
- Spring Validation
- Spring Data JPA
- H2
- Spring Boot Test + MockMvc

Frontend:

- React 19
- React DOM 19
- TypeScript 5.9
- Vite 7

Build:

- Maven para backend
- npm para frontend

## 4. Estructura real del repositorio

```text
.
├── README.md
├── pom.xml
├── docs
│   ├── asks
│   ├── obsidian
│   ├── react-test-client
│   ├── uml.md
│   └── codex.md
├── src
│   ├── main
│   │   ├── java/com/company/polizas
│   │   │   ├── config
│   │   │   ├── controller
│   │   │   ├── domain
│   │   │   ├── dto
│   │   │   ├── exception
│   │   │   ├── repository
│   │   │   ├── service
│   │   │   └── PolizasApplication.java
│   │   └── resources/application.yml
│   └── test/java/com/company/polizas/PolizasApiIntegrationTest.java
└── ui
    ├── package.json
    └── src
```

## 5. Proposito funcional del sistema

La API modela dos conceptos principales:

- `Poliza`
- `Riesgo`

Operaciones soportadas:

- listar polizas con filtros,
- listar riesgos por poliza,
- renovar una poliza,
- cancelar una poliza,
- agregar un riesgo a una poliza,
- cancelar un riesgo,
- registrar o simular un evento al CORE.

La logica se enfoca en mantener consistencia operativa basica mas que en representar un dominio asegurador completo.

## 6. Modelo de dominio

### 6.1 Entidad `Poliza`

Campos:

- `id`
- `tipo`
- `estado`
- `vigenciaMeses`
- `fechaInicio`
- `fechaFin`
- `canonMensual`
- `prima`
- `riesgos`

Observaciones:

- La relacion con `Riesgo` es `@OneToMany(mappedBy = "poliza", cascade = CascadeType.ALL, orphanRemoval = true)`.
- `cascade = ALL` implica que al persistir una poliza se persisten sus riesgos asociados.
- `orphanRemoval = true` prepara el modelo para eliminar riesgos huérfanos si se desacoplan de la coleccion.

### 6.2 Entidad `Riesgo`

Campos:

- `id`
- `poliza`
- `estado`
- `descripcion`

Observaciones:

- La relacion con `Poliza` es `@ManyToOne(fetch = FetchType.LAZY)`.
- Cada riesgo pertenece obligatoriamente a una poliza por `nullable = false`.

### 6.3 Enums del dominio

`TipoPoliza`:

- `INDIVIDUAL`
- `COLECTIVA`

`EstadoPoliza`:

- `VIGENTE`
- `RENOVADA`
- `CANCELADA`

`EstadoRiesgo`:

- `ACTIVO`
- `CANCELADO`

## 7. Reglas de negocio implementadas

Las reglas visibles en servicios y pruebas son:

- Una poliza `INDIVIDUAL` no admite agregar riesgos adicionales.
- Solo las polizas `COLECTIVA` permiten `POST /polizas/{id}/riesgos`.
- Una poliza `CANCELADA` no se puede renovar.
- Cancelar una poliza cambia su estado a `CANCELADA`.
- Cancelar una poliza cancela todos sus riesgos.
- Cancelar un riesgo cambia su estado a `CANCELADO`.
- Renovar una poliza recalcula canon y prima usando IPC.
- Toda operacion mutante intenta notificar al CORE mock.

Reglas no implementadas o no reforzadas a nivel estructural:

- No hay validacion explicita para impedir cancelar dos veces una poliza.
- No hay validacion para impedir cancelar dos veces un riesgo.
- No hay regla que impida agregar riesgos a una poliza colectiva cancelada.
- No hay regla que valide que una poliza renovada no pueda volver a renovarse.
- La restriccion "individual maximo 1 riesgo" esta modelada por comportamiento, no por constraint de base de datos.

## 8. Flujo general de una request

Orden mental correcto para leer el sistema:

1. Entra una request HTTP.
2. `CorrelationIdFilter` genera o reutiliza `x-correlation-id`.
3. `ApiKeyFilter` valida `x-api-key`.
4. El request llega al controller correspondiente.
5. El controller delega al service.
6. El service consulta o actualiza entidades via repository.
7. Si aplica, se notifica al `CoreMockService`.
8. La respuesta se transforma a DTO.
9. Si hay error, `GlobalExceptionHandler` genera el payload uniforme.

Ese recorrido es simple, legible y apropiado para un ejercicio tecnico.

## 9. Configuracion clave

Archivo: `src/main/resources/application.yml`

Valores importantes:

- `spring.application.name: polizas-api`
- H2 en memoria con `jdbc:h2:mem:polizasdb`
- `spring.jpa.hibernate.ddl-auto: create-drop`
- `spring.h2.console.enabled: true`
- `app.ipc: 0.10`
- `app.api-key: 123456`

Interpretacion:

- La base se reconstruye en cada arranque.
- El estado del sistema no sobrevive reinicios.
- El valor de IPC controla la renovacion.
- La API key esta hardcodeada por configuracion simple.

## 10. Capa de configuracion

### 10.1 `CorrelationIdFilter`

Responsabilidad:

- leer `x-correlation-id` si viene en el request,
- generar un UUID si no viene,
- guardarlo en MDC,
- devolverlo en response header,
- loggear la entrada del request.

Valor arquitectonico:

- mejora trazabilidad,
- prepara el sistema para observabilidad distribuida,
- evita perder contexto de una llamada entre logs.

### 10.2 `ApiKeyFilter`

Responsabilidad:

- validar el header `x-api-key`,
- rechazar requests no autorizados con `401`,
- excluir `OPTIONS` para soportar preflight CORS.

Fortaleza:

- resolucion muy simple y funcional para una prueba tecnica.

Limite:

- no usa Spring Security,
- no hay roles,
- no hay expiracion ni rotacion real de credenciales.

### 10.3 `WebCorsConfig`

Responsabilidad:

- habilitar requests desde `localhost` y `127.0.0.1`,
- permitir metodos comunes,
- exponer `x-correlation-id`.

Lectura tecnica:

- esta pensada para desarrollo local, no para un despliegue productivo.

### 10.4 `DataInitializer`

Responsabilidad:

- sembrar datos iniciales cuando la base esta vacia.

Datos creados:

- 1 poliza individual vigente con 1 riesgo activo.
- 1 poliza colectiva vigente con 3 riesgos activos.
- 1 poliza individual cancelada con 1 riesgo cancelado.

Interpretacion:

- sirve para probar listados, reglas y operaciones mutantes sin cargar datos manualmente.

## 11. Capa de controladores

### 11.1 `PolizaController`

Expone:

- `GET /polizas`
- `GET /polizas/{id}/riesgos`
- `POST /polizas/{id}/renovar`
- `POST /polizas/{id}/cancelar`
- `POST /polizas/{id}/riesgos`

Caracteristicas:

- controladores delgados,
- sin logica de negocio,
- delegacion limpia a `PolizaService`.

### 11.2 `RiesgoController`

Expone:

- `POST /riesgos/{id}/cancelar`

### 11.3 `CoreMockController`

Expone:

- `POST /core-mock/evento`

Uso:

- simular la recepcion de un evento externo o validar el contrato minimo del mock.

## 12. Capa de servicios

### 12.1 `PolizaService`

Es la pieza central del backend.

Responsabilidades:

- listar polizas con filtros opcionales,
- listar riesgos por poliza,
- renovar polizas,
- cancelar polizas,
- agregar riesgos,
- traducir entidades a DTOs,
- coordinar notificacion al CORE mock.

#### Metodo `listar`

Comportamiento:

- si llegan `tipo` y `estado`, filtra por ambos;
- si llega solo uno, filtra por ese;
- si no llega ninguno, retorna todo.

Decision importante:

- usa repositorio con `@EntityGraph(attributePaths = "riesgos")` para evitar problemas de carga al mapear `totalRiesgos`.

#### Metodo `listarRiesgosPorPoliza`

Comportamiento:

- obtiene la poliza,
- usa la coleccion de riesgos ya asociada,
- la transforma a `RiesgoResponse`.

#### Metodo `renovarPoliza`

Pasos:

1. Busca la poliza.
2. Si esta cancelada, lanza `BusinessException`.
3. Calcula nuevo canon como `canonMensual * (1 + ipc)`.
4. Calcula nueva prima como `canonNuevo * vigenciaMeses`.
5. Define nueva fecha de inicio como `fechaFin + 1 dia`.
6. Define nueva fecha fin como `nuevoInicio + vigenciaMeses`.
7. Cambia estado a `RENOVADA`.
8. Notifica al CORE mock.
9. Guarda y responde.

Observacion importante:

- `nuevoFin = nuevoInicio.plusMonths(vigenciaMeses)` puede producir una vigencia de facto mayor a la esperada si el negocio interpreta fin inclusivo. Es un detalle de modelado temporal que conviene discutir en una defensa tecnica.

#### Metodo `cancelarPoliza`

Pasos:

1. Busca la poliza.
2. Marca la poliza como `CANCELADA`.
3. Recorre todos los riesgos y los marca como `CANCELADO`.
4. Notifica al CORE.
5. Guarda la poliza.

Punto fuerte:

- la cancelacion propaga estado a la agregacion hija, alineado con la regla de negocio.

#### Metodo `agregarRiesgo`

Pasos:

1. Busca la poliza.
2. Verifica que sea `COLECTIVA`.
3. Crea riesgo activo.
4. Persiste el riesgo directamente con `RiesgoRepository`.
5. Notifica al CORE.
6. Devuelve DTO.

Punto de analisis:

- el metodo no valida el estado de la poliza; por ejemplo, podria permitir agregar riesgo a una poliza colectiva cancelada si existiera ese caso.

### 12.2 `RiesgoService`

Responsabilidad:

- cancelar un riesgo por id.

Pasos:

1. busca el riesgo,
2. cambia estado a `CANCELADO`,
3. guarda,
4. notifica al CORE con el `polizaId`,
5. devuelve DTO.

Observacion:

- no valida si el riesgo ya estaba cancelado.

### 12.3 `CoreMockService`

Responsabilidad:

- registrar el evento recibido,
- simular el intento de envio al CORE.

Lectura arquitectonica:

- no hay integracion HTTP real,
- no hay colas,
- no hay reintentos,
- pero la abstraccion permite evolucionar ese punto sin contaminar controllers.

## 13. Capa de persistencia

### 13.1 `PolizaRepository`

Extiende `JpaRepository<Poliza, Long>`.

Metodos:

- `findByTipoAndEstado`
- `findByTipo`
- `findByEstado`
- `findAll`

Punto importante:

- todos los metodos visibles usan `@EntityGraph(attributePaths = "riesgos")`.

Por que importa:

- evita problemas de `LazyInitializationException`,
- simplifica el mapeo a DTOs que dependen del tamanio de la coleccion,
- hace mas predecible el rendimiento en lecturas de este ejercicio.

### 13.2 `RiesgoRepository`

Extiende `JpaRepository<Riesgo, Long>`.

Metodo adicional:

- `findByPolizaId`

Observacion:

- en el estado actual casi no se usa, porque la lectura de riesgos se hace navegando desde `Poliza`.

## 14. DTOs y contratos

### 14.1 Respuesta de poliza

`PolizaResponse` expone:

- `id`
- `tipo`
- `estado`
- `vigenciaMeses`
- `fechaInicio`
- `fechaFin`
- `canonMensual`
- `prima`
- `totalRiesgos`

### 14.2 Respuesta de riesgo

`RiesgoResponse` expone:

- `id`
- `polizaId`
- `estado`
- `descripcion`

### 14.3 Request para agregar riesgo

`AgregarRiesgoRequest`:

- exige `descripcion` no vacia con `@NotBlank`.

### 14.4 Request del CORE mock

`CoreEventoRequest`:

- requiere `evento`,
- requiere `polizaId`.

Observacion:

- el campo `evento` usa `@NotNull` pero no `@NotBlank`, por lo que una cadena vacia seria aceptada.

## 15. Manejo de errores

El proyecto implementa un manejo de errores consistente via `GlobalExceptionHandler`.

Casos:

- `NotFoundException` -> `404`
- `BusinessException` -> `400`
- errores de validacion -> `400`
- `IllegalArgumentException` -> `400`
- excepciones no controladas -> `500`

Formato comun:

- `timestamp`
- `status`
- `error`
- `message`
- `path`

Punto bueno:

- la API mantiene un contrato de error relativamente estable.

Punto mejorable:

- no se retorna lista de errores de validacion cuando fallan varios campos.

## 16. Endpoints disponibles

### 16.1 Listar polizas

`GET /polizas?tipo=&estado=`

Headers:

- `x-api-key: 123456`

Uso:

- filtra por `TipoPoliza` y `EstadoPoliza`.

### 16.2 Listar riesgos por poliza

`GET /polizas/{id}/riesgos`

### 16.3 Renovar poliza

`POST /polizas/{id}/renovar`

Efecto:

- recalcula valores,
- mueve fechas,
- cambia estado a `RENOVADA`.

### 16.4 Cancelar poliza

`POST /polizas/{id}/cancelar`

Efecto:

- cancela la poliza,
- cancela todos sus riesgos.

### 16.5 Agregar riesgo

`POST /polizas/{id}/riesgos`

Body:

```json
{
  "descripcion": "Riesgo demo"
}
```

### 16.6 Cancelar riesgo

`POST /riesgos/{id}/cancelar`

### 16.7 Registrar evento CORE mock

`POST /core-mock/evento`

Body:

```json
{
  "evento": "ACTUALIZACION",
  "polizaId": 2
}
```

## 17. Datos semilla reales

El estado inicial esperado al levantar la aplicacion con base limpia es:

### Poliza 1

- tipo: `INDIVIDUAL`
- estado: `VIGENTE`
- vigencia: 12 meses
- riesgos: 1 activo

### Poliza 2

- tipo: `COLECTIVA`
- estado: `VIGENTE`
- vigencia: 6 meses
- riesgos: 3 activos

### Poliza 3

- tipo: `INDIVIDUAL`
- estado: `CANCELADA`
- vigencia: 12 meses
- riesgos: 1 cancelado

Este inventario es especialmente util para entender ids y probar rapidamente endpoints.

## 18. Suite de pruebas automatizadas

Archivo principal:

- `src/test/java/com/company/polizas/PolizasApiIntegrationTest.java`

Cobertura actual:

- request sin API key retorna `401`,
- renovar poliza cancelada retorna `400`,
- agregar riesgo a poliza individual retorna `400`,
- cancelar poliza cambia estado,
- `GET /polizas` filtra correctamente.

Lectura importante:

- las pruebas son de integracion con `MockMvc`,
- no hay tests unitarios por servicio,
- no hay tests de repositorios,
- no hay tests de validacion de payloads invalidos para todos los endpoints,
- no hay tests de correlation id,
- no hay tests de CORS.

## 19. Inconsistencias y hallazgos actuales

Esta es la seccion mas importante si quieres estudiar el proyecto con criterio, no solo memorizarlo.

### 19.1 Inconsistencia entre datos semilla y prueba

`DataInitializer` crea 3 riesgos para la poliza colectiva vigente.

Sin embargo, la prueba `cancelarPolizaCancelaTodosLosRiesgos` sigue esperando `hasSize(2)`.

Conclusion:

- si se ejecuta la suite tal como esta, esa prueba tiene alta probabilidad de fallar.
- esto indica desalineacion entre fixtures y assertions.

### 19.2 Inconsistencia con README historico

El `README.md` habla de una poliza colectiva con 2 riesgos iniciales.

El codigo real ahora crea 3.

Conclusion:

- la documentacion principal no refleja completamente el estado actual del codigo.

### 19.3 Posible ambiguedad temporal en renovacion

La renovacion usa:

- `nuevoInicio = fechaFin + 1 dia`
- `nuevoFin = nuevoInicio + vigenciaMeses`

Dependiendo de si el negocio considera fecha fin inclusiva o exclusiva, esto puede introducir un dia adicional o una interpretacion discutible del periodo.

### 19.4 Validaciones de negocio incompletas

Faltan restricciones como:

- no agregar riesgos a polizas canceladas,
- no renovar polizas ya renovadas,
- no cancelar elementos ya cancelados,
- no validar duplicidad o cardinalidad avanzada.

### 19.5 Seguridad minima

La seguridad por API key es suficiente para un challenge, pero no para produccion.

No existe:

- autenticacion de usuarios,
- autorizacion por roles,
- auditoria de actor,
- secretos gestionados externamente.

## 20. Analisis arquitectonico

### 20.1 Lo que esta bien resuelto

- separacion por capas clara,
- dominio simple y legible,
- endpoints coherentes,
- uso apropiado de DTOs,
- manejo uniforme de errores,
- filtros HTTP bien ubicados,
- datos semilla utiles,
- CORS local y UI de prueba para demostracion rapida.

### 20.2 Lo que esta simplificado intencionalmente

- sin autenticacion real,
- sin base persistente,
- sin migraciones,
- sin versionado de API,
- sin clientes externos reales,
- sin mensajeria,
- sin logs estructurados avanzados,
- sin cobertura de pruebas amplia.

### 20.3 Lo que un evaluador tecnico podria preguntar

- por que no se uso Spring Security,
- por que el dominio no impone mas invariantes,
- por que `RiesgoRepository.findByPolizaId` casi no se usa,
- por que `RENOVADA` reemplaza `VIGENTE` en lugar de mantener historial de versiones,
- como evolucionar esto a microservicios o a un monolito modular robusto,
- como harian auditoria real de cambios,
- como modelar una renovacion como nueva poliza en vez de mutar la existente.

## 21. UI React incluida

La UI en `ui/` y el cliente de `docs/react-test-client/` funcionan como herramientas de prueba manual.

Caracteristicas:

- permite configurar `Base URL`,
- permite configurar `API Key`,
- permite escoger `GET` o `POST`,
- permite enviar body JSON,
- muestra status y respuesta textual.

Valor real:

- acelera demos,
- evita depender de Postman,
- deja claro el contrato HTTP esperado por el backend.

Limitaciones:

- no hay tipado de respuestas en runtime,
- no hay manejo sofisticado de errores,
- no hay formularios por caso de uso,
- no hay estado de negocio,
- no es una UI de producto, solo un tester.

## 22. Ruta recomendada de estudio

Si quieres estudiar este proyecto a profundidad, el orden mas eficiente es este:

### Paso 1. Entender la historia funcional

Pregunta central:

- que problema resuelve el sistema y cuales son sus operaciones de negocio.

Lee:

- `README.md`
- este documento

### Paso 2. Entender el dominio

Lee:

- `Poliza`
- `Riesgo`
- enums

Pregunta:

- que estados existen y como cambian.

### Paso 3. Entender el flujo HTTP

Lee:

- `ApiKeyFilter`
- `CorrelationIdFilter`
- controllers
- `GlobalExceptionHandler`

Pregunta:

- como entra una request y como sale una respuesta o error.

### Paso 4. Entender reglas de negocio

Lee:

- `PolizaService`
- `RiesgoService`

Pregunta:

- que operaciones mutan estado y que validaciones se hacen antes.

### Paso 5. Entender persistencia y datos iniciales

Lee:

- repositories
- `DataInitializer`
- `application.yml`

Pregunta:

- con que dataset arranca el sistema y por que.

### Paso 6. Entender testing

Lee:

- `PolizasApiIntegrationTest`

Pregunta:

- que cosas quedaron protegidas por pruebas y cuales no.

### Paso 7. Detectar deuda y evolucion

Pregunta:

- que habria que cambiar para llevar esto a un entorno mas serio.

## 23. Preguntas de estudio sugeridas

Usa estas preguntas para autoevaluarte:

1. Por que `PolizaRepository` usa `@EntityGraph` en vez de confiar en lazy loading?
2. Que diferencia conceptual hay entre cancelar una poliza y cancelar un riesgo?
3. Que tradeoff tiene mutar la poliza al renovar en lugar de crear una nueva version?
4. Que bug potencial introduce la diferencia entre los datos semilla y la prueba de integracion?
5. Que pasaria si faltara `OPTIONS` en `ApiKeyFilter` para el navegador?
6. Por que el `CoreMockService` vive en `service` y no en `controller`?
7. Que ventajas y limites tiene usar records para DTOs en este caso?
8. Como convertirias la API key actual en un mecanismo de seguridad mas serio?
9. Que validaciones de negocio faltan para endurecer el dominio?
10. Si migraras de H2 a PostgreSQL, que partes del proyecto deberian revisarse primero?

## 24. Posibles mejoras futuras

Sin cambiar el objetivo del ejercicio, las mejoras naturales serian:

- agregar OpenAPI/Swagger,
- incorporar Spring Security,
- usar Flyway o Liquibase,
- separar casos de uso mas explicitamente,
- registrar auditoria por usuario,
- endurecer validaciones del dominio,
- ampliar cobertura de pruebas,
- alinear README, fixtures y tests,
- modelar renovaciones como entidades historicas,
- agregar logs estructurados y metricas.

## 25. Comandos utiles para repasar el proyecto

Backend:

```bash
mvn spring-boot:run
```

Pruebas:

```bash
mvn test
```

Frontend:

```bash
cd ui
npm install
npm run dev
```

## 26. Tesis corta para defender el proyecto

Si tuvieras que explicarlo en poco tiempo, una defensa tecnica correcta seria:

"Es una API REST en Spring Boot para gestionar polizas y riesgos con arquitectura por capas, persistencia temporal en H2, seguridad minima por API key, trazabilidad por correlation id, integracion mock con un CORE legado y pruebas de integracion basicas. La solucion resuelve las operaciones principales del dominio, aunque mantiene simplificaciones deliberadas en seguridad, versionado, persistencia productiva y cobertura de pruebas. El principal hallazgo actual es una desalineacion entre datos semilla, README y una prueba de integracion."

## 27. Conclusiones clave

- El proyecto esta bien orientado para un challenge o entrega inicial.
- La separacion por capas es clara y defendible.
- La lectura del sistema es rapida porque las responsabilidades estan bastante limpias.
- La mayor debilidad actual no es la arquitectura sino la consistencia entre codigo, tests y documentacion.
- Si estudias este proyecto, lo mas valioso no es repetir endpoints sino entender donde estan las invariantes del dominio y donde todavia faltan.

## 28. Checklist final de estudio

Puedes considerar que ya entiendes bien el proyecto si eres capaz de:

- explicar el flujo completo de una request sin mirar el codigo,
- nombrar las reglas de negocio implementadas y las faltantes,
- justificar por que hay filtros antes de controllers,
- describir el modelo `Poliza` -> `Riesgo`,
- explicar como se calcula una renovacion,
- identificar al menos 3 mejoras concretas,
- señalar la inconsistencia actual entre `DataInitializer`, tests y README.
