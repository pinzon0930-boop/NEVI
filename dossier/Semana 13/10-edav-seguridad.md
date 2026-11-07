# EDAV 3 — Escenario de Atributo de Calidad: Seguridad

**Proyecto:** NEVI — Plataforma de aprendizaje colaborativo con IA  
**Módulo:** 7 — Seguridad  
**Semana:** 13  
**Versión:** 1.0 — 2026-11-07

---

## 1. Contexto

Los Escenarios de Atributo de Calidad (EDAV) son el instrumento del SEI
(Software Engineering Institute) para especificar de manera verificable
los requisitos de calidad de un sistema. Este es el tercer escenario del
proyecto NEVI; los dos anteriores cubrieron rendimiento (EDAV-01) y
disponibilidad (EDAV-02). EDAV-03 cubre el atributo **seguridad**.

NEVI maneja datos sensibles: actividades educativas, mensajes de grupo,
conversaciones con la IA y tokens de autenticación. Un atacante que
obtenga el JWT de un usuario puede actuar en su nombre sin que el sistema
lo detecte hasta que el token expire.

---

## 2. Escenario formal

### EDAV-03-A — Acceso no autorizado mediante token JWT expuesto

| Campo | Descripción |
|-------|-------------|
| **ID** | EDAV-03-A |
| **Atributo de calidad** | Seguridad — Confidencialidad e integridad |
| **Fuente del estímulo** | Atacante externo sin cuenta válida en NEVI |
| **Estímulo** | El atacante captura un JWT de acceso desde el `localStorage` del navegador de la víctima (XSS o acceso físico al dispositivo) e intenta usarlo para acceder a los endpoints de la API (`/api/actividades`, `/api/mensajes`, `/api/grupos`) |
| **Entorno** | Sistema en operación normal (producción); el token robado aún está dentro de su ventana de validez de 15 minutos |
| **Artefacto** | Capa de seguridad: `JwtAuthFilter.java` + `JwtUtil.java` + Spring Security filter chain |
| **Respuesta** | El sistema valida la firma del token; si es válida acepta la petición. Si se detecta uso anómalo (misma IP con token diferente, o el token fue revocado a través del endpoint `/auth/logout`), el filtro rechaza con HTTP 401. Los logs registran el intento con `traceId`, IP de origen y endpoint objetivo. |
| **Medida de respuesta** | Tiempo máximo de detección de token revocado: < 100 ms. 0 peticiones exitosas con token revocado en base de datos. 100 % de intentos registrados en log con nivel WARN o superior. |

---

### EDAV-03-B — Fuga de secreto en artefacto público (build arg)

| Campo | Descripción |
|-------|-------------|
| **ID** | EDAV-03-B |
| **Atributo de calidad** | Seguridad — Confidencialidad |
| **Fuente del estímulo** | Proceso de CI/CD o desarrollador que construye la imagen Docker del frontend |
| **Estímulo** | Se pasa `GROQ_API_KEY` como argumento de build (`--build-arg`) al `Dockerfile` del frontend React/Vite; Vite sustituye `import.meta.env.VITE_GROQ_API_KEY` en tiempo de compilación, embebiendo la clave en el bundle JavaScript público |
| **Entorno** | Proceso de build en entorno de CI o local; cualquier usuario con acceso al sitio desplegado |
| **Artefacto** | `frontend/Dockerfile`, `docker-compose.yml`, bundle JS generado por Vite |
| **Respuesta esperada** | La clave **no** debe aparecer en el bundle público. Las llamadas a la API de Groq deben pasar por el backend (`/api/ia`), que actúa como proxy y posee la clave de forma exclusiva en variable de entorno del contenedor |
| **Respuesta actual** | ⚠️ La clave sí aparece en el bundle. Control pendiente documentado en `10-checklist-seguridad.md` como `CRED-02 ❌`. |
| **Medida de respuesta (objetivo)** | 0 ocurrencias de `VITE_GROQ_API_KEY` o `gsk_` en cualquier archivo bajo `dist/assets/` tras el build de producción. Verificable con: `grep -r "gsk_" frontend/dist/assets/` → sin salida. |

---

## 3. Relación con la arquitectura

```
                    ┌──────────────────────────────────────────────────┐
                    │  FRONTERA 1: Internet → Frontend                 │
  Atacante ─────────►  React SPA (Nginx)                              │
  (EDAV-03-B)       │  ⚠️  bundle JS contiene VITE_GROQ_API_KEY       │
                    └──────────────┬───────────────────────────────────┘
                                   │ HTTPS
                    ┌──────────────▼───────────────────────────────────┐
                    │  FRONTERA 2: Frontend → Backend (nevi_net)       │
  Atacante ─────────►  Spring Boot — JwtAuthFilter                    │
  (EDAV-03-A)       │  ✅ valida firma + chequea revocación            │
                    └──────────────┬───────────────────────────────────┘
                                   │ JDBC
                    ┌──────────────▼───────────────────────────────────┐
                    │  PostgreSQL 16 — tabla refresh_tokens            │
                    │  (token_hash, expires_at, revoked)               │
                    └──────────────────────────────────────────────────┘
```

---

## 4. Controles existentes y estado

| Control | Escenario | Estado | Archivo |
|---------|-----------|--------|---------|
| JWT access token TTL = 15 min | EDAV-03-A | ✅ Implementado | `JwtUtil.java` |
| Refresh token almacenado en PostgreSQL con `revoked` flag | EDAV-03-A | ✅ Diseñado (ADR-005) | `adr-005-jwt-refresh-tokens.md` |
| Revocación inmediata en logout | EDAV-03-A | ⚠️ Pendiente implementación | `10-checklist-seguridad.md` AUTH-04 |
| Proxy IA en backend (Groq call desde servidor) | EDAV-03-B | ⚠️ Pendiente | `10-checklist-seguridad.md` CRED-02 |
| Variable `VITE_GROQ_API_KEY` eliminada de build args | EDAV-03-B | ❌ No implementado | `10-checklist-seguridad.md` CRED-02 |

---

## 5. Criterio de verificación del EDAV

El escenario EDAV-03-A se considera **acreditado** cuando:

1. `mvn test -Dtest=NeviSecurityArchitectureTest` pasa sin errores (las 4 reglas ArchUnit).
2. Una petición con token revocado (marcado `revoked = true` en BD) recibe HTTP 401 en < 100 ms.
3. El log muestra `level=WARN event=JWT_REVOKED traceId=<id> ip=<ip>`.

El escenario EDAV-03-B se considera **acreditado** cuando:

1. `grep -r "gsk_" frontend/dist/assets/` no devuelve ninguna línea tras un build limpio.
2. Las peticiones a Groq se originan desde el backend, no desde el browser (verificable en Network tab del DevTools).

**Estado actual:** EDAV-03-A parcialmente acreditado (filtro JWT activo, revocación pendiente). EDAV-03-B no acreditado (control pendiente).

---

## 6. Referencia cruzada

- Riesgos relacionados: R03 (Exposición de JWT), R05 (Fuga de API key) en `10-seguridad.md`
- Amenaza STRIDE: Información Disclosure S2 en `10-modelado-amenazas.md`
- DREAD score: IDOR 13/15 en `10-priorizacion-amenazas.md`
- Control pendiente: CRED-02, AUTH-04 en `10-checklist-seguridad.md`
- Decisión arquitectónica: ADR-005 en `../Semana 14/adr-005-jwt-refresh-tokens.md`
