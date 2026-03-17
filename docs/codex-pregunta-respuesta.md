# Codex - Pregunta Respuesta para Estudio Oral

## Como usar este documento

Este archivo esta pensado para estudio oral, defensa tecnica y practica de entrevista.

Forma recomendada de uso:

- lee solo la pregunta,
- intenta responder sin mirar,
- compara con la respuesta sugerida,
- si puedes, amplia con la repregunta.

La idea no es memorizar frases exactas, sino dominar el razonamiento tecnico del proyecto.

## 1. Vision general

### Pregunta: Que hace este proyecto?

Respuesta:

Es una API REST construida con Spring Boot para gestionar polizas y riesgos. Permite listar polizas, listar riesgos por poliza, renovar polizas, cancelar polizas, agregar riesgos a polizas colectivas, cancelar riesgos y registrar eventos hacia un CORE mock. Adicionalmente incluye una UI React minima para pruebas manuales.

Repregunta:

- Si tuvieras que explicarlo en 20 segundos, que dirias?

### Pregunta: Cual es el objetivo tecnico principal del ejercicio?

Respuesta:

Demostrar una solucion backend funcional con arquitectura por capas, reglas de negocio basicas, persistencia con JPA, validaciones simples, manejo uniforme de errores, seguridad minima por API key y pruebas de integracion.

Repregunta:

- Que partes se ven como challenge tecnico y cuales como producto real?

### Pregunta: Cual es el stack principal?

Respuesta:

En backend usa Java 17, Spring Boot 3.3.8, Spring Web, Spring Validation, Spring Data JPA y H2. En frontend usa React 19, TypeScript y Vite. Para pruebas usa Spring Boot Test con MockMvc.

Repregunta:

- Por que H2 es adecuada para este tipo de ejercicio?

## 2. Arquitectura

### Pregunta: Que estilo de arquitectura usa el proyecto?

Respuesta:

Usa una arquitectura por capas. Los controllers reciben requests HTTP, los services contienen la logica de negocio, los repositories acceden a datos, las entidades modelan el dominio, los DTOs exponen contratos y la capa de configuracion resuelve preocupaciones transversales como seguridad simple, CORS, correlation id y datos semilla.

Repregunta:

- Que ventaja tiene esta arquitectura en un ejercicio pequeno?

### Pregunta: Por que no toda la logica esta en los controllers?

Respuesta:

Porque el controller debe ser una capa delgada enfocada en HTTP. Si metes logica de negocio ahi, mezclas transporte con dominio, complicas pruebas y dificultas reutilizacion. En este proyecto el controller delega casi todo a los services, que es la ubicacion correcta para reglas como renovar o cancelar.

Repregunta:

- Que problemas concretos aparecerian si renovar poliza viviera en el controller?

### Pregunta: Cual es el flujo completo de una request?

Respuesta:

La request entra al servidor, pasa primero por `CorrelationIdFilter`, luego por `ApiKeyFilter`, despues llega al controller, el controller llama al service, el service usa repositories y dominio, opcionalmente notifica al `CoreMockService`, transforma a DTO y devuelve la respuesta. Si ocurre un error, `GlobalExceptionHandler` lo convierte en un payload uniforme.

Repregunta:

- Por que importa entender ese orden exacto?

## 3. Dominio

### Pregunta: Cuales son las entidades principales?

Respuesta:

Las entidades principales son `Poliza` y `Riesgo`. Una poliza agrupa informacion de vigencia, tipo, estado, valores economicos y una coleccion de riesgos. Un riesgo pertenece a una sola poliza y tiene estado y descripcion.

Repregunta:

- Cual es la relacion cardinal entre poliza y riesgo?

### Pregunta: Que tipos de poliza existen?

Respuesta:

Existen dos tipos: `INDIVIDUAL` y `COLECTIVA`.

Repregunta:

- Que implicacion de negocio tiene esa diferencia en el sistema?

### Pregunta: Que estados de poliza existen?

Respuesta:

Los estados son `VIGENTE`, `RENOVADA` y `CANCELADA`.

Repregunta:

- Te parece suficiente este modelo de estados para un sistema real?

### Pregunta: Que estados de riesgo existen?

Respuesta:

Los estados son `ACTIVO` y `CANCELADO`.

Repregunta:

- Faltaria algun estado intermedio en un caso productivo?

### Pregunta: Como esta modelada la relacion entre poliza y riesgo?

Respuesta:

En `Poliza` hay un `@OneToMany` hacia `Riesgo` con `cascade = ALL` y `orphanRemoval = true`. En `Riesgo` hay un `@ManyToOne(fetch = LAZY)` hacia `Poliza`. Eso permite que la poliza sea la raiz agregada desde la cual se persisten y consultan sus riesgos.

Repregunta:

- Por que `fetch = LAZY` en `Riesgo` tiene sentido?

## 4. Reglas de negocio

### Pregunta: Cuales son las reglas de negocio principales implementadas?

Respuesta:

Las principales son: una poliza individual no permite agregar riesgos adicionales, solo una poliza colectiva admite agregar riesgos, una poliza cancelada no se puede renovar, cancelar una poliza cancela todos sus riesgos, cancelar un riesgo cambia su estado y toda operacion mutante intenta notificar al CORE mock.

Repregunta:

- Cuales de esas reglas estan realmente protegidas por pruebas?

### Pregunta: Por que una poliza individual no permite agregar riesgos?

Respuesta:

Porque esa es una restriccion funcional del dominio definida en el ejercicio. El sistema la aplica en `PolizaService`, donde valida que solo las polizas `COLECTIVA` puedan usar el endpoint de agregar riesgo.

Repregunta:

- Esa restriccion esta solo en codigo o tambien en base de datos?

### Pregunta: Que pasa cuando se cancela una poliza?

Respuesta:

La poliza cambia su estado a `CANCELADA` y todos sus riesgos pasan a `CANCELADO`. Luego se persiste el cambio y se intenta notificar al CORE mock.

Repregunta:

- Por que tiene sentido cancelar tambien los riesgos?

### Pregunta: Que pasa cuando se renueva una poliza?

Respuesta:

Se valida primero que no este cancelada. Luego se recalcula el canon usando el IPC configurado, se recalcula la prima, se ajustan fechas de vigencia, la poliza pasa a estado `RENOVADA`, se guarda y se intenta notificar al CORE mock.

Repregunta:

- Renovar deberia mutar la poliza existente o crear una nueva version historica?

### Pregunta: Que validaciones de negocio faltan?

Respuesta:

Faltan varias validaciones de endurecimiento, por ejemplo impedir agregar riesgos a polizas canceladas, impedir cancelar dos veces la misma poliza o riesgo, impedir renovaciones repetidas no deseadas y reforzar ciertas invariantes del dominio desde persistencia o reglas mas explicitas.

Repregunta:

- Cual de esas faltantes priorizarias primero y por que?

## 5. Seguridad y cross-cutting concerns

### Pregunta: Como se asegura la API?

Respuesta:

Se asegura con un filtro muy simple que valida el header `x-api-key` contra un valor configurado en `application.yml`. Si la clave no llega o no coincide, retorna `401 Unauthorized`.

Repregunta:

- Esto es suficiente para produccion?

### Pregunta: Por que el sistema deja pasar `OPTIONS` sin validar API key?

Respuesta:

Porque los navegadores usan requests `OPTIONS` para preflight CORS. Si el filtro exigiera API key ahi, el frontend podria fallar antes de hacer la llamada real. Permitir `OPTIONS` facilita interoperabilidad con la UI local.

Repregunta:

- Que problema practico verias en el navegador si eso no existiera?

### Pregunta: Que hace `CorrelationIdFilter`?

Respuesta:

Lee el header `x-correlation-id` si viene, y si no viene genera uno nuevo. Luego lo guarda en MDC, lo devuelve en la respuesta y registra un log con ese id para que cada request quede trazable.

Repregunta:

- Por que eso es util aunque el sistema sea pequeno?

### Pregunta: Que hace `WebCorsConfig`?

Respuesta:

Permite requests desde `http://localhost:*` y `http://127.0.0.1:*`, habilita metodos comunes y expone el header `x-correlation-id` al cliente. Esta pensado para desarrollo local con la UI incluida.

Repregunta:

- Lo dejarias igual en produccion?

## 6. Persistencia

### Pregunta: Que base de datos usa el proyecto?

Respuesta:

Usa H2 en memoria. Eso significa que la informacion se pierde al reiniciar la aplicacion y que el esquema se crea y destruye automaticamente.

Repregunta:

- Que ventaja y que desventaja tiene eso?

### Pregunta: Que significa `ddl-auto: create-drop`?

Respuesta:

Significa que Hibernate crea el esquema al iniciar la aplicacion y lo elimina al detenerla. Es util para pruebas y demos porque evita tareas manuales de setup, pero no es apto para persistencia real.

Repregunta:

- Que herramienta usarias en un entorno mas serio?

### Pregunta: Por que `PolizaRepository` usa `@EntityGraph(attributePaths = "riesgos")`?

Respuesta:

Porque las respuestas de poliza necesitan conocer los riesgos asociados, al menos para calcular `totalRiesgos`. Con `@EntityGraph` se cargan de forma controlada y se evita depender de lazy loading fuera del contexto transaccional.

Repregunta:

- Que error podria aparecer si eso no estuviera y se accediera a la coleccion tarde?

### Pregunta: Para que sirve `RiesgoRepository.findByPolizaId`?

Respuesta:

Sirve para consultar riesgos por id de poliza si se quisiera acceder directamente desde el repositorio. En el estado actual del proyecto casi no se usa porque el flujo principal navega desde `Poliza` hacia su coleccion de riesgos.

Repregunta:

- Lo eliminarias por ahora o lo mantendrias?

## 7. Servicios y logica

### Pregunta: Cual es el service mas importante del sistema?

Respuesta:

`PolizaService`, porque centraliza la mayor parte de la logica de negocio: listar polizas, listar riesgos por poliza, renovar, cancelar y agregar riesgos.

Repregunta:

- Que responsabilidades concretas asume ese service ademas de llamar repositorios?

### Pregunta: Como funciona `listar` en `PolizaService`?

Respuesta:

Recibe filtros opcionales `tipo` y `estado`. Si llegan ambos, filtra por ambos; si llega uno solo, filtra por ese; y si no llega ninguno, devuelve todas las polizas. Luego transforma entidades a `PolizaResponse`.

Repregunta:

- Como defenderias esta implementacion frente a alguien que prefiera Specifications?

### Pregunta: Como calcula el sistema la renovacion?

Respuesta:

Toma el `canonMensual`, lo multiplica por `1 + ipc`, redondea a dos decimales, luego calcula la nueva prima multiplicando el canon nuevo por la vigencia en meses. Despues mueve la fecha de inicio al dia siguiente de la fecha fin previa y calcula la nueva fecha fin.

Repregunta:

- Ves alguna ambiguedad en ese manejo de fechas?

### Pregunta: Que hace `CoreMockService` exactamente?

Respuesta:

No hace una integracion real. Registra en logs el intento de notificar al CORE y, cuando se usa el endpoint del mock, registra el evento recibido. Actua como una abstraccion simple para representar una dependencia externa.

Repregunta:

- Como lo evolucionarias si mañana hubiera que llamar a un CORE real?

## 8. API y contratos

### Pregunta: Que endpoints expone `PolizaController`?

Respuesta:

Expone:

- `GET /polizas`
- `GET /polizas/{id}/riesgos`
- `POST /polizas/{id}/renovar`
- `POST /polizas/{id}/cancelar`
- `POST /polizas/{id}/riesgos`

Repregunta:

- Por que estos endpoints se consideran coherentes con el dominio?

### Pregunta: Que endpoint expone `RiesgoController`?

Respuesta:

Expone `POST /riesgos/{id}/cancelar`.

Repregunta:

- Por que no se puso este endpoint dentro de `/polizas/{id}/riesgos/{riesgoId}`?

### Pregunta: Que endpoint expone `CoreMockController`?

Respuesta:

Expone `POST /core-mock/evento`, que recibe un evento y un `polizaId` para simular la recepcion o registro de una notificacion hacia el CORE.

Repregunta:

- En un sistema real, este endpoint seria publico?

### Pregunta: Que DTOs importantes existen?

Respuesta:

Los principales son:

- `PolizaResponse`
- `RiesgoResponse`
- `AgregarRiesgoRequest`
- `CoreEventoRequest`
- `ErrorResponse`

Repregunta:

- Por que es buena idea usar DTOs y no devolver entidades JPA directamente?

### Pregunta: Que validacion hay en `AgregarRiesgoRequest`?

Respuesta:

La descripcion se valida con `@NotBlank`, lo que evita que llegue vacia o solo con espacios.

Repregunta:

- Por que `@NotBlank` es mejor que `@NotNull` en este caso?

## 9. Errores y respuestas

### Pregunta: Como maneja errores la API?

Respuesta:

Con `GlobalExceptionHandler`. Este componente captura excepciones de negocio, de no encontrado, de validacion y errores inesperados, y devuelve una respuesta uniforme con timestamp, status, error, message y path.

Repregunta:

- Que gana el cliente al tener ese formato estable?

### Pregunta: Que codigo devuelve una poliza inexistente?

Respuesta:

Devuelve `404` porque el service lanza `NotFoundException` y el handler la traduce a `NOT_FOUND`.

Repregunta:

- Donde exactamente se genera esa excepcion?

### Pregunta: Que codigo devuelve una regla de negocio violada?

Respuesta:

Devuelve `400`, por ejemplo cuando se intenta renovar una poliza cancelada o agregar un riesgo a una poliza individual.

Repregunta:

- Te parece correcto usar `400` en ambos casos?

## 10. Datos semilla

### Pregunta: Con que datos arranca el sistema?

Respuesta:

Arranca con tres polizas:

- una individual vigente con un riesgo activo,
- una colectiva vigente con dos riesgos activos,
- una individual cancelada con un riesgo cancelado.

Repregunta:

- Por que ese dataset es suficiente para probar casi todos los flujos?

### Pregunta: Para que sirve `DataInitializer`?

Respuesta:

Sirve para poblar la base automaticamente al arrancar, siempre que este vacia. Eso acelera pruebas manuales, demos y testing local.

Repregunta:

- Que ventaja tiene condicionar la carga a `polizaRepository.count() > 0`?

## 11. Testing

### Pregunta: Que tipo de pruebas hay?

Respuesta:

Hay pruebas de integracion usando `@SpringBootTest` y `MockMvc`. Eso permite levantar el contexto completo y probar endpoints, filtros, servicios, persistencia y manejo de errores de manera integrada.

Repregunta:

- Por que no son pruebas unitarias puras?

### Pregunta: Que validan hoy las pruebas?

Respuesta:

Validan que:

- sin API key la API responda `401`,
- renovar una poliza cancelada responda `400`,
- agregar riesgo a una poliza individual responda `400`,
- cancelar una poliza cancele tambien sus riesgos,
- el filtro de `GET /polizas` funcione por tipo y estado.

Repregunta:

- Que caso importante agregarias inmediatamente?

### Pregunta: Que huecos de testing tiene el proyecto?

Respuesta:

Faltan pruebas para correlation id, validaciones de payload invalido, cancelacion de riesgo, endpoint del CORE mock, escenarios no encontrados, renovacion exitosa y varios bordes de negocio.

Repregunta:

- Cual de esos huecos representa mayor riesgo funcional?

## 12. UI React

### Pregunta: Para que sirve la UI en `ui/`?

Respuesta:

Sirve como cliente manual para probar la API sin usar Postman ni curl. Permite configurar la URL base, la API key, el metodo, el endpoint y el body JSON, y muestra status y respuesta.

Repregunta:

- La defenderias como frontend de producto o solo como herramienta de soporte?

### Pregunta: Por que esa UI es util aunque sea simple?

Respuesta:

Porque reduce friccion para demostrar el backend, facilita pruebas manuales y deja visible el contrato HTTP. En un challenge tecnico eso da velocidad para validar comportamientos frente a un evaluador.

Repregunta:

- Que mejora minima le agregarias sin convertirla en un proyecto grande?

## 13. Diseno y decisiones tecnicas

### Pregunta: Que decisiones tecnicas son defendibles en este proyecto?

Respuesta:

Son defendibles la arquitectura por capas, el uso de DTOs, el manejo uniforme de errores, el uso de filtros para concerns transversales, H2 para rapidez de setup, MockMvc para pruebas integradas y un servicio mock para representar integracion con sistemas externos.

Repregunta:

- Cual de esas decisiones cambiarias primero si el proyecto escalara?

### Pregunta: Que simplificaciones son conscientes y no necesariamente errores?

Respuesta:

La seguridad por API key simple, la base en memoria, la ausencia de migraciones, la integracion mock en vez de real, la UI minima y la cobertura parcial de pruebas. Son simplificaciones razonables para una entrega tecnica acotada.

Repregunta:

- Como distinguir una simplificacion valida de una mala practica?

### Pregunta: Cual es la principal deuda tecnica actual?

Respuesta:

La deuda mas visible ya no es la consistencia de fixtures, porque eso se ajusto. Ahora el foco estaria en ampliar validaciones de negocio, endurecer seguridad, mejorar pruebas y pensar un modelado mas robusto de renovaciones e historial.

Repregunta:

- Cual de esas deudas afecta mas a negocio y cual mas a mantenibilidad?

## 14. Evolucion futura

### Pregunta: Que mejorarias primero si esto fuera a produccion?

Respuesta:

Primero reemplazaria la seguridad simple por Spring Security con autenticacion real, moveria la base a PostgreSQL, agregaria migraciones con Flyway o Liquibase, aumentaria cobertura de pruebas, mejoraria observabilidad y revisaria el modelado de renovacion para soportar historial.

Repregunta:

- En que orden haria esas mejoras y por que?

### Pregunta: Como evolucionarias la integracion con el CORE?

Respuesta:

La encapsularia detras de una interfaz o adapter dedicado, agregaria cliente HTTP real o mensajeria, timeouts, reintentos, logs estructurados, metricas y mecanismos de resiliencia como circuit breaker o colas segun criticidad.

Repregunta:

- Cuando conviene usar integracion sincrona y cuando asincrona?

### Pregunta: Como modelarias mejor la renovacion?

Respuesta:

En un sistema mas completo probablemente modelaria la renovacion como una nueva version de poliza o una nueva entidad relacionada con historial, en lugar de mutar la misma fila. Eso mejora trazabilidad, auditoria y analisis historico.

Repregunta:

- Que tradeoff tiene esa mejora frente a la simplicidad actual?

## 15. Preguntas trampa o de profundidad

### Pregunta: Por que `RENOVADA` podria ser discutible como estado final?

Respuesta:

Porque una poliza renovada podria seguir siendo funcionalmente vigente, y usar `RENOVADA` como estado podria mezclar evento historico con estado actual. A veces es mejor distinguir entre "ocurrio una renovacion" y "cual es el estado vigente del contrato".

### Pregunta: Que problema de modelado temporal ves en `nuevoFin = nuevoInicio.plusMonths(vigenciaMeses)`?

Respuesta:

Puede haber ambiguedad respecto a si la fecha fin es inclusiva o exclusiva. Dependiendo de la interpretacion de negocio, sumar meses directamente podria introducir un desfase de un dia o una vigencia efectiva distinta a la esperada.

### Pregunta: Que limitacion tiene `CoreEventoRequest` al validar `evento`?

Respuesta:

Usa `@NotNull`, pero no `@NotBlank`. Eso significa que una cadena vacia o solo espacios podria pasar la validacion aunque semantica y funcionalmente no tenga sentido.

### Pregunta: Que ventaja da `MockMvc` en este proyecto?

Respuesta:

Permite probar el contrato HTTP y el comportamiento integrado del backend sin desplegar manualmente el servidor en un puerto real. Es ideal para validar endpoints, filtros, serializacion y respuestas de error dentro del contexto de Spring.

## 16. Respuestas cortas de defensa

### Pregunta: Defiende el proyecto en una respuesta corta

Respuesta:

Es una solucion backend limpia y suficiente para un challenge tecnico: tiene arquitectura por capas, reglas de negocio centrales, persistencia con JPA, filtros para seguridad y trazabilidad, manejo uniforme de errores, datos semilla, pruebas de integracion y una UI minima para demo.

### Pregunta: Cual es su mayor fortaleza?

Respuesta:

La claridad estructural. Se entiende rapido donde vive cada responsabilidad y eso facilita lectura, mantenimiento y defensa tecnica.

### Pregunta: Cual es su principal punto de mejora?

Respuesta:

Endurecer el dominio y el entorno: mas validaciones, mas pruebas, mejor seguridad y mejor modelado historico de renovaciones.

## 17. Mini simulacro oral

### Pregunta: Explicame el flujo de cancelar una poliza

Respuesta modelo:

La request entra al backend, pasa por correlation id y validacion de API key, llega a `PolizaController` y se delega a `PolizaService.cancelarPoliza`. Ahi se obtiene la poliza, se cambia su estado a `CANCELADA`, se recorren sus riesgos para marcarlos como `CANCELADO`, se intenta notificar al CORE mock y se persiste la entidad. Finalmente se devuelve un `PolizaResponse`. Si la poliza no existe, el sistema responde `404` mediante el manejador global.

### Pregunta: Explicame el flujo de agregar un riesgo

Respuesta modelo:

La request llega al endpoint `POST /polizas/{id}/riesgos`, el controller valida el body y delega al service. `PolizaService` busca la poliza, verifica que sea `COLECTIVA`, crea un nuevo riesgo activo con la descripcion recibida, lo guarda usando `RiesgoRepository`, notifica al CORE mock y devuelve un `RiesgoResponse`. Si la poliza es individual, responde `400`.

### Pregunta: Explicame por que este proyecto esta bien para un challenge pero no para produccion

Respuesta modelo:

Porque resuelve correctamente los flujos principales con buena organizacion tecnica, pero conserva simplificaciones deliberadas: seguridad basica por API key, base H2 en memoria, integracion mock, pocas pruebas, sin migraciones ni auditoria real y con un modelado de dominio todavia simple. Para un challenge eso esta bien; para produccion, no alcanza.

## 18. Checklist final de dominio oral

Si puedes responder esto sin mirar el codigo, ya tienes buen dominio:

- Que hace el sistema y que endpoints tiene.
- Como estan modeladas polizas y riesgos.
- Cuales son las reglas de negocio principales.
- Como funciona la seguridad por API key.
- Que hace el correlation id.
- Como se calcula una renovacion.
- Que valida hoy la suite de pruebas.
- Que mejoras tecnicas harias si esto creciera.
