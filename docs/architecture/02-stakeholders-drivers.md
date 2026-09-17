# 02 · Stakeholders, Restricciones y Drivers Preliminares — NEVI

**Estado:** Semana 1 · Hito 3 y 4  
**Fecha:** 2026-09-11

---

## 1. Mapa de Stakeholders

| Stakeholder | Rol en el sistema | Preocupación principal | Atributo de calidad prioritario |
|---|---|---|---|
| **Estudiante** | Usuario final — consume actividades, chat y quizzes | El sistema debe responder rápido y no perder mensajes del chat | Rendimiento, Disponibilidad |
| **Profesor / Docente** | Crea grupos, actividades, quizzes y rúbricas con IA | Herramienta confiable para planear y ejecutar su clase | Funcionalidad, Usabilidad |
| **Administrador del sistema** | Gestiona usuarios, roles y configuración global | Acceso controlado; ningún usuario accede a datos ajenos | Seguridad, Mantenibilidad |
| **Equipo de desarrollo** | Mantiene y evoluciona el sistema | Poder modificar componentes sin romper otros; deuda técnica controlada | Modificabilidad, Testeabilidad |
| **Docente del curso 702302** | Evalúa la arquitectura del sistema | El sistema levanta en dos comandos; las decisiones están documentadas y justificadas con evidencia | Verificabilidad, Trazabilidad |
| **Proveedor Groq (externo)** | Provee el LLM para funciones de IA de NEVI | — | Disponibilidad del proveedor, Latencia de red |

---

## 2. Restricciones Insalvables

> Una restricción limita de manera absoluta el espacio de alternativas. El arquitecto diseña **a pesar de ellas**, no las ignora.

### 2.1 Restricciones Técnicas

| ID | Restricción | Origen | Impacto arquitectónico |
|---|---|---|---|
| RT-01 | Stack heredado: React 18 + Spring Boot 3.3.4 + PostgreSQL 16 | Código legacy de asignatura predecesora | Todo diseño debe asumir JVM, Servlet API y SQL relacional — no se puede migrar a NoSQL ni a otro runtime |
| RT-02 | JWT almacenado en `localStorage` | Implementado en `useAuth.jsx` — decisión previa | Superficie de ataque XSS permanente en esta iteración; no se puede mover a `HttpOnly` cookie sin refactorizar el contexto de autenticación completo |
| RT-03 | `ddl-auto: update` en Hibernate | `application.yml` línea 17 | Sin Flyway ni Liquibase; un refactor de entidad puede destruir datos al arrancar — no hay rollback automático del esquema |
| RT-04 | Pool JDBC HikariCP por defecto (máx. 10 conexiones) | Spring Boot auto-config | Bajo carga concurrente con WebSocket activo, el pool se agota antes de saturar la CPU |
| RT-05 | CORS `allowed-origins: "*"` | `application.yml` línea 27 | Cualquier origen puede llamar la API REST — inaceptable en producción; limita el hardening de seguridad HTTP |
| RT-06 | WebSocket/STOMP declarado (sin límite de conexiones) | `WebSocketConfig.java` | El broker gestiona conexiones en memoria sin límite configurado — punto de quiebre bajo carga |

> **Verificación RT-03:** `grep -n "ddl-auto" backend/src/main/resources/application.yml` → línea 17: `ddl-auto: update`  
> **Verificación RT-05:** `grep -n "allowed-origins" backend/src/main/resources/application.yml` → línea 27: `allowed-origins: "*"`

### 2.2 Restricciones Económicas

| ID | Restricción | Impacto |
|---|---|---|
| RE-01 | API Groq en free tier: límite ~14,400 tokens/min | Las funciones de IA (quiz, rúbrica, retroalimentación) fallan con HTTP 429 bajo uso intensivo simultáneo |
| RE-02 | Infraestructura en máquina local del desarrollador | Sin presupuesto de nube — no hay réplicas, balanceadores ni almacenamiento externo |

### 2.3 Restricciones Organizacionales

| ID | Restricción | Impacto |
|---|---|---|
| RO-01 | Equipo de 1 integrante (o equipo pequeño) | Sin revisión interna de pares para decisiones arquitectónicas; toda la deuda técnica recae en una persona |
| RO-02 | Ventana de 2 semanas (12 h independientes) | No hay tiempo para rediseños estructurales — solo análisis, documentación y mitigaciones puntuales |
| RO-03 | Sin CI/CD pipeline configurado | El Checkpoint de Semana 2 no puede verificarse automáticamente; la ejecución de pruebas es manual |

---

## 3. Drivers Arquitectónicos Preliminares

> Un driver arquitectónico es la fuerza prioritaria que guía la estructura del sistema: destilación de metas de negocio + atributos de calidad críticos + riesgos. **No es prosa decorativa — cada driver cita el archivo real que lo evidencia.**

### DRIVER-01 · Seguridad — Protección del token JWT
**Prioridad:** 🔴 Alta (innegociable)  
**Stakeholders:** Todos los usuarios, Administrador  
**Archivo afectado:** `frontend/src/context/AuthContext.jsx` (o `useAuth.jsx`)  
**Evidencia:** `localStorage.setItem('nevi_token', token)` — el token vive en localStorage, accesible por cualquier script JavaScript en la página.  
**Trade-off en tensión:** Seguridad vs. Simplicidad de implementación  
**Métrica de aceptación:** Cero tokens accesibles vía `document.cookie` XSS en una prueba manual con `<script>alert(localStorage.getItem('nevi_token'))</script>`  
**Estado actual:** ⚠️ Riesgo activo — sin mitigación implementada

---

### DRIVER-02 · Modificabilidad — Control de versión del esquema de BD
**Prioridad:** 🔴 Alta  
**Stakeholders:** Equipo de desarrollo, DBA  
**Archivo afectado:** `backend/src/main/resources/application.yml` línea 17  
**Evidencia:** `ddl-auto: update` → Hibernate aplica cambios de esquema automáticamente en cada arranque del contenedor.  
**Trade-off en tensión:** Agilidad de desarrollo (no hay que escribir migraciones) vs. Seguridad del dato en producción (una migración mal formulada puede eliminar columnas)  
**Métrica de aceptación:** Tiempo de recuperación ante migración fallida — hoy: indefinido (no hay rollback documentado)  
**Estado actual:** ⚠️ Riesgo activo — no hay Flyway ni Liquibase

---

### DRIVER-03 · Disponibilidad — Sin health check ni reinicio automático
**Prioridad:** 🟡 Media  
**Stakeholders:** Estudiantes, Profesores  
**Archivo afectado:** `docker-compose.yml` (ausencia de clave `healthcheck` en todos los servicios)  
**Evidencia:** `grep -n "healthcheck" docker-compose.yml` → sin resultados. Un fallo en PostgreSQL no dispara reinicio del backend.  
**Trade-off en tensión:** Disponibilidad vs. Complejidad de configuración del Compose  
**Métrica de aceptación:** MTTR (tiempo de recuperación) ante fallo de BD — hoy: manual, sin SLA definido  
**Estado actual:** ⚠️ Sin mecanismo de auto-recuperación

---

### DRIVER-04 · Rendimiento — Agotamiento del pool JDBC bajo carga concurrente
**Prioridad:** 🟡 Media  
**Stakeholders:** Estudiantes (usuarios del chat en tiempo real)  
**Archivo afectado:** `backend/src/main/resources/application.yml` (ausencia de `spring.datasource.hikari.maximum-pool-size`)  
**Evidencia:** Pool por defecto de HikariCP en Spring Boot = 10 conexiones máximas. Con múltiples sesiones WebSocket activas + peticiones REST paralelas, el pool se agota → `SQLTransientConnectionException`.  
**Trade-off en tensión:** Rendimiento bajo carga vs. Costo de conexiones abiertas a PostgreSQL  
**Métrica de aceptación:** Latencia P95 del endpoint `POST /api/mensajes` bajo 50 usuarios concurrentes < 500 ms — **no medido aún**  
**Estado actual:** 🔴 No instrumentado

---

### DRIVER-05 · Testeabilidad — Ausencia de suite de pruebas automatizadas
**Prioridad:** 🔴 Crítica para el Checkpoint Hito 5  
**Stakeholders:** Docente del curso 702302 (evaluador), Equipo de desarrollo  
**Archivo afectado:** `backend/src/test/` (directorio vacío o inexistente)  
**Evidencia:** `find backend/src/test -name "*.java" | wc -l` → 0 archivos de prueba.  
**Trade-off en tensión:** Velocidad de entrega inicial vs. Confianza en regresiones y certificación del Checkpoint  
**Métrica de aceptación:** Al menos 1 prueba de integración que valide el flujo principal (autenticación + creación de mensaje) con resultado GREEN en CI  
**Estado actual:** 🔴 Cobertura 0% — bloquea el Checkpoint de Semana 2
