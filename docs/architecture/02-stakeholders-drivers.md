# 02 — Stakeholders, restricciones y drivers · NEVI

## Mapa de stakeholders

| Stakeholder | Interés o preocupación | Influencia |
|---|---|---|
| Estudiante | Acceder a contenido y chatear sin interrupciones | Alta |
| Profesor | Gestionar cursos y comunicarse con estudiantes | Alta |
| Equipo de desarrollo | Cambios seguros que no rompan el chat en tiempo real | Alta |
| Profesor / auditor del curso | Evidencia reproducible: dossier, pruebas, PR con doble control | Alta |
| Institución educativa | Privacidad de datos académicos y personales | Media |
| Operación futura | Escalar a más usuarios sin rediseñar el frontend | Media |

## Restricciones del sistema

### Técnicas

| ID | Restricción | Consecuencia arquitectónica |
|---|---|---|
| RT-01 | Frontend en React 18 + Vite + Tailwind | SPA; comunicación exclusiva por HTTP/JSON y WebSocket |
| RT-02 | Spring Boot 3.3.4 / Java 21 obligatorio | API en Java; anotaciones Jakarta EE 10 |
| RT-03 | PostgreSQL 16 | Esquema relacional; JPA + Hibernate |
| RT-04 | Docker Compose sin orquestador | Un solo nodo; sin HA ni balanceo |
| RT-05 | WebSocket / STOMP + SockJS | Chat en tiempo real; requiere sesión activa |

### Económicas

| ID | Restricción | Consecuencia arquitectónica |
|---|---|---|
| RE-01 | Sin presupuesto de nube | Docker local; sin CDN ni almacenamiento externo |
| RE-02 | Herramientas de código abierto | Spring Boot, React, PostgreSQL; sin licencias comerciales |

### Organizacionales

| ID | Restricción | Consecuencia arquitectónica |
|---|---|---|
| RO-01 | Evidencia mediante PR por integrante | Rama obligatoria; sin push directo a `main` |
| RO-02 | Entregas semanales con fecha fija | Se prioriza funcionalidad comprobable sobre características extra |
| RO-03 | JWT secreto no debe estar en producción | `JWT_SECRET` debe rotarse; no hardcodear en código |

## Drivers arquitectónicos

### DRIVER-01 · Seguridad 🔴 Crítica

**Necesidad:** Las conversaciones entre estudiantes y profesores contienen
información académica privada. El token JWT no debe ser accesible a
scripts de terceros.

**Evidencia en el código:**

```java
// El token se guarda en localStorage del navegador
// Clave: nevi_token — vulnerable a XSS
localStorage.setItem('nevi_token', response.token);
```

```yaml
# application.yml — secreto hardcodeado con valor por defecto inseguro
nevi:
  jwt:
    secret: ${JWT_SECRET:nevi-super-secret-key-change-in-production-2024}
```

**Riesgo activo (R-02):** cualquier script inyectado en la página puede
leer `localStorage` y robar el token JWT.

---

### DRIVER-02 · Modificabilidad 🔴 Crítica

**Necesidad:** Cambios en la API no deben obligar a redesplegar el
frontend manualmente ni romper el chat WebSocket.

**Evidencia en el código:**

```java
// AuthController.java — endpoints versionados bajo /api/auth
@RequestMapping("/api/auth")
```

Los endpoints REST y WebSocket están desacoplados del frontend mediante
contratos HTTP/JSON estables.

---

### DRIVER-03 · Disponibilidad 🟡 Moderada

**Necesidad:** El sistema debe responder sin interrupciones durante clases
y sesiones de chat activas.

**Evidencia en el código:**

```yaml
# application.yml — pool de conexiones limitado
hikari:
  maximum-pool-size: 10   # riesgo de agotamiento bajo carga
```

**Riesgo activo (R-01):** con 10 conexiones máximas, picos de tráfico
pueden agotar el pool y generar errores 500 en cascada.

---

### DRIVER-04 · Rendimiento 🟡 Moderado

**Necesidad:** Los mensajes de chat deben entregarse en tiempo real sin
latencias perceptibles para los participantes.

**Evidencia:** WebSocket + STOMP sobre SockJS garantiza entrega push sin
polling HTTP. La latencia depende de la conexión del cliente.

---

### DRIVER-05 · Testeabilidad 🔴 Crítica

**Necesidad:** El Checkpoint Hito 5 exige que el sistema levante en 2
comandos y que las pruebas automatizadas pasen sin intervención manual.

**Evidencia:** el backend no tiene pruebas de integración activas. Cualquier
cambio en `AuthController` o `ChatController` puede romper el flujo sin
detección en CI.

**Riesgo activo (R-04):** bloquea el Checkpoint Hito 5 hasta que se
agregue al menos una prueba de integración de autenticación.
