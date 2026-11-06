# 04 — Escenarios de calidad · NEVI

Los escenarios siguen la plantilla del curso:
**Estímulo → Fuente → Artefacto → Entorno → Respuesta → Medida de respuesta**

Cada escenario está vinculado a un atributo de calidad (`QA-XX`) y a un
driver arquitectónico (`DRIVER-XX`), y registra las mediciones de línea base
que sirven como antecedente directo para el **Experimento de Crecimiento,
Resiliencia y Costo del Módulo 6 (Semanas 11 y 12)**.

---

## ESC-01 · Disponibilidad — Carga de 10 usuarios concurrentes

| Campo | Detalle |
|---|---|
| **Atributo** | QA-01 Disponibilidad |
| **Driver** | DRIVER-03 |
| **Estímulo** | 10 usuarios envían solicitudes `GET /api/grupos` de forma concurrente |
| **Fuente** | Herramienta de carga k6 ejecutada en la máquina de desarrollo |
| **Artefacto** | API REST — `GrupoController.java` → `GrupoService.java` → PostgreSQL |
| **Entorno** | Sistema en ejecución local con Docker Compose, semilla de datos cargada (distribución 80/20) |
| **Respuesta** | El sistema responde HTTP 200 con la lista de grupos del usuario |
| **Medida de respuesta** | Tasa de éxito ≥ 95 % durante 60 s; sin errores HTTP 500 |
| **Medición Línea Base (10 VUs)** | **Tasa de éxito: 100 % (0 % errores HTTP 500)** con 10 VUs durante 60 s. Sin embargo, al escalar la prueba en escalones hacia **100–150 VUs** (antecedente Módulo 6), se confirma el riesgo **R-01**: el pool por defecto de HikariCP (`maximum-pool-size = 10`) se satura (*Pool Starvation*), generando una tasa de error del **2.6 %** por timeouts de conexión JDBC. |
| **Estado actual** |  Medido en Línea Base (10 VUs cumple ≥ 95 %) /  En riesgo bajo estrés (>100 VUs activa **R-01**, mitigado en Semana 11) |

---

## ESC-02 · Rendimiento — Latencia p95 en listar grupos

| Campo | Detalle |
|---|---|
| **Atributo** | QA-02 Rendimiento |
| **Driver** | DRIVER-04 |
| **Estímulo** | 10 VUs envían `GET /api/grupos` durante 60 s con token JWT válido |
| **Fuente** | Script k6 con autenticación previa |
| **Artefacto** | Cadena: `GrupoController` → `GrupoService` → `GrupoRepository` → PostgreSQL |
| **Entorno** | Docker Compose local; semilla: 5 grupos por usuario, 3 miembros por grupo (y semilla sintética 80/20) |
| **Respuesta** | El endpoint devuelve la lista de grupos en JSON |
| **Medida de respuesta** | Latencia p95 ≤ 800 ms (meta base Semana 4) / Latencia p95 < 300 ms (meta estricta Módulo 6); latencia mediana ≤ 400 ms |
| **Hipótesis** | Se espera p95 entre 300 ms y 600 ms con la configuración base |
| **Medición Línea Base (Baseline)** | • **Con 10 VUs (60 s):** Latencia mediana (`p50`) = **145 ms**; Latencia **`p95` = 385 ms**; Throughput = **31.4 req/s** (confirma la hipótesis inicial de 300–600 ms).<br>• **Antecedente de estrés (100 VUs sin táctica):** Latencia **`p95` sube a 760 ms** (`p99` = 1,180 ms) por encolamiento en el pool de 10 conexiones de HikariCP y consultas JPA sin caché. |
| **Estado actual** |  Baseline medido con k6 — sirve de antecedente cuantitativo para el experimento de la Semana 11 |

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
| **Estado actual** |  Verificado manualmente — `SecurityConfig` protege rutas `/api/**` (tiempo de rechazo medido: ~8 ms) |

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
| **Estado actual** |  Arquitectura MVC de Spring Boot lo permite por diseño |

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
| **Estado actual** |  Implementado — ver `backend/src/test/java/com/nevi/AuthFlowIntegrationTest.java`; el CI ejecuta el flujo registro→login→acceso protegido |

---

## ESC-06 · Escalabilidad y Resiliencia — Crecimiento en escalones discretos (Módulo 6)

| Campo | Detalle |
|---|---|
| **Atributo** | QA-01 Disponibilidad / QA-02 Rendimiento y Escalabilidad |
| **Driver** | DRIVER-03 y DRIVER-04 (Mitigación del riesgo **R-01**) |
| **Estímulo** | Demanda creciente en escalones discretos (**50 → 100 → 150 → 200 VUs**) enviando `GET /api/grupos` con token JWT válido |
| **Fuente** | Script de experimento de crecimiento en `k6` con validaciones de estado HTTP 200 y cuerpo JSON no vacío (evitando *falsos verdes*) |
| **Artefacto** | Trayecto completo `JwtAuthFilter` → `GrupoController` → `GrupoService` → `GrupoRepository` (`HikariCP`) → PostgreSQL 16 |
| **Entorno** | Docker Compose local en un único host físico; semilla sintética con distribución 80/20 |
| **Respuesta** | El sistema absorbe los picos de lectura sin agotar el pool de conexiones ni sufrir fallo en cascada, manteniendo los SLOs protegidos por una *Fitness Function* en CI |
| **Medida de respuesta** | **Latencia p95 < 300 ms**, **Tasa de error < 1.0 %** y **Disponibilidad ≥ 99.5 %** hasta el escalón de 100–150 VUs |
| **Estado actual** |  Medido y validado en Semana 11 (con táctica combinada: Pool HikariCP a 25 conexiones + Timeout Fail-Fast 3s + optimización JPA `JOIN FETCH` / Caché local) |

---

## Registro de Mediciones de Línea Base (Antecedente para Semana 11 y ADR 4)

Esta tabla consolida las mediciones del sistema base (**sin tácticas de escalabilidad aplicadas**, con `HikariCP = 10 conexiones` por defecto) que sirven como punto de comparación (*Baseline*) para el experimento del Módulo 6:

| Escalón de Carga (`GET /api/grupos`) | Usuarios Concurrentes (VUs) | Latencia Mediana (`p50`) | Latencia `p95` (Base) | Latencia `p99` (Base) | Throughput (`req/s`) | Tasa de Error HTTP | Diagnóstico Arquitectónico Observado |
|---|---:|---:|---:|---:|---:|---:|---|
| **Baseline Inicial (ESC-01 / ESC-02)** | `10 VUs` | `145 ms` | **`385 ms`** | `510 ms` | `31.4 req/s` | `0.0 %` | Operación saludable; el pool de 10 conexiones alcanza exactamente para los 10 VUs concurrentes. |
| **Escalón 1 (Carga controlada)** | `50 VUs` | `210 ms` | **`490 ms`** | `680 ms` | `84.0 req/s` | `0.0 %` | Inicia contención en HikariCP; supera el umbral estricto de `300 ms` aunque cumple el umbral inicial de `800 ms`. |
| **Escalón 2 (Presión intermedia)** | `100 VUs` | `420 ms` | **`760 ms`** | `1,180 ms` | `118.0 req/s` | **`2.6 %`** | **Punto de degradación (R-01 confirmado):** *Pool Starvation* en HikariCP (10 conexiones) y sobrecarga por consultas repetidas a PostgreSQL. |
| **Escalón 3 (Punto de quiebre base)** | `150 VUs` | `890 ms` | **`1,640 ms`** | `2,850 ms` | `109.0 req/s` | **`11.4 %`** | **Punto de quiebre del sistema base:** El throughput retrocede y los hilos HTTP quedan bloqueados esperando conexiones JDBC. |

---

## Trazabilidad escenarios ↔ atributos

| Escenario | Atributo | Driver | Medible ahora / Evidencia |
|---|---|---|---|
| ESC-01 | QA-01 Disponibilidad | DRIVER-03 |  Medido con k6 (Baseline 10 VUs: 100 % éxito; a 100 VUs se manifiesta R-01 con 2.6 % error) |
| ESC-02 | QA-02 Rendimiento | DRIVER-04 |  Medido con k6 (Baseline 10 VUs: `p95 = 385 ms`; a 100 VUs sin táctica: `p95 = 760 ms`) |
| ESC-03 | QA-03 Seguridad | DRIVER-01 |  Verificado manualmente (`JwtAuthFilter.java` + `SecurityConfig.java` → HTTP 401 en < 50 ms) |
| ESC-04 | QA-04 Modificabilidad | DRIVER-02 |  Por diseño (`ActividadController`, `ActividadService`, `ActividadRepository`) |
| ESC-05 | QA-05 Testeabilidad | DRIVER-05 |  Implementado (`backend/src/test/java/com/nevi/AuthFlowIntegrationTest.java` en CI) |
| ESC-06 | QA-01 / QA-02 Escalabilidad y Resiliencia | DRIVER-03 / DRIVER-04 |  Medido en Semana 11 (Escalones 50–200 VUs + Fitness Function en CI + ADR 4) |
