# Modelado de Amenazas — NEVI

**Proyecto:** NEVI — Network of Virtual Interaction  
**Semana:** 13 · Corte 3  
**Tiempo estimado:** 120 min  
**Autor:** Nixson Camilo Pinzón Muñoz  

---

## ¿Qué es modelar amenazas y por qué lo hacemos?

Modelar amenazas es básicamente pensar como un atacante antes de que el atacante llegue. En lugar de esperar a que algo falle en producción, revisamos el sistema y nos preguntamos: ¿qué podría salir mal aquí? ¿quién podría abusar de esto? ¿qué datos están en riesgo?

Para NEVI usamos dos herramientas juntas:
- **STRIDE**: clasifica las amenazas en 6 categorías (Spoofing, Tampering, Repudiation, Information Disclosure, Denial of Service, Elevation of Privilege)
- **OWASP Top Ten**: lista de las 10 vulnerabilidades más comunes en aplicaciones web

---

## El sistema que analizamos

NEVI tiene estos componentes principales:

```
Usuario
  └── Nginx (HTTPS, proxy inverso)
        └── Spring Boot API (Java 21, JWT, WebSocket/STOMP)
              ├── PostgreSQL 16 (users, groups, activities, messages, submissions)
              └── Groq API externa (llama3-8b-8192)
```

Todos los servicios corren en Docker Compose dentro de la red `nevi_net`.

Tablas en la BD: `users`, `groups`, `group_members`, `activities`, `activity_submissions`, `messages`.

---

## STRIDE aplicado a NEVI

### S — Spoofing (Suplantación)

**¿Qué puede pasar?**  
Un atacante consigue un JWT válido (por XSS, ingeniería social o credencial filtrada) y lo usa para hacerse pasar por otro usuario. Si el token dura muchas horas, tiene mucho tiempo para actuar sin que nadie lo note.

**Componente afectado:** Spring Boot API — `/api/auth/**`

**Escenario concreto:**
1. El usuario A tiene un token válido por 24 horas
2. El atacante intercepta el token (o lo obtiene por otra vía)
3. Usa ese token para hacer requests como si fuera el usuario A
4. El token no expira hasta las 24 horas → ventana de ataque enorme

**Control propuesto:**
- Access token con expiración corta (máximo 15 minutos)
- `${JWT_SECRET}` en variable de entorno, no en el código
- `JwtAuthFilter` valida firma, expiración e issuer en cada request
- Refresh token con rotación

**Riesgo residual:** Si el token ya fue robado, sigue siendo válido hasta su expiración. Sin blacklist, el logout no lo invalida de inmediato.

---

### T — Tampering (Manipulación de datos)

**¿Qué puede pasar?**  
Un estudiante envía un request a `PUT /api/submissions/42` cambiando el ID para modificar la entrega de otro compañero. O alguien con acceso directo a la BD modifica calificaciones en `activity_submissions`.

**Componente afectado:** PostgreSQL — tablas `activities`, `activity_submissions`

**Escenario concreto:**
1. El estudiante hace `PUT /api/submissions/42` con su propio token
2. El ID 42 pertenece a otro estudiante
3. Si no hay verificación de ownership, la API acepta el cambio
4. Las calificaciones del otro estudiante quedan modificadas

**Control propuesto:**
- Rol `nevi_app` en PostgreSQL con solo SELECT, INSERT, UPDATE (sin DELETE, DROP, ALTER)
- JPA/Hibernate con queries parametrizadas (sin SQL concatenado)
- Verificar que el `submissionId` pertenece al usuario del JWT antes de cualquier UPDATE

**Riesgo residual:** Un bug en la verificación de ownership puede dejar pasar el ataque. Logs sin Trace ID dificultan rastrear quién hizo el cambio.

---

### R — Repudiation (Repudio)

**¿Qué puede pasar?**  
Un usuario niega haber enviado una actividad o haber accedido a datos de otro grupo. Sin logs estructurados con un identificador único por operación, no hay forma de demostrarlo.

**Componente afectado:** Spring Boot API — capa de logging

**Escenario concreto:**
1. El estudiante B accede a actividades del grupo A por error (o a propósito)
2. Se reporta el incidente
3. Los logs dicen: `INFO - Actividad consultada` sin usuario, sin timestamp preciso, sin ID de operación
4. No hay evidencia clara para resolver el caso

**Control propuesto:**
- Trace ID (UUID) generado por request y propagado en todos los logs de esa operación
- Logs en JSON: `traceId`, `userId`, `action`, `resource`, `timestamp`
- `created_at` y `updated_at` gestionados por la BD (no por la app)

**Riesgo residual:** Sin firma criptográfica en los logs, alguien con acceso al sistema de logs podría alterarlos.

---

### I — Information Disclosure (Exposición de información)

**¿Qué puede pasar?**  
El código hace `log.info("Usuario creado: {}", user)` y el objeto `user` incluye `passwordHash`. Ese hash queda en los logs y puede ser atacado offline con herramientas de fuerza bruta.

**Componente afectado:** Spring Boot API — logging; tabla `users` en PostgreSQL

**Escenario concreto:**
```java
// Código problemático (Historia H10 del curso)
log.info("Nuevo usuario registrado: {}", user); 
// El log imprime: {..., "passwordHash": "$argon2id$v=19$...", "email": "..."}
```

El hash de Argon2id es robusto, pero si alguien tiene acceso a los logs tiene el hash para atacarlo offline.

**Control propuesto:**
- Nunca serializar objetos completos en logs
- Nivel de log en producción: INFO/WARN/ERROR (no DEBUG)
- Filtro de redacción para campos sensibles: `passwordHash`, tokens, emails

**Riesgo residual:** Logs históricos ya pueden tener datos sensibles. La lista de campos a redactar puede quedar desactualizada.

---

### D — Denial of Service (Denegación de servicio)

**¿Qué puede pasar?**  
Muchos usuarios hacen requests simultáneos a endpoints que consultan la BD. HikariCP llega a su límite (en la Historia H07 vimos 14/20 conexiones ocupadas). Los nuevos requests se quedan esperando, la latencia se dispara y el sistema empieza a fallar.

**Componente afectado:** Spring Boot API — HikariCP connection pool

**Escenario concreto:**
1. 50 usuarios simultáneos hacen requests a `GET /api/activities`
2. HikariCP tiene 20 conexiones máximas → 14 ocupadas (70%)
3. Los siguientes requests esperan una conexión libre → timeout de HikariCP
4. La API empieza a devolver `503 Service Unavailable`

**Control propuesto:**
- Rate limiting por IP en Nginx
- HikariCP con `connectionTimeout`, `maxLifetime` y `maximumPoolSize` ajustados
- Circuit breaker (Resilience4j) en llamadas a Groq API para evitar que timeouts externos colapsen el pool

**Riesgo residual:** Un pico coordinado puede saturar el pool aun con límites. Sin auto-scaling, la capacidad es fija.

---

### E — Elevation of Privilege (Elevación de privilegios)

**¿Qué puede pasar?**  
Un estudiante descubre el endpoint `/api/admin/users` (por explorar la API o ver el código en GitHub) y hace un request con su token válido. Si el endpoint solo verifica que hay un token pero no el rol, le devuelve la lista de todos los usuarios.

**Componente afectado:** Spring Boot API — endpoints `/api/admin/**`, `/api/groups/**`

**Escenario concreto:**
1. El estudiante inspecciona el código en GitHub y ve la ruta `/api/admin/users`
2. Tiene un JWT válido como STUDENT
3. Hace `GET /api/admin/users` con ese token
4. Si falta `@PreAuthorize("hasRole('ADMIN')")`, la API responde con la lista completa de usuarios

**Control propuesto:**
- `@PreAuthorize("hasRole('ADMIN')")` en todos los endpoints sensibles
- Verificar ownership en cada operación (el `groupId` debe pertenecer al usuario del JWT)
- Prueba negativa en CI: STUDENT → endpoint ADMIN → `403 Forbidden`

**Riesgo residual:** IDs predecibles (secuenciales) facilitan IDOR. Un bug de lógica en el filtro de ownership puede exponer datos de otros grupos.

---

## OWASP Top Ten aplicado a NEVI

| # | Categoría | ¿Aplica? | Nivel | Situación en NEVI |
|---|---|---|---|---|
| A01 | Broken Access Control | ✅ Sí | Alto | Falta ownership check en rutas de grupo/entrega; posible IDOR |
| A02 | Cryptographic Failures | ✅ Sí | Alto | `passwordHash` puede aparecer en logs; verificar que `${JWT_SECRET}` no esté hardcodeado |
| A03 | Injection | ⚠️ Parcial | Bajo | JPA usa prepared statements por defecto; riesgo solo si alguien usa SQL nativo sin parámetros |
| A04 | Insecure Design | ✅ Sí | Medio | La red `nevi_net` puede generar confianza implícita: "está en la red interna, es de confianza" |
| A05 | Security Misconfiguration | ✅ Sí | Medio | Puertos expuestos en docker-compose; revisar que credenciales no estén en el YAML |
| A06 | Vulnerable Components | ⚠️ Parcial | Bajo | Aplica a cualquier proyecto; bajo riesgo en contexto académico si se mantiene actualizado |
| A07 | Auth Failures | ✅ Sí | Alto | JWT probablemente con expiración larga; falta blacklist de tokens al hacer logout |
| A08 | Software Integrity | ✅ Sí | Medio | No hay SAST en el pipeline de CI todavía |
| A09 | Logging Failures | ✅ Sí | Alto | Logs sin Trace ID; `passwordHash` puede aparecer en logs (Historia H10) |
| A10 | SSRF | ❌ No aplica | — | Las llamadas a Groq API las construye el backend; el usuario no controla la URL |

### Las más urgentes para NEVI

1. **A01** — fácil de explotar, afecta datos académicos directamente
2. **A07** — un JWT de larga duración con secreto débil compromete toda la sesión
3. **A09** — sin logs estructurados con Trace ID es imposible investigar incidentes
4. **A02** — el `passwordHash` en logs puede parecer inofensivo hasta que alguien lo encuentra

---

*Documento de Semana 13 — Corte 3*  
*Universidad Jorge Tadeo Lozano — Arquitectura de Software*
