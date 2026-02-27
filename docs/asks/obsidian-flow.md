# Flujo de Obsidian para esta prueba

## Objetivo
Tener trazabilidad rapida de arquitectura, decisiones, pruebas y evidencias de ejecucion dentro de una vault de Obsidian.

## Estructura recomendada de carpetas
- `00-Inbox/` capturas rapidas
- `01-Requisitos/` resumen de lo pedido
- `02-Arquitectura/` decisiones tecnicas
- `03-Implementacion/` endpoints, reglas y cambios
- `04-Pruebas/` casos ejecutados y resultados
- `05-Entrega/` checklist final y links a archivos

## Flujo diario (paso a paso)
1. Crear nota `01-Requisitos/brief.md` con alcance y restricciones.
2. Crear nota `02-Arquitectura/decisiones.md` con decisiones (H2, API key, capas, IPC).
3. Crear una nota por bloque tecnico en `03-Implementacion/`:
   - `api-endpoints.md`
   - `reglas-negocio.md`
   - `seguridad.md`
   - `core-mock.md`
4. Registrar cada corrida en `04-Pruebas/ejecuciones.md`:
   - comando
   - fecha/hora
   - resultado
5. Cerrar en `05-Entrega/checklist.md` validando entregables y rutas.

## Plantilla de nota tecnica
```markdown
# Titulo
## Contexto
## Decision
## Impacto
## Evidencia
- Archivo:
- Comando:
- Resultado:
```

## Convenciones utiles
- Usar tags: `#api #springboot #polizas #test #entrega`
- Enlazar archivos del repo con rutas relativas, ejemplo: `[[../src/main/java/com/company/polizas/service/PolizaService.java]]`
- Mantener una nota indice: `MOC - Gestion Polizas`.

## Checklist de cierre en Obsidian
- [ ] Endpoints obligatorios documentados
- [ ] Reglas de negocio validadas
- [ ] Seguridad `x-api-key` verificada
- [ ] Pruebas `mvn test` registradas
- [ ] Docs de modulos 1,3,4,5 enlazados
- [ ] README actualizado enlazado
