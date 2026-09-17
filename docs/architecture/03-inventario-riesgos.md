# 03 · Inventario Inicial de Riesgos — NEVI

**Estado:** Semana 2 · Hito 4  
**Fecha:** 2026-09-11  
**Método:** Ciclo EDAV — los riesgos 1–5 fueron generados por IA y clasificados manualmente (ver `ai_audit.md`)

> **Regla del curso:** Un riesgo no es válido si no es cuantificable y trazable a un componente real del repositorio.

---

## Tabla de Riesgos

| ID | Riesgo | Componente afectado | Variable de estrés | Clasificación EDAV | Probabilidad | Impacto | Prioridad |
|---|---|---|---|---|---|---|---|
| R-01 | Agotamiento del pool JDBC bajo carga concurrente | `application.yml` (HikariCP por defecto, max 10) | Conexiones simultáneas al pool | ✅ Válido | Alta | Alto | 🔴 Crítico |
| R-02 | Extracción de JWT vía XSS | `useAuth.jsx` (`localStorage.setItem`) | Scripts maliciosos en el DOM | ✅ Válido | Media | Alto | 🔴 Crítico |
| R-03 | Corrupción de esquema de BD por `ddl-auto: update` | `application.yml` línea 17 | Renombrado/eliminación de campos en entidades JPA | ✅ Válido | Media | Alto | 🔴 Crítico |
| R-04 | Ausencia de pruebas automatizadas bloquea el Checkpoint | `backend/src/test/` (vacío) | Ninguna suite ejecutable en CI | ✅ Válido | Certeza | Alto | 🔴 Crítico |
| R-05 | CORS wildcard permite llamadas desde cualquier origen | `application.yml` línea 27 | Peticiones cross-origin maliciosas | ✅ Válido | Alta | Medio | 🟡 Alto |

---

## Detalle de Riesgos Críticos

### R-01 · Agotamiento del pool JDBC

**Descripción técnica:** HikariCP en Spring Boot usa 10 conexiones por defecto. El sistema mantiene conexiones WebSocket persistentes + peticiones REST. Si 15 usuarios del chat envían mensajes simultáneamente, el pool se agota y Spring lanza `SQLTransientConnectionException: Unable to acquire JDBC Connection`.

**Componente:** `backend/src/main/resources/application.yml`  
**Verificación:** `grep -n "maximum-pool-size" backend/src/main/resources/application.yml` → sin resultado (confirma que se usa el valor por defecto de 10).

**Mitigación propuesta:** Agregar `spring.datasource.hikari.maximum-pool-size: 25` en `application.yml` y medir con `ab -n 1000 -c 50 http://localhost:8080/api/mensajes`.

---

### R-02 · Extracción de JWT vía XSS

**Descripción técnica:** El token JWT se almacena en `localStorage` bajo la clave `nevi_token`. Un atacante que logre inyectar JavaScript en la página puede ejecutar `localStorage.getItem('nevi_token')` y robar la sesión sin que el usuario lo note.

**Componente:** `frontend/src/context/useAuth.jsx` (o `AuthContext.jsx`)  
**Verificación manual:** Abrir la consola del navegador en http://localhost:80 → ejecutar `localStorage.getItem('nevi_token')` → devuelve el token activo.

**Mitigación a largo plazo:** Migrar a `HttpOnly` cookies gestionadas por el backend (requiere refactorizar el flujo de autenticación completo — costo: > 1 día de ingeniería).  
**Mitigación a corto plazo en esta iteración:** Implementar Content Security Policy (CSP) en el `nginx.conf` para reducir la superficie de inyección.

---

### R-03 · Corrupción de esquema por `ddl-auto: update`

**Descripción técnica:** Si un campo de una entidad JPA es renombrado o eliminado, Hibernate intenta aplicar el cambio al arrancar el contenedor. En algunos casos esto destruye columnas con datos sin posibilidad de rollback.

**Componente:** `backend/src/main/resources/application.yml` línea 17  
**Verificación:** `grep -n "ddl-auto" backend/src/main/resources/application.yml` → `ddl-auto: update`.

**Mitigación:** Cambiar a `ddl-auto: validate` e introducir Flyway para gestionar migraciones explícitas.

---

### R-04 · Ausencia de pruebas — bloquea el Checkpoint

**Descripción técnica:** El Hito 5 exige que el sistema "pase todas sus pruebas unitarias e integración" como condición del Checkpoint. Actualmente no existe ninguna clase de prueba en el backend.

**Componente:** `backend/src/test/java/com/nevi/` (directorio vacío)  
**Verificación:** `find backend/src/test -name "*.java" | wc -l` → 0.

**Mitigación urgente (antes del Checkpoint):** Crear al menos 1 prueba de integración con `@SpringBootTest` que valide el endpoint `POST /api/auth/login` devuelve HTTP 200 con un token válido.

---

### R-05 · CORS wildcard

**Descripción técnica:** `allowed-origins: "*"` permite que cualquier dominio del mundo llame a la API REST de NEVI con las credenciales del usuario.

**Componente:** `backend/src/main/resources/application.yml` línea 27  
**Verificación:** `grep -n "allowed-origins" backend/src/main/resources/application.yml` → `allowed-origins: "*"`.

**Mitigación:** Restringir a `allowed-origins: "http://localhost:80"` en desarrollo y al dominio de producción real.

---

## Supuestos Declarados (no verificados aún)

| ID | Supuesto | Cómo verificar |
|---|---|---|
| S-01 | PostgreSQL no tiene índices en columnas de búsqueda frecuente (`user_id`, `grupo_id`) | `\d+ mensajes` en psql → verificar columnas indexadas |
| S-02 | El broker STOMP no tiene límite de suscripciones por usuario | Revisar `WebSocketConfig.java` y testear con 100 conexiones simultáneas |
| S-03 | La clave JWT hardcodeada en `application.yml` coincide con la de producción | `grep -n "secret" backend/src/main/resources/application.yml` |
