# Matriz de Riesgos Operativos — NEVI

**Proyecto:** NEVI — Network of Virtual Interaction  
**Semana:** 14 · Corte 3  
**Tiempo estimado:** 60 min  
**Autor:** Nixson Camilo Pinzón Muñoz  

---

## ¿Qué son los riesgos operativos?

Los riesgos de seguridad (Semana 13) se preguntaban: ¿qué puede hacer un atacante? Los riesgos operativos se preguntan: ¿qué puede salir mal en la operación normal del sistema, aunque nadie tenga malas intenciones?

Incluyen cosas como: se va la luz, se llena el disco, una dependencia externa falla, se hace un deploy con un bug, o simplemente hay más usuarios de los esperados.

La diferencia clave es que los riesgos operativos se mitigan con procedimientos, monitoreo y arquitectura resiliente — no principalmente con seguridad.

---

## Criterios de evaluación

**Probabilidad:**
- **Alta** — Pasa regularmente o es muy fácil que pase
- **Media** — Podría pasar, requiere condiciones específicas
- **Baja** — Poco probable, pero posible

**Impacto:**
- **Alto** — El sistema deja de funcionar o hay pérdida de datos
- **Medio** — Funcionalidad degradada, algunos usuarios afectados
- **Bajo** — Molestia menor, fácil de recuperar

**Nivel de riesgo:**
- 🔴 **Crítico** — Alta probabilidad + Alto impacto
- 🟠 **Alto** — Al menos uno de los dos es alto
- 🟡 **Medio** — Probabilidad media + Impacto medio
- 🟢 **Bajo** — Probabilidad baja o Impacto bajo

---

## Matriz de riesgos operativos

### Categoría 1 — Disponibilidad

| ID | Riesgo | Prob. | Impacto | Nivel | Mitigación | Detección |
|---|---|---|---|---|---|---|
| O01 | El contenedor de Spring Boot falla y no se reinicia | Media | Alto | 🟠 Alto | `restart: unless-stopped` en docker-compose; health check configurado | Alerta: no hay requests exitosos en 2 min |
| O02 | El contenedor de PostgreSQL falla | Baja | Alto | 🟠 Alto | `restart: unless-stopped`; backups diarios | Alerta: Spring Boot no puede conectar a BD |
| O03 | Saturación del pool JDBC (HikariCP) | Alta | Medio | 🟠 Alto | Rate limiting Nginx; circuit breaker; ajustar `maximumPoolSize` | Métrica: `hikaricp.connections.pending > 0` |
| O04 | Groq API cae o tiene alta latencia | Media | Medio | 🟡 Medio | Circuit breaker (Resilience4j) con fallback "servicio no disponible" | Métrica: circuit breaker en OPEN |
| O05 | La VM cae (mantenimiento del proveedor) | Baja | Alto | 🟠 Alto | No hay redundancia en Opción A; aceptado para contexto académico | Monitor externo (UptimeRobot gratis) |

### Categoría 2 — Datos

| ID | Riesgo | Prob. | Impacto | Nivel | Mitigación | Detección |
|---|---|---|---|---|---|---|
| O06 | Se llena el disco del servidor | Media | Alto | 🟠 Alto | Monitorear uso de disco; retención de logs configurada (30 días máx) | Alerta: disco > 80% |
| O07 | Corrupción de datos en PostgreSQL por apagado abrupto | Baja | Alto | 🟠 Alto | PostgreSQL usa WAL (Write-Ahead Log) por defecto; resiste apagados abruptos | Verificar integridad al arrancar |
| O08 | Pérdida de datos por ausencia de backups | Media | Alto | 🟠 Alto | Backup diario de PostgreSQL con `pg_dump`; guardar en almacenamiento externo (S3 o similar) | Verificación diaria de que el backup se generó |
| O09 | Deploy fallido que corrompe la BD | Baja | Alto | 🟠 Alto | Nunca ejecutar migraciones sin backup previo; usar Flyway con transacciones | Health check post-deploy |

### Categoría 3 — Rendimiento

| ID | Riesgo | Prob. | Impacto | Nivel | Mitigación | Detección |
|---|---|---|---|---|---|---|
| O10 | Latencia alta en endpoints de BD (queries sin índice) | Media | Medio | 🟡 Medio | Revisar `EXPLAIN ANALYZE` en queries frecuentes; índices en FKs | Métrica: p95 > 500 ms |
| O11 | Memory leak en Spring Boot — heap se llena con el tiempo | Baja | Medio | 🟡 Medio | Monitorear heap JVM; reinicio programado si no se encuentra el leak | Alerta: heap > 85% |
| O12 | WebSocket — muchas conexiones simultáneas en modo activo | Media | Medio | 🟡 Medio | Límite de conexiones por usuario; timeout de inactividad en STOMP | Métrica: threads activos de WebSocket |

### Categoría 4 — Proceso y operación

| ID | Riesgo | Prob. | Impacto | Nivel | Mitigación | Detección |
|---|---|---|---|---|---|---|
| O13 | Deploy con bug que rompe la API | Media | Alto | 🟠 Alto | Health check post-deploy; procedimiento de rollback documentado | Alerta: error rate > 5% en 5 min post-deploy |
| O14 | Variable de entorno faltante en servidor | Media | Alto | 🟠 Alto | Validar variables al arrancar la app (Spring `@Value` con `required=true`); doc de variables necesarias | La app no arranca si falta una variable |
| O15 | Certificado TLS vencido | Media | Alto | 🟠 Alto | Let's Encrypt con renovación automática (certbot); alerta 30 días antes | Monitor externo verifica HTTPS |
| O16 | Cambio de API key de Groq sin actualizar `.env` | Baja | Medio | 🟡 Medio | Procedimiento documentado para rotación de credenciales | Alerta: errores 401 en llamadas a Groq API |

---

## Riesgos aceptados

Estos riesgos se identificaron pero se acepta convivir con ellos por el contexto académico del proyecto:

| ID | Riesgo | Razón de aceptación |
|---|---|---|
| O05 | Caída de la VM única | Sin presupuesto para redundancia; el uptime no es crítico en contexto académico |
| O11 | Memory leak potencial | Código académico controlado; reinicio manual es aceptable |
| O12 | Muchas conexiones WebSocket | El volumen de usuarios académicos es bajo |

---

## Plan de respuesta a incidentes (simplificado)

Para los riesgos de nivel 🔴 Crítico o 🟠 Alto, el procedimiento básico es:

**1. Detectar:** La alerta llega por el sistema de monitoreo o un usuario reporta el problema.

**2. Evaluar (< 5 min):**
- ¿Está caído todo el sistema o solo una parte?
- ¿Hay pérdida de datos?
- ¿Cuántos usuarios están afectados?

**3. Contener:**
- Si la API está caída: revisar logs del contenedor (`docker logs nevi-api`)
- Si la BD está caída: verificar estado del contenedor (`docker ps`)
- Si hay deploy roto: hacer rollback a la imagen anterior

**4. Comunicar:**
- Notificar a usuarios si el tiempo estimado de recuperación es > 15 min

**5. Recuperar:**
- Seguir el procedimiento específico para cada tipo de fallo

**6. Revisar:**
- Después de resolver: documentar qué pasó, cuánto duró y qué se puede mejorar para que no pase de nuevo

---

## Conexión con observabilidad

Cada riesgo operativo tiene al menos una señal de detección definida en `12-despliegue-observabilidad.md`. El principio es: si no hay una alerta que detecte el riesgo automáticamente, el riesgo pasa de "conocido" a "oculto".

Los riesgos sin detección automática (O07, O08, O16) requieren verificación manual periódica — deben estar en un checklist de mantenimiento semanal.

---

*Documento de Semana 14 — Corte 3*  
*Universidad Jorge Tadeo Lozano — Arquitectura de Software*
