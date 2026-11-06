# ADR 5 — Estrategia de JWT y Refresh Tokens en NEVI

**Estado:** Propuesto  
**Fecha:** 2026-11-06  
**Autor:** Nixson Camilo Pinzón Muñoz  
**Proyecto:** NEVI — Network of Virtual Interaction  

---

## Contexto

NEVI usa JWT (JSON Web Tokens) para autenticar a los usuarios desde el primer corte. La implementación actual tiene un problema de seguridad identificado en el modelado de amenazas de la Semana 13:

**El problema concreto:**
- Los access tokens duran demasiado tiempo (probablemente 24 horas por el default de Spring Boot)
- No hay mecanismo de logout que invalide el token — si alguien roba un JWT, lo puede usar hasta que expire
- Si el usuario cambia su contraseña, el token antiguo sigue siendo válido

Esto se mapeó como amenaza **S (Spoofing)** en STRIDE con prioridad alta (puntaje DREAD: 12/15).

**Lo que necesitamos:**
1. Tokens de corta duración para limitar la ventana de ataque
2. Un mecanismo para invalidar tokens en logout sin agregar una base de datos de sesiones muy compleja
3. Una solución que funcione con la arquitectura actual de Spring Boot + PostgreSQL sin agregar más servicios externos

---

## Opciones evaluadas

### Opción 1 — Access token corto + refresh token en BD

**¿Cómo funciona?**
- Access token: JWT firmado, expira en 15 minutos
- Refresh token: UUID aleatorio, guardado en tabla `refresh_tokens` de PostgreSQL, expira en 7 días
- Logout: marcar el refresh token como revocado en la BD
- Renovación: el cliente envía el refresh token, el servidor verifica que no esté revocado y devuelve un nuevo access token

```
Usuario → POST /api/auth/login
  ← access_token (JWT, 15 min)
  ← refresh_token (UUID, 7 días, guardado en BD)

[15 minutos después...]
Usuario → POST /api/auth/refresh { refresh_token: "uuid..." }
  ← access_token nuevo (JWT, 15 min)
  ← refresh_token nuevo (rotación — el anterior queda inválido)

Usuario → POST /api/auth/logout { refresh_token: "uuid..." }
  ← 200 OK (refresh token marcado como revocado en BD)
```

**Ventajas:**
- Logout real que invalida la sesión
- Rotación del refresh token detecta reutilización (si alguien roba el refresh token y lo usa, el usuario original ve un 401 en el siguiente refresh)
- PostgreSQL ya existe — no hay que agregar Redis ni otro servicio
- Implementación conocida y bien documentada en Spring Security

**Desventajas:**
- Cada renovación requiere una consulta a la BD
- Hay que limpiar periódicamente los refresh tokens expirados
- Más código para implementar que las opciones más simples

---

### Opción 2 — Token de larga duración con blacklist en memoria

**¿Cómo funciona?**
- Access token: JWT firmado, expira en 1 hora
- Logout: agregar el JTI (JWT ID) a un Set en memoria de Spring Boot
- Verificación: en cada request, revisar si el JTI está en el Set

**Ventajas:**
- Simple de implementar (un Set en memoria)
- Sin consultas a BD en cada verificación

**Desventajas:**
- Si el servidor se reinicia, la blacklist se pierde — todos los tokens "revocados" vuelven a ser válidos
- Si hay múltiples instancias (escalabilidad), la blacklist no se comparte entre instancias
- El token sigue siendo de larga duración (1 hora de ventana de ataque)
- No es una solución real para producción

---

### Opción 3 — Solo reducir expiración, sin refresh tokens

**¿Cómo funciona?**
- Access token: JWT firmado, expira en 15 minutos
- Sin refresh tokens — el usuario tiene que volver a hacer login cada 15 minutos
- Logout: simplemente eliminar el token del cliente

**Ventajas:**
- Implementación mínima — solo cambiar un parámetro en `application.yml`
- Sin cambios al flujo de autenticación

**Desventajas:**
- Experiencia de usuario muy mala — tener que hacer login cada 15 minutos en un entorno educativo es inaceptable
- El logout sigue sin invalidar el token (aunque dura poco, el token sigue siendo válido hasta expirar)

---

## Decisión

**Adoptamos la Opción 1 — Access token corto (15 min) + refresh token en PostgreSQL con rotación.**

---

## Justificación

La Opción 3 no es viable por experiencia de usuario — interrumpir la sesión de un estudiante cada 15 minutos es inaceptable. La Opción 2 tiene una vulnerabilidad fundamental (blacklist se pierde al reiniciar). La Opción 1 es la única que resuelve el problema correctamente.

Usar PostgreSQL para los refresh tokens es la decisión correcta en este contexto porque:
- Ya existe en el stack — no hay que agregar Redis ni otro servicio
- Para el volumen académico de NEVI (decenas de usuarios, no millones), la carga adicional es negligible
- La rotación de refresh tokens detecta usos sospechosos automáticamente

---

## Consecuencias

### Qué cambia
- Agregar tabla `refresh_tokens` en PostgreSQL: `(id UUID, user_id BIGINT, token_hash VARCHAR, expires_at TIMESTAMP, revoked BOOLEAN)`
- Nuevo endpoint: `POST /api/auth/refresh`
- Modificar `POST /api/auth/logout` para revocar el refresh token
- El cliente (React) debe almacenar el refresh token en memoria (no en localStorage) y el access token en memoria también — nunca en localStorage por riesgo de XSS
- Configurar `spring.security.jwt.expiration=900000` (15 minutos en ms)

### Qué NO cambia
- El formato del JWT (mismo payload, misma firma HS256)
- `JwtAuthFilter` — sigue verificando el access token en cada request
- El flujo de autenticación inicial (POST /api/auth/login)

### Riesgos residuales
- Si el refresh token es robado (por XSS que accede a memoria), el atacante puede renovar el access token. Mitigación: tokens en memoria (no localStorage), HTTPS obligatorio, Content Security Policy en Nginx.
- La tabla `refresh_tokens` puede crecer — necesita un job de limpieza periódica para registros expirados.

---

## Conexión con otros ADRs

- ADR 1 (si existe): selección de Spring Boot como framework — implica que la implementación usa Spring Security
- ADR 5 (este): estrategia de autenticación y sesiones
- ADR futuro: observabilidad — las renovaciones de tokens deben loggearse con Trace ID

---

*ADR 5 — Semana 14 · Corte 3*  
*Universidad Jorge Tadeo Lozano — Arquitectura de Software*
