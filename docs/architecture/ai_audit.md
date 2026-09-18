# Auditoría de IA — Ciclo EDAV · NEVI

> **Ciclo EDAV:** Especificar → Delegar → Auditar → Verificar  
> **Fecha:** 2026-09-18  
> **Modelo consultado:** Claude Sonnet 4.6  
> **Rúbrica:** ✅ Válido · ✏️ Modificado · 🚫 Genérico · ❌ Falso

---

## E — Especificar

**Prompt entregado a la IA:**

> "Analiza los riesgos de seguridad y disponibilidad de un sistema con
> las siguientes características: API REST Spring Boot 3.3.4 en Java 21,
> PostgreSQL 16 con HikariCP (pool 10), JWT almacenado en localStorage
> del navegador, CORS con allowed-origins wildcard, ddl-auto update en
> JPA, WebSocket con STOMP y SockJS para chat en tiempo real, y sin
> pruebas de integración en el backend. Identifica 5 riesgos concretos
> con evidencia técnica."

---

## D — Delegar

La IA generó los siguientes 5 riesgos iniciales:

| # | Riesgo sugerido por IA |
|---|---|
| IA-01 | Pool HikariCP puede agotarse bajo carga de chat concurrente |
| IA-02 | JWT en localStorage es vulnerable a robo por XSS |
| IA-03 | `ddl-auto: update` puede corromper el esquema en producción |
| IA-04 | Sin pruebas de integración, un refactor puede romper el login sin detección |
| IA-05 | CORS wildcard combinado con JWT en localStorage amplía la superficie de ataque |

---

## A — Auditar

Cada riesgo es evaluado contra el código real del repositorio.

### IA-01 — Pool HikariCP agotado

**Clasificación: ✅ Válido**

El código confirma `maximum-pool-size: 10` en `application.yml`. El chat
WebSocket + STOMP mantiene conexiones persistentes que consumen hilos del
servidor; combinado con consultas a PostgreSQL por cada mensaje, el pool
puede agotarse con más de 10 usuarios activos simultáneamente.

**Acción:** incluido como R-01 sin modificación.

---

### IA-02 — JWT en localStorage vulnerable a XSS

**Clasificación: ✅ Válido**

El frontend guarda el token en `localStorage` bajo la clave `nevi_token`.
El chat en tiempo real permite enviar mensajes con contenido HTML/texto
que, si no se sanitiza correctamente en el cliente, puede ejecutar
scripts maliciosos que lean el token.

**Acción:** incluido como R-02 sin modificación. Es el riesgo de mayor
impacto del sistema.

---

### IA-03 — `ddl-auto: update` corrompe el esquema

**Clasificación: ✅ Válido**

`spring.jpa.hibernate.ddl-auto: update` está confirmado en
`application.yml`. Hibernate ejecuta DDL automático al arrancar,
sin control de versiones ni rollback posible.

**Acción:** incluido como R-03 sin modificación.

---

### IA-04 — Sin pruebas de integración

**Clasificación: ✅ Válido**

```bash
find backend/src/test -name "*.java" | wc -l
# → 0
```

No existe ningún archivo de prueba en el backend. Confirmado
empíricamente revisando el repositorio.

**Acción:** incluido como R-04 y marcado como bloqueante del Checkpoint
Hito 5.

---

### IA-05 — CORS wildcard amplía superficie de ataque

**Clasificación: ✏️ Modificado**

La IA presentó este riesgo como independiente. Sin embargo, su impacto
real es **condicional**: solo es peligroso en combinación con R-02 (JWT
en localStorage). Si el token estuviera en una cookie `HttpOnly`, el CORS
wildcard no permitiría que sitios externos lo usaran.

**Modificación aplicada al incluirlo como R-05:** se reencuadró como
riesgo amplificador de R-02, no como riesgo autónomo. La gravedad se
ajustó de Alta a Media porque depende de que R-02 ya se haya explotado.

---

## V — Verificar

### Resumen de clasificaciones

| ID IA | Clasificación | Acción |
|---|---|---|
| IA-01 | ✅ Válido | → R-01 sin cambios |
| IA-02 | ✅ Válido | → R-02 sin cambios; mayor impacto del sistema |
| IA-03 | ✅ Válido | → R-03 sin cambios |
| IA-04 | ✅ Válido | → R-04 sin cambios; marcado bloqueante |
| IA-05 | ✏️ Modificado | → R-05 reencuadrado como amplificador de R-02 |

**Rechazados:** ninguno. Los 5 riesgos tienen sustento en el código.
Un riesgo requirió corrección de encuadre antes de ser incorporado.

### Lección aprendida

> La IA identificó correctamente los 5 riesgos, pero presentó R-05 como
> un problema autónomo cuando en realidad su peligrosidad depende de R-02.
> El ciclo EDAV demostró que analizar las interdependencias entre riesgos
> es una tarea que el equipo debe hacer, no la IA.
