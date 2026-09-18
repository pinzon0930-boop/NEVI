# 02 — Resultados de la medición baseline · NOVI

**Fecha de ejecución:** 2026-09-18
**Escenario:** ESC-02 — Latencia p95 en `GET /api/grupos`
**Preregistro correspondiente:** `01-preregistro.md`

---

## Resultados por corrida

| Corrida | Peticiones | p50 (mediana) | p95 | p99 | Error % |
|---|---|---|---|---|---|
| corrida-01 | 1,170 | 10.04 ms | 28.30 ms | — | 0.00% |
| corrida-02 | 1,180 | 7.85 ms | 18.17 ms | — | 0.00% |
| corrida-03 | 1,182 | 6.93 ms | 13.22 ms | — | 0.00% |

## Resultado representativo (mediana de las 3 corridas)

| Métrica | Valor |
|---|---|
| **p50** | 7.85 ms |
| **p95** | **18.17 ms** |
| **Tasa de error** | 0.00% |
| Checks pasados | 10,596 / 10,596 (100%) |

---

## Comparación contra la hipótesis

| Métrica | Hipótesis (preregistro) | Resultado real | Veredicto |
|---|---|---|---|
| p50 | 100–300 ms | 7.85 ms | Muy por debajo de lo esperado |
| p95 | 300–800 ms | 18.17 ms | Muy por debajo de lo esperado |
| Error | < 5% | 0% | Confirmado |

**Veredicto formal:** la hipótesis se **refuta** — no porque el sistema
rindiera peor de lo esperado, sino porque el margen de seguridad asumido
era demasiado conservador. El criterio de éxito (p95 ≤ 800 ms) sí se
cumple ampliamente.

---

## Análisis de causa raíz

La hipótesis original asumía que el pool de HikariCP (máximo 10 conexiones)
sería un cuello de botella perceptible con 10 VUs concurrentes. Los datos
muestran que esto no ocurrió, por las siguientes razones:

1. **Volumen de datos mínimo:** la semilla usada (5 grupos por usuario, 3
   miembros por grupo) genera un JOIN trivial para PostgreSQL — sin
   necesidad de índices adicionales a esta escala.

2. **Sin latencia de red real:** backend y base de datos comparten el mismo
   equipo físico vía Docker Desktop (red interna de contenedores), a
   diferencia de un entorno de producción distribuido.

3. **10 VUs no satura 10 conexiones del pool:** cada request es de
   corta duración (~10ms), por lo que las conexiones se liberan y
   reutilizan antes de generar contención real.

4. **Efecto de *warmup*:** el p95 bajó de 28.3ms (corrida-01) a 13.22ms
   (corrida-03), consistente con la JVM optimizando el bytecode en caliente
   (JIT) y el pool de conexiones ya establecido desde la primera corrida.

---

## Limitaciones de esta medición

- El resultado **no es representativo de un entorno de producción** con
  usuarios reales distribuidos por red, latencia variable y un volumen de
  datos mucho mayor.
- La condición "10 VUs" fue elegida por ser el máximo del pool configurado
  actualmente, no por representar un pico de uso realista del curso.
- No se midió el comportamiento del sistema bajo un número de VUs mayor al
  tamaño del pool (por ejemplo, 20-30 VUs), que sí pondría a prueba R-01
  de forma más directa.

## Trabajo futuro sugerido

Para poner a prueba realmente el riesgo R-01 (agotamiento del pool), se
recomienda una corrida adicional con **20-30 VUs concurrentes** — por
encima del límite de 10 conexiones — y comparar si aparecen errores
`HikariPool-1 - Connection is not available`. Esta corrida queda fuera del
alcance de la medición baseline actual y se documenta como trabajo futuro.
