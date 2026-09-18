# 01 — Preregistro de medición · NOVI

> Este documento debe existir **antes** de ejecutar cualquier corrida de
> carga. Su fecha en Git sirve como evidencia de que la hipótesis se
> formuló antes de ver los resultados (no después).

**Fecha de preregistro:** 2026-09-18
**Escenario medido:** ESC-02 — Latencia p95 en `GET /api/grupos`

---

## Hipótesis

Con la configuración base de HikariCP (`maximum-pool-size: 10`) y sin
índices adicionales en PostgreSQL, se espera que la operación
`GET /api/grupos` con 10 usuarios virtuales (VUs) concurrentes durante
60 segundos muestre:

- **Latencia mediana (p50):** entre 100 ms y 300 ms
- **Latencia p95:** entre 300 ms y 800 ms
- **Tasa de error HTTP:** < 5 %

**Razonamiento:** la consulta implica un JOIN entre `grupos` y
`group_members` filtrado por `user_id`. Con la semilla de 5 grupos por
usuario y 3 miembros por grupo, el resultado es pequeño. El cuello de
botella esperado es la contención del pool de conexiones con 10 VUs.

---

## Protocolo de medición

### Herramienta
k6 (versión ≥ 0.49.0)

### Operación medida
`GET /api/grupos`
Con token JWT válido generado previamente (login manual antes de la corrida).

### Configuración de carga
- VUs: 10
- Duración: 60 s
- Rampa: sin rampa (carga constante desde el inicio)

### Máquina de prueba
_(completar antes de correr)_
- Plataforma: Docker Desktop — SO: Windows 11
- CPU: Ryzen 7 7735HS
- RAM: 16 Ram

### Semilla de datos
- 5 usuarios de prueba registrados (1 profesor, 4 estudiantes)
- 5 grupos creados por el profesor
- Los 4 estudiantes son miembros de al menos 3 grupos cada uno
- Sin mensajes ni actividades adicionales (estado limpio)

### Verificación de códigos de respuesta
El script k6 incluye un check que valida que cada respuesta retorna
HTTP 200. Las respuestas 4xx y 5xx se cuentan como errores.

### Número de corridas
Mínimo 3 corridas (`corrida-01`, `corrida-02`, `corrida-03`).
Se reporta la **mediana de los valores p95** de las tres corridas como
resultado representativo.

---

## Criterio de éxito

La hipótesis se confirma si:
- Latencia p95 ≤ 800 ms en las 3 corridas.
- Tasa de error < 5 % en las 3 corridas.

La hipótesis se refuta si:
- Latencia p95 > 800 ms en al menos 2 de las 3 corridas.
- Tasa de error ≥ 5 % en alguna corrida.

En caso de refutación, se documenta en `02-resultados-baseline.md` con
análisis de causa.
