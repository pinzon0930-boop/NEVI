# Seguridad — Semana 13 (PR Index)

**Proyecto:** NEVI — Network of Virtual Interaction  
**Semana:** 13 · Corte 3  
**Tiempo estimado:** 45 min  
**Autor:** Nixson Camilo Pinzón Muñoz  

---

## ¿Qué hay en este PR?

Este PR cubre el trabajo de seguridad de la Semana 13. No son correcciones de código todavía — son los documentos de análisis que justifican qué hay que arreglar y por qué, que se implementarán en la Semana 14.

Los 4 documentos que forman este PR son:

| Archivo | Qué contiene | Tiempo |
|---|---|---|
| [`10-modelado-amenazas.md`](./10-modelado-amenazas.md) | STRIDE completo aplicado a NEVI + OWASP Top Ten | 120 min |
| [`10-checklist-seguridad.md`](./10-checklist-seguridad.md) | Estado real de cada control de seguridad en el sistema | 60 min |
| [`10-controles-confianza.md`](./10-controles-confianza.md) | Límites de confianza Zero Trust + controles por frontera | 90 min |
| [`10-priorizacion-amenazas.md`](./10-priorizacion-amenazas.md) | Amenazas priorizadas con DREAD + genéricas de IA descartadas | 45 min |

---

## Estado actual de seguridad en NEVI

### Lo que está bien

- El `JWT_SECRET` y las credenciales de BD están en variables de entorno (`.env`)
- El `.env` está en `.gitignore` — nunca commiteado
- JPA/Hibernate usa prepared statements por defecto — sin riesgo de SQL injection
- `JwtAuthFilter` valida firma, expiración e issuer en cada request

### Lo que hay que trabajar (Semana 14)

**Alta prioridad:**
- Ownership check antes de cualquier operación sobre grupos y entregas — sin esto hay IDOR
- JWT con expiración ≤15 minutos — el default actual es demasiado largo
- Trace ID en todos los logs — sin esto no se pueden investigar incidentes
- `@PreAuthorize("hasRole('ADMIN')")` verificado en todos los endpoints admin

**Media prioridad:**
- Rol `nevi_app` en PostgreSQL con solo SELECT/INSERT/UPDATE
- Logs en formato JSON estructurado
- Filtro de redacción para `passwordHash` y tokens en logs
- Pipeline CI con SAST (Semgrep)

**Verificar ahora:**
- Que PostgreSQL no tenga el puerto 5432 mapeado al host en `docker-compose.yml`
- Revisar historial de Git para confirmar que `.env` nunca fue commiteado

---

## El modelo de confianza que aplicamos

Usamos el modelo **Zero Trust / Atlas Airport**:

> "Atravesar una frontera no concede automáticamente permiso para atravesar las demás."

Esto significa que en NEVI:
- Estar en `nevi_net` (red interna) no es suficiente para confiar en un request
- Un JWT válido (autenticación) ≠ permiso para ese recurso específico (autorización)
- Cada frontera (Nginx, Spring Boot, PostgreSQL, Groq) verifica por su cuenta

Los 4 límites de confianza identificados:
1. **Internet → Nginx**: TLS + rate limiting
2. **Nginx → Spring Boot**: JWT verificado en cada request
3. **Spring Boot → PostgreSQL**: rol con permisos mínimos + credenciales en env vars
4. **Spring Boot → Groq API**: API key en env var + circuit breaker (pendiente)

---

## Amenazas descartadas

Del análisis con IA, descartamos las siguientes amenazas genéricas porque no aplican a la arquitectura de NEVI:

- **Prompt Injection**: Los usuarios no controlan el prompt que va a Groq — el backend lo construye
- **Model Poisoning**: NEVI no entrena ni hace fine-tuning de ningún modelo
- **Training Data Leakage**: Los datos de NEVI no se usan para entrenar a Groq
- **Jailbreaking del modelo**: El modelo no tiene acceso a datos ni puede ejecutar acciones en el sistema

---

## Conexión con el resto del dossier

Este análisis de seguridad se conecta con otros documentos del dossier:

- El **ADR 5** (Semana 14) documentará la decisión formal sobre el mecanismo de JWT y refresh tokens
- La **fitness function de CI** (Semana 14) incluirá una prueba que falle si un endpoint admin no tiene `@PreAuthorize`
- La **matriz de riesgos operativos** (Semana 14) usará la priorización de este documento como insumo

---

## Checklist del PR

- [x] `10-modelado-amenazas.md` — STRIDE completo + OWASP Top Ten
- [x] `10-checklist-seguridad.md` — Estado real de los controles (6 ✅, 17 ❌, 3 ⚠️)
- [x] `10-controles-confianza.md` — 4 límites de confianza con controles por frontera
- [x] `10-priorizacion-amenazas.md` — 6 amenazas priorizadas + 4 genéricas de IA descartadas
- [x] Redacción en español, tono natural
- [x] Contenido basado en la arquitectura real de NEVI (no genérico)

---

*Documento de Semana 13 — Corte 3*  
*Universidad Jorge Tadeo Lozano — Arquitectura de Software*
