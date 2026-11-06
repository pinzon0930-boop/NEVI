# Checklist de Seguridad de Ingeniería — NEVI

**Proyecto:** NEVI — Network of Virtual Interaction  
**Semana:** 13 · Corte 3  
**Tiempo estimado:** 60 min  
**Autor:** Nixson Camilo Pinzón Muñoz  

---

## ¿Para qué sirve este checklist?

Este checklist está basado en el estándar de ingeniería de seguridad del OWASP Top Ten y en los controles que definimos en el modelado de amenazas. La idea es revisar el sistema completo de NEVI — no solo el backend, sino también el frontend, la base de datos, el pipeline y la configuración de despliegue.

Cada ítem tiene tres estados posibles:
- ✅ **Hecho** — el control está implementado y verificado
- ❌ **Pendiente** — no está implementado, hay que trabajarlo
- ⚠️ **Parcial** — hay algo hecho pero no está completo o no fue verificado

---

## Área 1: Autenticación y JWT

| Estado | Control | Notas |
|---|---|---|
| ❌ | Los access tokens JWT tienen expiración ≤ 15 minutos | El default de Spring Boot es más largo; nunca se ajustó |
| ✅ | `${JWT_SECRET}` está en variable de entorno y no está en el código | Configurado en `.env` desde el inicio del proyecto |
| ✅ | `JwtAuthFilter` valida firma, expiración e issuer en cada request | El sistema de autenticación funciona correctamente |
| ❌ | Hay refresh tokens con rotación implementados | No se construyó este mecanismo |
| ❌ | El logout invalida el refresh token (blacklist o expiración forzada) | No hay blacklist de tokens |

**Acciones pendientes:**
- Ajustar `spring.security.jwt.expiration` a 900000 ms (15 min) en `application.yml`
- Implementar endpoint de logout que invalide el refresh token

---

## Área 2: Autorización y control de acceso

| Estado | Control | Notas |
|---|---|---|
| ❌ | Todos los endpoints admin tienen `@PreAuthorize("hasRole('ADMIN')")` | No verificado; puede faltar en algunos endpoints |
| ❌ | Hay verificación de ownership antes de UPDATE/GET sobre recursos | El `groupId` y `submissionId` no se validan contra el usuario del JWT |
| ❌ | Existe prueba negativa: STUDENT → endpoint ADMIN → 403 | No existe este test en el proyecto |
| ❌ | Los IDs de recursos son UUIDs (no enteros secuenciales) | Los IDs secuenciales facilitan IDOR |

**Acciones pendientes:**
- Revisar todos los controllers y agregar `@PreAuthorize` donde falte
- Agregar verificación de ownership en los servicios antes de cualquier operación sobre grupos o entregas
- Escribir al menos 1 prueba negativa de RBAC en JUnit

---

## Área 3: Gestión de secretos

| Estado | Control | Notas |
|---|---|---|
| ✅ | El archivo `.env` está en `.gitignore` | Configurado desde el inicio |
| ✅ | `DB_PASSWORD`, `JWT_SECRET`, `VITE_GROQ_API_KEY` solo están en variables de entorno | Verificado en sesiones anteriores |
| ❌ | El pipeline CI tiene un scan que detecta credenciales hardcodeadas | El pipeline de CI todavía no existe |
| ⚠️ | Se verificó que el historial de Git no tiene secretos commiteados | No se hizo una revisión del historial completo |

**Acciones pendientes:**
- Revisar el historial de Git con `git log --all --full-diff -p -- .env` para confirmar que el `.env` nunca fue commiteado
- Agregar Semgrep con regla de credenciales al pipeline CI (Semana 14)

---

## Área 4: Logging y trazabilidad

| Estado | Control | Notas |
|---|---|---|
| ❌ | Cada request genera un Trace ID (UUID) propagado en todos los logs | No está implementado en NEVI |
| ❌ | Los logs están en formato JSON estructurado | Probablemente formato texto plano por defecto |
| ❌ | El nivel de log en producción es INFO o superior (no DEBUG) | No se revisó la configuración de producción |
| ❌ | No hay serialización de objetos completos en logs | Historia H10 mostró que esto pasa en sistemas similares |
| ❌ | Los campos `passwordHash`, tokens y datos personales están excluidos de los logs | No hay filtro de redacción configurado |

**Acciones pendientes:**
- Agregar un filtro en Spring Boot que genere un UUID por request y lo agregue al MDC (Mapped Diagnostic Context)
- Configurar Logback para formato JSON (librería `logstash-logback-encoder`)
- Revisar todos los `log.info()`/`log.debug()` que serialicen objetos de usuario

---

## Área 5: Base de datos

| Estado | Control | Notas |
|---|---|---|
| ❌ | El rol `nevi_app` tiene solo SELECT, INSERT, UPDATE en PostgreSQL | Nunca se creó este rol específico |
| ✅ | Todas las consultas usan JPA/Hibernate (sin SQL nativo concatenado) | Hibernate usa prepared statements por defecto |
| ✅ | Las credenciales de BD no están en `docker-compose.yml` (usan variables de entorno) | Configurado correctamente |
| ⚠️ | HikariCP está configurado con límites para evitar saturar la BD | Probablemente usa valores por defecto; no fue ajustado |

**Acciones pendientes:**
- Crear el rol `nevi_app` en PostgreSQL con permisos mínimos:
```sql
CREATE ROLE nevi_app LOGIN PASSWORD '${DB_PASSWORD}';
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO nevi_app;
```
- Revisar y ajustar la configuración de HikariCP en `application.yml`

---

## Área 6: Configuración y despliegue

| Estado | Control | Notas |
|---|---|---|
| ⚠️ | Nginx tiene HTTPS configurado | Solo en desarrollo con certificado autofirmado; no en producción real |
| ❌ | Los puertos de PostgreSQL no están expuestos al exterior en `docker-compose.yml` | Hay que verificarlo — PostgreSQL no debería tener puerto mapeado al host |
| ❌ | El pipeline CI tiene al menos un job de SAST (análisis estático) | No existe el pipeline todavía |
| ❌ | Las imágenes Docker usan versiones fijas (no `latest`) | Usar `latest` puede introducir cambios inesperados |

**Acciones pendientes:**
- Revisar `docker-compose.yml` y quitar el mapeo de puerto de PostgreSQL (`5432:5432`) si está expuesto
- El pipeline CI se trabajará en Semana 14

---

## Resumen de estado actual

| Área | ✅ Hecho | ❌ Pendiente | ⚠️ Parcial |
|---|---|---|---|
| Autenticación y JWT | 2 | 3 | 0 |
| Autorización y acceso | 0 | 4 | 0 |
| Gestión de secretos | 2 | 1 | 1 |
| Logging y trazabilidad | 0 | 5 | 0 |
| Base de datos | 2 | 1 | 1 |
| Configuración y despliegue | 0 | 3 | 1 |
| **Total** | **6** | **17** | **3** |

La mayoría de los ítems pendientes están relacionados con logging, autorización y el pipeline CI — todos se trabajan en Semana 14.

---

*Documento de Semana 13 — Corte 3*  
*Universidad Jorge Tadeo Lozano — Arquitectura de Software*
