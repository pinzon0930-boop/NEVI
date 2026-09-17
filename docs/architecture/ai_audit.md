# AI Audit — Registro Crítico de Sugerencias de IA (Ciclo EDAV)

**Estado:** Semana 2 · Hito 6  
**Fecha:** 2026-09-11  
**Protocolo:** Ciclo EDAV — Especificar → Delegar → Auditar → Verificar

> **Advertencia del curso:** El uso ciego de las salidas de la IA sin contrastarlas contra el código real constituye una violación grave a la integridad académica del curso.

---

## Paso 1 — ESPECIFICAR: Contexto entregado al LLM

Se entregó el siguiente contexto técnico real al LLM antes de generar sugerencias de riesgo:

```
Sistema: NEVI — plataforma educativa en tiempo real
Stack declarado en pom.xml y package.json:
- Spring Boot 3.3.4 / Java 21
- React 18 + Vite 5 + Tailwind CSS
- PostgreSQL 16 (Docker)
- WebSocket / STOMP sobre SockJS
- JWT almacenado en localStorage (useAuth.jsx)
- API Groq externa (groq.js)
- Docker Compose: 3 servicios (db, backend, frontend/Nginx)

Configuración relevante en application.yml:
- ddl-auto: update
- allowed-origins: "*"
- HikariCP sin maximum-pool-size declarado (default: 10)
- JWT secret en variable de entorno (default hardcodeado)

Diagrama: frontend → backend REST (JWT) + backend WS (STOMP) → PostgreSQL
```

---

## Paso 2 — DELEGAR: Prompt estructurado ejecutado

> "Actúa como un Auditor de Arquitectura Senior. Analiza el contexto técnico provisto y genera 5 riesgos iniciales específicos sobre seguridad, escalabilidad, modificabilidad, concurrencia o contención de datos. Para cada riesgo, indica el componente o archivo afectado y la variable física de estrés involucrada."

---

## Paso 3 — AUDITAR: Triage humano de las 5 sugerencias

Las 5 sugerencias generadas por el LLM fueron debatidas y clasificadas usando la rúbrica cualitativa del curso:

| # | Sugerencia del LLM | Componente mencionado | Clasificación | Justificación del equipo |
|---|---|---|---|---|
| 1 | "El pool de conexiones JDBC puede agotarse bajo alta concurrencia dado que HikariCP usa 10 conexiones por defecto y el sistema tiene WebSocket activo" | `application.yml` — ausencia de `maximum-pool-size` | ✅ **Válido** | Específico, trazable y verificable: `grep -n "maximum-pool-size" application.yml` → sin resultado. El riesgo es real y cuantificable. |
| 2 | "El JWT en localStorage es vulnerable a ataques XSS ya que cualquier script puede ejecutar `localStorage.getItem('nevi_token')`" | `useAuth.jsx` — `localStorage.setItem` | ✅ **Válido** | Verificado manualmente: abrir consola en localhost:80 → `localStorage.getItem('nevi_token')` devuelve el token activo. Riesgo real y reproducible. |
| 3 | "La configuración `ddl-auto: update` puede corromper el esquema de base de datos si una entidad JPA es refactorizada sin migración explícita" | `application.yml` línea 17 | ✅ **Válido** | `grep -n "ddl-auto" application.yml` → `ddl-auto: update` confirmado. Área de dolor legítima con impacto directo en la integridad de datos. |
| 4 | "El sistema podría ser vulnerable a ataques de inyección SQL a través de los endpoints de búsqueda de usuarios" | Controllers REST | ✏️ **Modificado** | El LLM describe SQLi genérico, pero NEVI usa Spring Data JPA con métodos derivados (`findByEmail`, `findById`) que usan parametrización nativa del ORM — SQL literal nunca se construye manualmente. El riesgo real es diferente: consultas JPQL mal escritas con concatenación de strings, que no existen en el código actual. **Se registra como falso para SQL injection clásico, pero se mantiene como alerta de vigilancia en futuras adiciones de queries nativas.** |
| 5 | "El sistema podría caer si el servidor no tiene electricidad o la conexión a internet falla" | Servidor en general | 🚫 **Genérico** | Perogrullada aplicable a cualquier software del mundo. No aporta ningún valor de diseño ingenieril. Descartado de inmediato por vaguedad. |

---

## Paso 4 — VERIFICAR: Declaración escrita de evidencia empírica

### Riesgo 1 — Válido (sin modificación)
**Comando de verificación:**
```bash
grep -n "maximum-pool-size" backend/src/main/resources/application.yml
# Resultado: sin líneas → confirma que se usa el default de 10 conexiones
```
**Decisión:** Aceptado como riesgo R-01. Se registra en `03-inventario-riesgos.md`. Mitigación: agregar `spring.datasource.hikari.maximum-pool-size: 25`.

---

### Riesgo 2 — Válido (sin modificación)
**Verificación manual:**
```
1. Abrir http://localhost:80 en el navegador
2. Iniciar sesión con cualquier usuario
3. Abrir DevTools → Consola → ejecutar: localStorage.getItem('nevi_token')
4. Resultado: el token JWT completo aparece en claro
```
**Decisión:** Aceptado como riesgo R-02. Mitigación a corto plazo: Content Security Policy en `nginx.conf`.

---

### Riesgo 3 — Válido (sin modificación)
**Comando de verificación:**
```bash
grep -n "ddl-auto" backend/src/main/resources/application.yml
# Resultado: línea 17: ddl-auto: update
```
**Decisión:** Aceptado como riesgo R-03. Mitigación: migrar a `validate` + Flyway.

---

### Riesgo 4 — Modificado
**Verificación del rechazo de SQLi clásico:**
```bash
# Buscar si existe alguna query con concatenación de strings en los repositories
grep -rn "createQuery\|createNativeQuery\|EntityManager" backend/src/main/java/com/nevi/
# Resultado esperado: sin resultados → JPA usa métodos derivados, no SQL literal
```
**Corrección aplicada:** El LLM asumió SQLi clásico. NEVI usa Spring Data JPA con métodos derivados (`findByEmail(String email)`) que son inmunes a SQLi por parametrización del ORM. La aserción del LLM fue imprecisa tecnológicamente. Se documenta como **Modificado** en el AI Decision Log: el área de dolor es vigilar futuras queries nativas, no el código actual.

---

### Riesgo 5 — Rechazado (Genérico)
**Justificación del rechazo:**
La sugerencia "el servidor podría caer si no tiene electricidad" es una perogrullada aplicable a cualquier sistema computacional. No cita ningún componente específico de NEVI, no es verificable empíricamente en el repositorio y no contribuye a ninguna decisión de diseño. **Rechazado por vaguedad.**

---

## Resumen del AI Decision Log

| Riesgo IA | Clasificación final | Acción |
|---|---|---|
| Pool JDBC agotado | ✅ Válido | Inyectado en dossier como R-01 + issue de mitigación |
| JWT en localStorage | ✅ Válido | Inyectado en dossier como R-02 + mitigación CSP |
| `ddl-auto: update` | ✅ Válido | Inyectado en dossier como R-03 + mitigación Flyway |
| Inyección SQL | ✏️ Modificado | La aserción técnica corregida: JPA parametriza; vigilancia en queries nativas futuras |
| Servidor sin electricidad | 🚫 Genérico | Rechazado — descartado del dossier por vaguedad total |
