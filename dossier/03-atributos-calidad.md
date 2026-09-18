# 03 — Atributos de calidad · NOVI

## Criterios de selección

Los atributos de calidad se derivan directamente de los drivers arquitectónicos
definidos en `02-stakeholders-drivers.md`. Solo se incluyen atributos que pueden
medirse en el sistema base actual o evaluarse mediante escenarios concretos.

---

## QA-01 · Disponibilidad

**Driver:** DRIVER-03
**Prioridad:** Alta

El sistema debe responder correctamente durante sesiones activas de chat y
revisión de actividades. La caída del backend durante una clase implica pérdida
de comunicación entre profesores y estudiantes.

**Definición operacional:** el endpoint `GET /api/grupos` responde con HTTP 200
en al menos el 95 % de las solicitudes bajo carga normal (≤ 10 usuarios
concurrentes).

**Relación con riesgos:** R-01 (pool HikariCP) es el principal inhibidor de
disponibilidad en el sistema actual. Ver `09-inventario-riesgos.md`.

---

## QA-02 · Rendimiento (latencia)

**Driver:** DRIVER-04
**Prioridad:** Alta

Los mensajes del chat grupal deben percibirse como en tiempo real. Las
respuestas de la API REST deben ser lo suficientemente rápidas para no
interrumpir el flujo de trabajo del profesor.

**Definición operacional:** la latencia p95 del endpoint `GET /api/grupos`
bajo 10 VUs durante 60 s no debe superar los 800 ms.

**Hipótesis inicial (pre-medición):** con la configuración actual de HikariCP
(10 conexiones) y sin índices adicionales, se espera una latencia p95 entre
300 ms y 600 ms para la operación de listar grupos con la semilla de 5 grupos
por usuario.

---

## QA-03 · Seguridad

**Driver:** DRIVER-01
**Prioridad:** Crítica

La información académica intercambiada (mensajes, actividades, rúbricas) es
privada. El token JWT no debe ser accesible a scripts de terceros.

**Definición operacional:** ningún endpoint protegido devuelve datos con HTTP
200 sin un token JWT válido en el encabezado `Authorization`.

**Relación con riesgos:** R-02 (localStorage) y R-05 (CORS wildcard) son las
vulnerabilidades activas documentadas en `09-inventario-riesgos.md`.

---

## QA-04 · Modificabilidad

**Driver:** DRIVER-02
**Prioridad:** Alta

El equipo debe poder agregar nuevos endpoints o modificar los existentes sin
romper el chat WebSocket ni requerir un redespliegue manual del frontend.

**Definición operacional:** un cambio en la capa de servicio (`service/`) que
no altere el contrato de la API (`@RequestMapping`) no requiere modificar
ningún archivo del frontend.

---

## QA-05 · Testeabilidad

**Driver:** DRIVER-05
**Prioridad:** Crítica — bloquea Checkpoint Hito 5

El sistema debe poder verificarse automáticamente en CI. Un cambio que rompa
el login o el acceso a grupos debe detectarse antes de llegar a `main`.

**Definición operacional:** el pipeline de CI ejecuta al menos una prueba de
integración que cubre el flujo registro → login → acceso a recurso protegido,
y reporta fallo si algún paso devuelve un código de error inesperado.

---

## Tabla resumen

| ID | Atributo | Prioridad | Driver | Riesgo relacionado |
|---|---|---|---|---|
| QA-01 | Disponibilidad | Alta | DRIVER-03 | R-01 |
| QA-02 | Rendimiento | Alta | DRIVER-04 | R-01 |
| QA-03 | Seguridad | Crítica | DRIVER-01 | R-02, R-05 |
| QA-04 | Modificabilidad | Alta | DRIVER-02 | — |
| QA-05 | Testeabilidad | Crítica | DRIVER-05 | R-04 |
