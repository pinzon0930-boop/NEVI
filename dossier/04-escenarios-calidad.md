# 04 — Escenarios de calidad · NOVI

Los escenarios siguen la plantilla del curso:
**Estímulo → Fuente → Artefacto → Entorno → Respuesta → Medida de respuesta**

Cada escenario está vinculado a un atributo de calidad (`QA-XX`) y a un
driver arquitectónico (`DRIVER-XX`).

---

## ESC-01 · Disponibilidad — Carga de 10 usuarios concurrentes

| Campo | Detalle |
|---|---|
| **Atributo** | QA-01 Disponibilidad |
| **Driver** | DRIVER-03 |
| **Estímulo** | 10 usuarios envían solicitudes `GET /api/grupos` de forma concurrente |
| **Fuente** | Herramienta de carga k6 ejecutada en la máquina de desarrollo |
| **Artefacto** | API REST — `GrupoController.java` → `GrupoService.java` → PostgreSQL |
| **Entorno** | Sistema en ejecución local con Docker Compose, semilla de datos cargada |
| **Respuesta** | El sistema responde HTTP 200 con la lista de grupos del usuario |
| **Medida de respuesta** | Tasa de éxito ≥ 95 % durante 60 s; sin errores HTTP 500 |
| **Estado actual** | ⚠️ En riesgo — R-01 (pool de 10 conexiones puede agotarse) |

---

## ESC-02 · Rendimiento — Latencia p95 en listar grupos

| Campo | Detalle |
|---|---|
| **Atributo** | QA-02 Rendimiento |
| **Driver** | DRIVER-04 |
| **Estímulo** | 10 VUs envían `GET /api/grupos` durante 60 s con token JWT válido |
| **Fuente** | Script k6 con autenticación previa |
| **Artefacto** | Cadena: `GrupoController` → `GrupoService` → `GrupoRepository` → PostgreSQL |
| **Entorno** | Docker Compose local; semilla: 5 grupos por usuario, 3 miembros por grupo |
| **Respuesta** | El endpoint devuelve la lista de grupos en JSON |
| **Medida de respuesta** | Latencia p95 ≤ 800 ms; latencia mediana ≤ 400 ms |
| **Hipótesis** | Se espera p95 entre 300 ms y 600 ms con la configuración base |
| **Estado actual** | 🔴 Sin medir — pendiente corrida de baseline |

---

## ESC-03 · Seguridad — Acceso sin token JWT

| Campo | Detalle |
|---|---|
| **Atributo** | QA-03 Seguridad |
| **Driver** | DRIVER-01 |
| **Estímulo** | Un cliente envía `GET /api/grupos` sin encabezado `Authorization` |
| **Fuente** | Atacante externo o solicitud malformada |
| **Artefacto** | `JwtAuthFilter.java` + `SecurityConfig.java` |
| **Entorno** | Sistema en producción o local con `docker-compose up` |
| **Respuesta** | El sistema rechaza la solicitud |
| **Medida de respuesta** | HTTP 401 Unauthorized devuelto en < 50 ms; sin datos expuestos en el cuerpo |
| **Estado actual** | ✅ Verificado manualmente — `SecurityConfig` protege rutas `/api/**` |

---

## ESC-04 · Modificabilidad — Agregar nuevo endpoint

| Campo | Detalle |
|---|---|
| **Atributo** | QA-04 Modificabilidad |
| **Driver** | DRIVER-02 |
| **Estímulo** | Un desarrollador agrega un nuevo endpoint `GET /api/actividades/{id}/estadisticas` |
| **Fuente** | Integrante del equipo de desarrollo |
| **Artefacto** | `ActividadController.java`, `ActividadService.java`, `ActividadRepository.java` |
| **Entorno** | Rama de feature con PR hacia `main` |
| **Respuesta** | El nuevo endpoint funciona sin cambios en el frontend ni en `SecurityConfig` |
| **Medida de respuesta** | El cambio toca ≤ 3 archivos; el PR no incluye modificaciones en `src/` del frontend |
| **Estado actual** | ✅ Arquitectura MVC de Spring Boot lo permite por diseño |

---

## ESC-05 · Testeabilidad — Detección de regresión en login

| Campo | Detalle |
|---|---|
| **Atributo** | QA-05 Testeabilidad |
| **Driver** | DRIVER-05 |
| **Estímulo** | Un cambio en `AuthService.java` introduce un bug en la generación del token JWT |
| **Fuente** | Desarrollador — commit hacia rama de feature |
| **Artefacto** | `AuthController.java` → `AuthService.java` → `JwtUtil.java` |
| **Entorno** | Pipeline de CI (GitHub Actions) ejecutando `./mvnw test` |
| **Respuesta** | El pipeline reporta fallo en la prueba de integración antes de que el PR se fusione |
| **Medida de respuesta** | El CI detecta el fallo en < 5 min; el PR no puede fusionarse con CI rojo |
| **Estado actual** | ✅ Implementado — ver `backend/src/test/java/com/nevi/AuthFlowIntegrationTest.java`; el CI ejecuta el flujo registro→login→acceso protegido |

---

## Trazabilidad escenarios ↔ atributos

| Escenario | Atributo | Driver | Medible ahora |
|---|---|---|---|
| ESC-01 | QA-01 Disponibilidad | DRIVER-03 | Con k6 |
| ESC-02 | QA-02 Rendimiento | DRIVER-04 | Con k6 (pendiente) |
| ESC-03 | QA-03 Seguridad | DRIVER-01 | ✅ Verificado manualmente |
| ESC-04 | QA-04 Modificabilidad | DRIVER-02 | ✅ Por diseño |
| ESC-05 | QA-05 Testeabilidad | DRIVER-05 | ✅ Implementado (AuthFlowIntegrationTest) |
