# Despliegue y Observabilidad — NEVI

**Proyecto:** NEVI — Network of Virtual Interaction  
**Semana:** 14 · Corte 3  
**Tiempo estimado:** 90 min (despliegue) + 90 min (observabilidad)  
**Autor:** Nixson Camilo Pinzón Muñoz  

---

## Parte 1 — Despliegue conceptual y comparación de costos

### ¿Qué significa "despliegue conceptual"?

En esta etapa no estamos comprando servidores ni configurando nada en producción real. Estamos decidiendo en papel cómo se va a ver la arquitectura cuando salga de Docker Compose local y vaya a un entorno que otras personas puedan usar.

La pregunta central es: ¿dónde viven los contenedores de NEVI?

---

### Opción A — Una sola VM (todo en un servidor)

```
┌─────────────────────────────────────────┐
│          VM Única (~$15/mes)            │
│                                         │
│  ┌──────────┐    ┌─────────────────┐   │
│  │  Nginx   │───▶│  Spring Boot    │   │
│  │ (puerto  │    │  (API + WS)     │   │
│  │  80/443) │    │                 │   │
│  └──────────┘    └────────┬────────┘   │
│                           │            │
│                  ┌────────▼────────┐   │
│                  │  PostgreSQL 16  │   │
│                  │  (mismo host)   │   │
│                  └─────────────────┘   │
│                                         │
│  Red: todos los servicios en localhost  │
└─────────────────────────────────────────┘
              │
              ▼ HTTPS
         [Usuarios]
```

**Especificación:** 1 VPS (DigitalOcean Droplet, Hetzner CX21, o similar)
- 2 vCPU, 4 GB RAM, 80 GB SSD
- Ubuntu 22.04 LTS + Docker + Docker Compose
- Nginx maneja TLS con Let's Encrypt

**Costos aproximados (mensuales):**

| Componente | Proveedor | Costo/mes |
|---|---|---|
| VM (2 vCPU, 4 GB) | DigitalOcean | $24 |
| VM (2 vCPU, 4 GB) | Hetzner CX21 | ~$6 |
| Dominio | Namecheap | ~$1 (amortizado) |
| Certificado TLS | Let's Encrypt | Gratis |
| **Total DigitalOcean** | | **~$25/mes** |
| **Total Hetzner** | | **~$7/mes** |

**Ventajas:**
- Configuración más simple — Docker Compose funciona casi igual que en desarrollo
- Costo más bajo
- Una sola máquina que administrar

**Desventajas:**
- Si la VM falla, todo cae — API, BD y frontend al mismo tiempo
- La base de datos y la API comparten CPU/RAM — un pico de tráfico puede afectar a la BD
- Escalar requiere migrar a otra arquitectura
- No hay separación de red entre API y BD

**Cuándo es apropiada:** Proyecto académico, MVP, menos de 100 usuarios activos simultáneos.

---

### Opción B — Dos VMs con red privada

```
┌──────────────────────┐    Red privada    ┌──────────────────────┐
│   VM 1 — Frontend    │◄─────────────────▶│   VM 2 — Backend     │
│   + API (~$15/mes)   │                   │   + BD (~$15/mes)    │
│                      │                   │                      │
│  ┌────────────────┐  │                   │  ┌────────────────┐  │
│  │ Nginx (443)    │  │                   │  │ PostgreSQL 16  │  │
│  │ Spring Boot    │  │  10.x.x.x:5432   │  │ (puerto cerrado│  │
│  │ (puerto 8080   │  │──────────────────▶│  │ al exterior)   │  │
│  │  interno)      │  │                   │  └────────────────┘  │
│  └────────────────┘  │                   └──────────────────────┘
└──────────────────────┘
              │
              ▼ HTTPS (solo VM 1 tiene IP pública)
         [Usuarios]
```

**Especificación:** 2 VPS conectados por red privada (VLAN o VPC)
- VM 1: Nginx + Spring Boot (IP pública)
- VM 2: PostgreSQL solo (IP privada, sin exposición a internet)

**Costos aproximados (mensuales):**

| Componente | Proveedor | Costo/mes |
|---|---|---|
| VM 1 (2 vCPU, 2 GB) | Hetzner CX21 | ~$6 |
| VM 2 (2 vCPU, 2 GB) | Hetzner CX21 | ~$6 |
| Red privada (VLAN) | Hetzner | Gratis |
| Dominio + TLS | Namecheap + Let's Encrypt | ~$1 |
| **Total Hetzner** | | **~$13/mes** |
| VM 1 (DigitalOcean) | | $12 |
| VM 2 (DigitalOcean) | | $12 |
| VPC | DigitalOcean | Gratis |
| **Total DigitalOcean** | | **~$25/mes** |

**Ventajas:**
- La base de datos no tiene IP pública — solo accesible desde VM 1 por red privada
- Fallo de la API no afecta directamente a la BD
- Separación de responsabilidades más clara
- Escalar cada capa de forma independiente

**Desventajas:**
- Dos máquinas que configurar y mantener
- Latencia de red entre API y BD (mínima en la misma región, ~1 ms)
- Configuración de firewall más compleja

**Cuándo es apropiada:** Cuando hay más de ~50 usuarios y se quiere separación real entre capas.

---

### Comparación directa

| Criterio | Opción A (1 VM) | Opción B (2 VMs) |
|---|---|---|
| Costo mínimo mensual | ~$7 (Hetzner) | ~$13 (Hetzner) |
| Complejidad de setup | Baja | Media |
| Aislamiento de BD | ❌ Mismo host | ✅ Red privada |
| Tolerancia a fallos | ❌ Un punto de fallo | ⚠️ Mejor, no ideal |
| Capacidad de escalar | Limitada | Mejor |
| Apropiada para NEVI (académico) | ✅ Sí | ✅ Sí |

**Decisión recomendada para NEVI:** Opción A para el contexto académico actual. Si el proyecto avanzara a producción real con usuarios reales, migrar a Opción B. Esta decisión se formaliza en el ADR 5.

---

## Parte 2 — Plan de observabilidad

### ¿Qué es observabilidad y por qué la necesitamos?

Observabilidad es la capacidad de entender qué está pasando dentro del sistema desde afuera — sin tener que conectarse al servidor y mirar logs manualmente. Se construye con tres señales:

1. **Logs** — qué pasó exactamente (eventos, errores, acciones de usuarios)
2. **Métricas** — qué tan bien está funcionando (latencia, uso de recursos, errores por segundo)
3. **Trazas** — cómo viajó una request por todos los servicios del sistema

En el módulo 8 identificamos varios escenarios de fallo. Este plan define cómo detectar cada uno antes de que un usuario lo reporte.

---

### Señal 1 — Logs

**¿Qué queremos lograr?**  
Que cada evento importante quede registrado con suficiente contexto para investigar un incidente sin tener que hacer preguntas.

**Formato de log:**
```json
{
  "timestamp": "2026-11-06T18:30:00.123Z",
  "level": "INFO",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "spanId": "ab12cd34",
  "userId": "usr-42",
  "action": "SUBMISSION_CREATED",
  "resource": "/api/groups/7/submissions",
  "durationMs": 45,
  "message": "Entrega creada exitosamente"
}
```

**Reglas de logging:**
- Nivel en producción: INFO/WARN/ERROR (nunca DEBUG en prod)
- Nunca loggear: `passwordHash`, tokens JWT, credenciales
- Siempre incluir: `traceId`, `userId` (si está autenticado), `action`, `resource`
- Objetos completos: nunca serializar entidades JPA directamente en logs

**Implementación en Spring Boot:**
```java
// En el filtro de cada request:
String traceId = UUID.randomUUID().toString();
MDC.put("traceId", traceId);
MDC.put("userId", extractUserIdFromJwt(request));

// logback-spring.xml con logstash-logback-encoder:
// Genera JSON automáticamente con todos los campos del MDC
```

---

### Señal 2 — Métricas

**Métricas clave para NEVI (con Spring Boot Actuator + Micrometer):**

| Métrica | Qué mide | Umbral de alerta |
|---|---|---|
| `http.server.requests` (p95) | Latencia del 95% de requests | > 500 ms |
| `http.server.requests` (error rate) | % de requests con error 5xx | > 1% en 5 min |
| `hikaricp.connections.active` | Conexiones JDBC en uso | > 80% del pool |
| `hikaricp.connections.pending` | Requests esperando conexión | > 0 por más de 10 s |
| `jvm.memory.used` | Uso de memoria JVM | > 85% del heap |
| `process.cpu.usage` | CPU del proceso | > 80% por más de 2 min |

**Vinculado a escenarios de fallo del módulo 8:**

| Escenario de fallo (módulo 8) | Métrica que lo detecta | Alerta |
|---|---|---|
| H07: Pool JDBC al 70% (14/20 conexiones) | `hikaricp.connections.active` ≥ 14 | ⚠️ Aviso |
| H07: Pool JDBC saturado — requests en timeout | `hikaricp.connections.pending` > 0 | 🔴 Crítico |
| Latencia alta en endpoints de actividades | `http.server.requests` p95 > 500 ms | ⚠️ Aviso |
| API devolviendo errores 5xx | `http.server.requests{status=5xx}` > 1% | 🔴 Crítico |
| Groq API con timeout externo | `resilience4j.circuitbreaker.state = OPEN` | ⚠️ Aviso |

---

### Señal 3 — Trazas distribuidas

**¿Para qué sirven las trazas en NEVI?**  
Cuando un usuario reporta que "la entrega no se guardó", las trazas permiten seguir ese request desde Nginx → Spring Boot → PostgreSQL y ver exactamente dónde falló y cuánto tardó cada paso.

**Implementación con W3C Trace Context:**
- Header `traceparent` propagado entre servicios
- Formato: `00-{traceId}-{spanId}-{flags}`
- En Spring Boot: Spring Cloud Sleuth o Micrometer Tracing + Zipkin

**Traza de ejemplo (request de creación de entrega):**
```
Trace: a1b2c3d4...
  ├── [0 ms]  Nginx → Spring Boot (HTTP 1 ms)
  ├── [1 ms]  JwtAuthFilter.doFilter (3 ms)
  ├── [4 ms]  SubmissionController.create (40 ms)
  │     ├── [4 ms]  SubmissionService.validateOwnership (5 ms)
  │     └── [9 ms]  SubmissionRepository.save (35 ms) ← LENTO
  └── [44 ms] Response HTTP 201
```

Esto permite identificar que el cuello de botella está en la escritura a PostgreSQL, no en la lógica de negocio.

---

### Alertas críticas

Estas son las alertas que deben dispararse de forma automática — no esperar a que alguien revise un dashboard.

| Nombre | Condición | Severidad | Acción |
|---|---|---|---|
| **API Down** | No hay requests exitosos en 2 min | 🔴 Crítico | Reiniciar servicio; notificar |
| **Pool JDBC saturado** | `hikaricp.connections.pending` > 0 por > 30 s | 🔴 Crítico | Escalar o reiniciar; revisar queries lentas |
| **Error rate alta** | Más de 5% de requests con 5xx en 5 min | 🔴 Crítico | Revisar logs de error; posible bug nuevo |
| **Groq API caída** | Circuit breaker en estado OPEN por > 5 min | ⚠️ Aviso | El AI assistant no responde; funcionalidad degradada |
| **Latencia elevada** | p95 > 1 segundo por > 5 min | ⚠️ Aviso | Revisar queries lentas; posible índice faltante |
| **Memoria alta** | Heap JVM > 85% por > 10 min | ⚠️ Aviso | Posible memory leak; planear reinicio |
| **Intento de acceso no autorizado** | Más de 10 errores 403 del mismo IP en 1 min | ⚠️ Aviso | Posible escaneo o ataque; revisar logs |

---

### Herramientas recomendadas para NEVI

Para un contexto académico/MVP, el stack más accesible es:

| Señal | Herramienta | Costo |
|---|---|---|
| Logs | Loki + Grafana (self-hosted) | Gratis |
| Métricas | Prometheus + Grafana | Gratis |
| Trazas | Zipkin (self-hosted) | Gratis |
| Alertas | Grafana Alerts + email/Telegram | Gratis |

Alternativa gestionada (sin infraestructura propia):
| Señal | Herramienta | Costo |
|---|---|---|
| Logs + Métricas + Trazas | Grafana Cloud (free tier) | Gratis hasta 10K series |
| Logs | Papertrail | Gratis (100 MB/mes) |

Para NEVI académico: Grafana Cloud free tier es suficiente y no requiere configurar servidores adicionales.

---

*Documento de Semana 14 — Corte 3*  
*Universidad Jorge Tadeo Lozano — Arquitectura de Software*
