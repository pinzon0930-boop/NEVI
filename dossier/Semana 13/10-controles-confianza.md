# Controles Arquitectónicos y Límites de Confianza — NEVI

**Proyecto:** NEVI — Network of Virtual Interaction  
**Semana:** 13 · Corte 3  
**Tiempo estimado:** 90 min  
**Autor:** Nixson Camilo Pinzón Muñoz  

---

## ¿Qué son los límites de confianza y por qué importan?

Un límite de confianza es el punto donde una petición pasa de una zona a otra del sistema. Cuando un request cruza ese límite, el sistema tiene que tomar una decisión: ¿confío en esto o lo verifico?

La trampa más común en arquitectura es asumir que si una petición llegó hasta aquí, ya fue verificada en algún paso anterior. Eso es confianza implícita, y es exactamente lo que el modelo **Zero Trust** busca eliminar.

La regla central de Zero Trust es sencilla:  
**"Atravesar una frontera no concede automáticamente permiso para atravesar las demás."**

Estar dentro de la red interna (`nevi_net`) no significa que el request es legítimo. Que el JWT sea válido no significa que el usuario puede acceder a ese recurso específico. Cada frontera verifica por su cuenta.

---

## Los 4 límites de confianza de NEVI

NEVI tiene cuatro fronteras donde el nivel de confianza cambia:

```
[ Internet ]
      │  HTTPS
      ▼
┌─────────────────┐
│   Nginx         │  ← Frontera 1: Internet → Nginx
│  (proxy + TLS)  │
└────────┬────────┘
         │  HTTP interno
         ▼
┌─────────────────┐
│  Spring Boot    │  ← Frontera 2: Nginx → Spring Boot API
│  (API + JWT)    │
└────┬───────┬────┘
     │       │  HTTP externo
     ▼       ▼
┌──────────┐ ┌────────────┐
│PostgreSQL│ │  Groq API  │  ← Fronteras 3 y 4
└──────────┘ └────────────┘
```

---

## Frontera 1: Internet → Nginx

**¿Qué cruza esta frontera?**  
Todo el tráfico que viene de los usuarios — requests HTTP/HTTPS, conexiones WebSocket, archivos estáticos del frontend.

**¿Qué controles aplican aquí?**

| Control | Estado | Descripción |
|---|---|---|
| HTTPS obligatorio (TLS 1.2+) | ⚠️ Parcial | Configurado en desarrollo; pendiente certificado válido en producción |
| Rate limiting por IP | ❌ Pendiente | Nginx puede limitar con `limit_req_zone`; no está configurado |
| Bloqueo de métodos HTTP no usados | ❌ Pendiente | Solo se necesitan GET, POST, PUT, DELETE, OPTIONS |
| Cabeceras de seguridad HTTP | ❌ Pendiente | `X-Content-Type-Options`, `X-Frame-Options`, `Strict-Transport-Security` |

**¿Qué NO debe pasar en esta frontera?**  
Nginx no verifica el JWT — eso no es su responsabilidad. Su trabajo es asegurarse de que el tráfico llega en HTTPS y de que no hay abuso de tasa de requests.

---

## Frontera 2: Nginx → Spring Boot API

**¿Qué cruza esta frontera?**  
Requests HTTP del proxy con el JWT en el header `Authorization: Bearer <token>`.

**¿Qué controles aplican aquí?**

| Control | Estado | Descripción |
|---|---|---|
| Verificación de JWT en cada request | ✅ Hecho | `JwtAuthFilter` valida firma, expiración e issuer |
| Verificación de rol antes de acceder a endpoints admin | ❌ Pendiente | `@PreAuthorize("hasRole('ADMIN')")` puede faltar en algunos endpoints |
| Verificación de ownership antes de operar sobre un recurso | ❌ Pendiente | El `groupId`/`submissionId` no se valida contra el userId del JWT |
| JWT con expiración corta (≤15 min) | ❌ Pendiente | Expiración probablemente mayor a 15 minutos |

**El error clásico en esta frontera:**  
Confundir autenticación con autorización.

- **Autenticación**: "¿Este token es válido?" — lo responde `JwtAuthFilter`
- **Autorización**: "¿Este usuario puede hacer esta operación sobre este recurso?" — lo debe responder el servicio, no el filtro

Un JWT válido (`401` resuelto) no implica permiso para cualquier operación (`403` puede seguir aplicando).

**Ejemplo concreto — lo que debe pasar:**
```
GET /api/groups/42/activities
Authorization: Bearer eyJ...

JwtAuthFilter:
  → Token válido ✅
  → userId del token: "usuario-123"

GroupService.getActivities(42):
  → ¿grupo 42 pertenece a usuario-123? ✅
  → Si no: 403 Forbidden (aunque el token sea válido)
```

---

## Frontera 3: Spring Boot → PostgreSQL

**¿Qué cruza esta frontera?**  
Queries SQL ejecutadas por JPA/Hibernate contra la base de datos.

**¿Qué controles aplican aquí?**

| Control | Estado | Descripción |
|---|---|---|
| Credenciales de BD en variables de entorno | ✅ Hecho | `${DB_PASSWORD}` en `.env`, nunca en el código |
| Rol `nevi_app` con permisos mínimos | ❌ Pendiente | Se conecta con un rol de alta privilegio; falta crear el rol restringido |
| Queries parametrizadas (sin SQL concatenado) | ✅ Hecho | JPA/Hibernate usa prepared statements por defecto |
| Red interna (`nevi_net`): Spring Boot puede alcanzar PostgreSQL | ✅ Hecho | Comunicación interna de Docker Compose |

**El riesgo de la confianza implícita aquí:**  
Si alguien obtiene las credenciales del servicio (por ejemplo, robándolas del contenedor), y el rol de BD tiene permisos de `DROP TABLE` o `TRUNCATE`, puede borrar toda la base de datos. El principio de mínimo privilegio limita el daño: si el rol solo tiene SELECT/INSERT/UPDATE, el impacto máximo es corrupción de datos, no pérdida total.

**Lo que se debe implementar:**
```sql
-- Crear rol con permisos mínimos
CREATE ROLE nevi_app LOGIN PASSWORD '${DB_PASSWORD}';
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO nevi_app;
-- Sin: DELETE (excepto donde sea necesario), DROP, TRUNCATE, ALTER
```

**Importante:** Estar en `nevi_net` no es suficiente para confiar en la conexión. El rol de BD debe verificar las credenciales igual que si viniera de internet.

---

## Frontera 4: Spring Boot → Groq API (externa)

**¿Qué cruza esta frontera?**  
Requests HTTPS al endpoint de Groq con la API key en el header.

**¿Qué controles aplican aquí?**

| Control | Estado | Descripción |
|---|---|---|
| API key en variable de entorno | ✅ Hecho | `${VITE_GROQ_API_KEY}` en `.env`, nunca en el código |
| Circuit breaker para evitar cascadas de fallo | ❌ Pendiente | Si Groq falla, puede saturar los threads del pool |
| Timeout configurado en el cliente HTTP | ❌ Pendiente | Sin timeout, un request lento bloquea un thread indefinidamente |
| El usuario no controla la URL de Groq | ✅ Hecho | El backend construye la URL internamente; no hay SSRF posible |

**El riesgo aquí:**  
Si Groq tiene latencia alta o falla, y no hay circuit breaker, cada request al endpoint de AI queda esperando. Con suficientes requests simultáneos, se agotan los threads del servidor y la API completa deja de responder — no por un ataque, sino por una dependencia externa lenta.

---

## Resumen: qué verifica cada frontera

| Frontera | ¿Qué verifica? | ¿Estado actual? |
|---|---|---|
| Internet → Nginx | TLS válido, rate limiting | ⚠️ Parcial |
| Nginx → Spring Boot | JWT (firma + expiración + issuer) | ✅ Hecho |
| Spring Boot (servicio) | Rol del usuario + ownership del recurso | ❌ Pendiente |
| Spring Boot → PostgreSQL | Credenciales + permisos mínimos del rol | ⚠️ Parcial |
| Spring Boot → Groq | API key + circuit breaker + timeout | ⚠️ Parcial |

---

## El principio que une todo

Cada componente en NEVI debe comportarse como si los demás pudieran estar comprometidos. El Spring Boot no confía ciegamente en Nginx. La base de datos no confía ciegamente en Spring Boot. Groq no sabe nada de NEVI.

Esto no significa que el sistema sea paranoico o ineficiente — significa que las verificaciones están en el lugar correcto, y si un componente falla o es comprometido, el daño está contenido.

---

*Documento de Semana 13 — Corte 3*  
*Universidad Jorge Tadeo Lozano — Arquitectura de Software*
