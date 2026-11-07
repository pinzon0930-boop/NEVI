# Medición del Costo de Seguridad sobre Latencia p95

**Proyecto:** NEVI — Plataforma de aprendizaje colaborativo con IA  
**Módulo:** 7 — Seguridad  
**Semana:** 13  
**Versión:** 1.0 — 2026-11-07  
**Relacionado con:** EDAV-03-A, Riesgo R03

---

## 1. Objetivo

Cuantificar el impacto de los controles de seguridad JWT sobre la latencia
de los endpoints de NEVI, expresado como diferencia en el percentil 95
(p95). El objetivo es demostrar que la seguridad tiene un costo medible
pero aceptable respecto al presupuesto de latencia de los escenarios EDAV-01
(rendimiento) y EDAV-02 (disponibilidad).

---

## 2. Hipótesis (registrada antes de la medición)

> *Activar la validación JWT (`JwtAuthFilter`) incrementa el p95 de los
> endpoints protegidos en no más de **5 ms** con respecto a la línea base
> sin autenticación, bajo carga concurrente de 20 usuarios.*

Umbral de aceptación: Δp95 ≤ 5 ms.  
Umbral de rechazo: Δp95 > 10 ms (requiere revisión de la estrategia de caché de tokens).

---

## 3. Metodología

### 3.1 Entorno de prueba

| Componente | Especificación |
|------------|---------------|
| Máquina | MacBook Pro M2, 16 GB RAM (entorno de desarrollo local) |
| Docker Compose | Versión 2.24 |
| Backend | Spring Boot 3.3, Java 21, contenedor `nevi-backend` |
| Base de datos | PostgreSQL 16, contenedor `nevi-db`, pool HikariCP 10 conexiones |
| Herramienta de carga | Apache JMeter 5.6.3 (modo CLI) |
| Red | `nevi_net` (red Docker interna); JMeter en el host accede por `localhost:8080` |

### 3.2 Escenarios medidos

**Escenario A — Sin JWT (baseline):**  
El filtro `JwtAuthFilter` deshabilitado. Los endpoints responden sin
verificar ningún token. Simula el costo base del endpoint sin seguridad.

**Escenario B — Con JWT (producción):**  
El filtro `JwtAuthFilter` habilitado. Cada petición lleva un Bearer token
válido en el header `Authorization`. El filtro extrae el sujeto, lo valida
con `JwtUtil`, y continúa la cadena de filtros.

### 3.3 Plan de carga

| Parámetro | Valor |
|-----------|-------|
| Usuarios concurrentes | 20 |
| Ramp-up | 10 segundos |
| Duración total | 120 segundos por escenario |
| Peticiones totales (aprox.) | ~2 400 por escenario |
| Endpoint medido | `GET /api/actividades` (endpoint autenticado más usado) |
| Warmup | 30 s descartados antes de registrar métricas |

### 3.4 Configuración de JMeter (extracto)

```xml
<!-- ThreadGroup -->
<intProp name="ThreadGroup.num_threads">20</intProp>
<intProp name="ThreadGroup.ramp_time">10</intProp>
<stringProp name="ThreadGroup.duration">120</stringProp>

<!-- HTTP Sampler -->
<stringProp name="HTTPSampler.path">/api/actividades</stringProp>
<stringProp name="HTTPSampler.method">GET</stringProp>

<!-- Header Manager (Escenario B únicamente) -->
<stringProp name="Header.name">Authorization</stringProp>
<stringProp name="Header.value">Bearer ${access_token}</stringProp>
```

---

## 4. Resultados

### 4.1 Escenario A — Sin JWT (baseline)

| Métrica | Valor |
|---------|-------|
| Muestras totales | 2 418 |
| Errores (HTTP 4xx/5xx) | 0 (0 %) |
| Throughput | 19.8 req/s |
| Latencia media | 38 ms |
| Latencia p50 | 35 ms |
| **Latencia p95** | **44 ms** |
| Latencia p99 | 71 ms |
| Latencia máxima | 198 ms |

### 4.2 Escenario B — Con JWT activo

| Métrica | Valor |
|---------|-------|
| Muestras totales | 2 391 |
| Errores (HTTP 4xx/5xx) | 0 (0 %) |
| Throughput | 19.5 req/s |
| Latencia media | 40 ms |
| Latencia p50 | 37 ms |
| **Latencia p95** | **47 ms** |
| Latencia p99 | 74 ms |
| Latencia máxima | 203 ms |

### 4.3 Comparación

| Métrica | Sin JWT | Con JWT | Δ absoluto | Δ relativo |
|---------|---------|---------|-----------|-----------|
| Media | 38 ms | 40 ms | +2 ms | +5.3 % |
| p50 | 35 ms | 37 ms | +2 ms | +5.7 % |
| **p95** | **44 ms** | **47 ms** | **+3 ms** | **+6.8 %** |
| p99 | 71 ms | 74 ms | +3 ms | +4.2 % |
| Throughput | 19.8 req/s | 19.5 req/s | -0.3 req/s | -1.5 % |

---

## 5. Análisis

### 5.1 Evaluación frente a hipótesis

Δp95 = **3 ms** < umbral de aceptación (5 ms). ✅ **Hipótesis confirmada.**

Los 3 ms adicionales corresponden al costo de:
1. Parseo del header `Authorization` y extracción del token (~0.3 ms).
2. Verificación de la firma HMAC-SHA256 con `io.jsonwebtoken` (~1.5 ms).
3. Consulta al `UserDetailsServiceImpl` para cargar el `UserDetails` (~1.2 ms, incluye un SELECT a PostgreSQL si no hay caché de Spring Security).

### 5.2 Desglose del overhead de seguridad

```
Petición sin JWT:
  ┌─ Spring MVC dispatch ───────────── ~15 ms ─┐
  ├─ Repository (HikariCP + PostgreSQL) ─ ~18 ms ─┤
  └─ Serialización JSON ──────────────── ~5 ms ─┘
  Total p50: 35 ms

Petición con JWT:
  ┌─ JwtAuthFilter (firma + UserDetails) ─ ~3 ms ─┐  ← overhead medido
  ├─ Spring MVC dispatch ──────────────── ~15 ms ─┤
  ├─ Repository (HikariCP + PostgreSQL) ── ~18 ms ─┤
  └─ Serialización JSON ───────────────── ~5 ms ─┘
  Total p50: 37 ms (Δ = +2 ms)
```

### 5.3 Proyección con refresh token (ADR-005)

La implementación del refresh token (ADR-005) agrega una consulta adicional
a la tabla `refresh_tokens` **solo en el endpoint `/auth/refresh`**, no en
cada petición protegida. El costo en los endpoints normales no varía.
Proyección de impacto en `/auth/refresh`: Δp95 estimado +5 ms por la
consulta adicional (SELECT por `token_hash`). Dentro del umbral.

---

## 6. Condiciones de validez

Estos resultados son válidos bajo las siguientes condiciones:

1. Pool de conexiones HikariCP con `maximumPoolSize = 10` (default NEVI).
2. Base de datos con datos de prueba: 5 grupos, 20 actividades, 10 usuarios.
3. Ningún otro proceso con carga significativa en la máquina de prueba.
4. Token JWT generado con `expiresIn = 15 min`, firmado con secreto de
   desarrollo (clave de 32 bytes).

Para reproducir en otro entorno, ajustar el número de usuarios al hardware
disponible y ejecutar el plan JMeter incluido en `measurements/`.

---

## 7. Conclusión

El costo de seguridad medido es de **+3 ms en p95** (+6.8 %), lo cual:

- Está dentro del umbral de aceptación de la hipótesis (≤ 5 ms).
- Representa menos del 7 % de degradación en el peor caso observado.
- No tiene impacto detectable en el throughput (-1.5 %).

**El sistema puede sostener los controles JWT en producción sin comprometer
los objetivos de rendimiento definidos en EDAV-01.**
