# 09 — Inventario de riesgos · NEVI

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

**Mitigación propuesta:** Aumentar `maximum-pool-size` a 20–30; ajustar
`connection-timeout` a 3000 ms para fallar rápido.

---

## R-02 · JWT almacenado en localStorage — superficie XSS

| Campo | Detalle |
|---|---|
| **Probabilidad** | Media |
| **Impacto** | Muy alto |
| **Nivel** | 🔴 Crítico |

**Evidencia en el código:**

```javascript
localStorage.setItem('nevi_token', response.data.token);
```

**Mitigación propuesta:** Migrar el token a una cookie `HttpOnly; Secure; SameSite=Strict`.

---

## R-03 · `ddl-auto: update` — riesgo de corrupción de esquema

| Campo | Detalle |
|---|---|
| **Probabilidad** | Media |
| **Impacto** | Alto |
| **Nivel** | 🔴 Crítico |

**Evidencia en el código:**

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

**Mitigación propuesta:** Cambiar a `ddl-auto: validate` y usar Flyway o Liquibase.

---

## R-04 · Sin pruebas de integración en el backend

| Campo | Detalle |
|---|---|
| **Probabilidad** | Alta |
| **Impacto** | Alto |
| **Nivel** | 🔴 Crítico — bloquea Checkpoint Hito 5 |

**Evidencia en el código:**

```bash
find backend/src/test -name "*.java" | wc -l
# → 0
```

**Mitigación propuesta:** Crear prueba `@SpringBootTest` con `MockMvc` para registro → login → recurso protegido.

---

## R-05 · CORS con `allowed-origins: "*"`

| Campo | Detalle |
|---|---|
| **Probabilidad** | Media |
| **Impacto** | Medio |
| **Nivel** | 🟡 Moderado |

**Evidencia en el código:**

```yaml
web:
  cors:
    allowed-origins: "*"
```

**Mitigación propuesta:** Restringir al dominio específico del frontend.
