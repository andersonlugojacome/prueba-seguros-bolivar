🚀 PRUEBA Desarrollador TI/Sénior
⏱ Duración total sugerida: 4 horas
Se divide en 5 módulos, cada uno evaluando competencias distintas.
🧩 MÓDULO 1 – Diseño de Sistema
(System Design)
Duración: 60 minutos
Evalúa: Arquitectura, diseño, escalabilidad, pensamiento técnico estratégico.
Caso: Plataforma de Gestión de Pólizas
Nuestro negocio maneja dos tipos de pólizas para arrendamiento de inmuebles: Individual y Colectiva. Las pólizas colectivas están orientadas a las inmobiliarias y Administraciones de copropiedades, se aseguran a los arrendatarios de los inmuebles y en caso de siniestros los beneficiarios son los arrendadores. En las pólizas individuales el tomador y el asegurado es el arrendatario y el beneficiario es el arrendador.
Una póliza colectiva puede tener uno o muchos riesgos, en una póliza individual solo
tiene un riesgo.
Todas las pólizas cuentan con: periodo de vigencia, un valor de canón mensual de
arrendamiento, un valor de prima que corresponde al valor canon mensual por número de
meses de la vigencia, igualmente, todas las pólizas pueden ser renovadas por el mismo
periodo de vigencia inicial y para ello se debe ajustar el valor de canon según incremento del IPC.
El negocio necesita una plataforma que permita:
1. Crear, consultar y modificar pólizas (individuales y Colectivas).
2. Renovación automática basada en reglas de negocio.
3. Eventos de notificación (correo/SMS) para la creación y renovación.
4. Integración con un CORE transaccional legado.
5. Disponibilidad 24/7 y resiliencia.

Se requiere implementar un API que permita la gestión de pólizas así:
1. Para colectivas: Agregar riesgos, eliminar (cancelar) riesgos
2. Para individuales: Soportar los consumos necesarios del front.
3. Todas las acciones que modifiquen estados de pólizas y/o riesgos, deben consumir un servicio agnóstico de edición, disponibilizado a través de capa media en weblogic, el cuál mantiene actualizado el sistema CORE de seguros.

Actividades a realizar
1. Diseñar la arquitectura de alto nivel del sistema.
2. Seleccionar 3 patrones de arquitectura y justificar por qué:
○ Ej.: event-driven, hexagonal, CQRS, API Gateway, microservicios, etc.
3. Describir el modelo de datos principal (sin entrar en SQL detallado).
4. Explicar cómo manejaría:
○ Escalabilidad
○ Logs y observabilidad
○ Tolerancia a fallos
○ Versionamiento de APIs
5. Diagramar (texto o dibujo simple) los componentes principales:
○ Servicio de Pólizas
○ Servicio de Riesgos
○ Servicio de Notificaciones
○ Adapter de integración con CORE
○ Base de datos
○ API Gateway
󰞵 MÓDULO 2 – Prueba Técnica Práctica
(Hands-on Code Challenge)
Duración: 90 minutos
Evalúa: Estilo de codificación, lógica, buenas prácticas, arquitectura limpia.
Caso Técnico Simplificado: API de Gestión de Pólizas
Implementar solo lo esencial:
Requerimientos
1. GET /polizas
Listar pólizas por “tipo” y “estado”
.
2. GET /polizas/{id}/riesgos
3. POST /polizas/{id}/renovar
○
Incrementa canon y prima en +IPC
○
Estado pasa a “RENOVADA”
4. POST /polizas/{id}/cancelar
5. POST /polizas/{id}/riesgos
Solo si tipo = Colectiva.
6. POST /riesgos/{id}/cancelar
Reglas de negocio esenciales
●
●
●
●
Una póliza individual solo puede tener 1 riesgo.
No se puede renovar una póliza cancelada.
La cancelación de una póliza cancela todos sus riesgos.
Agregar riesgo exige validación del tipo de póliza.
Mock externo obligatorio
Implementar un endpoint simple:
POST /core-mock/evento
{
"evento": "ACTUALIZACION"
,
"polizaId": 555
}
Su único propósito:
registrar en logs que la operación se intentó enviar al CORE.
Seguridad mínima
Header obligatorio: x-api-key: 123456
Lo que debe entregar
● Estructura del proyecto (Spring Boot + capas: controller, service, repository).
● Entidades básicas (Poliza, Riesgo).
● Código funcional para los endpoints.
● Validaciones de negocio.
● Archivo README con instrucciones.
MÓDULO 3 – Conocimientos en BBDD
Duración: 30 minutos
Optimización de Base de Datos (SQL / PL/SQL) Tienes una tabla orders con 10
millones de registros y una tabla customers con 500,000 registros. Necesitas ejecutar
frecuentemente la siguiente consulta para encontrar los pedidos de clientes de 'México'
,
pero es muy lenta.
SQL
None
SELECT
o.order_id,
o.order_date,
c.customer_name,
o.total_amount
FROM
orders o
JOIN
customers c ON o.customer_id = c.customer_id
WHERE
c.country =
'México';
Describe al menos tres estrategias diferentes que implementarías para optimizar el
rendimiento de esta consulta.
MÓDULO 4 – Conocimientos en versionamiento
Duración: 30 minutos
Git y GitHub (Manejo de Versiones) Estás trabajando en una rama
feature/new-login. Mientras tanto, un compañero ha fusionado un cambio crítico en la
rama main que soluciona un bug de seguridad. Necesitas incorporar urgentemente ese
cambio específico de main en tu rama feature/new-login sin traer el resto de las
actualizaciones de main. ¿Qué comando o estrategia de Git usarías y por qué?
🧠 MÓDULO 5 – Evaluación de Liderazgo Técnico y Gestión
Duración: 30 minutos
Evalúa: Liderazgo, priorización, comunicación, capacidad de dirección técnica.
Caso de Liderazgo
Tu equipo tiene 8 desarrolladores. Hay:
● 40% deuda técnica en servicios claves
● 10 incidentes críticos en el último mes
● Presión del negocio para entregar una funcionalidad en 3 semanas
● Falta de estándares y práctica irregular de code review
● Dos desarrolladores junior con brechas técnicas importantes
Preguntas a responder
1. ¿Cuáles serían tus 5 prioridades en las primeras 2 semanas?
2. ¿Cómo organizarías al equipo para mejorar velocidad y calidad?
3. ¿Qué métricas implementarías para evaluar el desempeño del área?
4. ¿Qué prácticas técnicas establecerías como obligatorias?
5. ¿Cómo gestionarías la presión del negocio sin comprometer la calidad?