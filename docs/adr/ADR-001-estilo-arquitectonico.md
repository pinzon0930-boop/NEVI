# ADR-001 — Estilo arquitectónico: Monolito modular vs. Microservicios

**Estado:** Confirmada
**Fecha:** 2026-09-18
**Autores:** Equipo NOVI (Nixson Pinzón, Roberto Hernández, Camilo Flórez, Michael Lopez)
**Revisores:** Mini-comité técnico — semana 8

---

## Contexto

NOVI es una plataforma educativa con backend Spring Boot (paquete `com.nevi`), frontend React y
base de datos PostgreSQL, ejecutada con Docker Compose en un único nodo.
El equipo de 4 personas trabaja bajo restricciones del curso: entregas
semanales, presupuesto cero para infraestructura de nube, y sin la opción
de cambiar el stack tecnológico a mitad de desarrollo.

Los drivers que más influyen en esta decisión son:

- **DRIVER-02 (Modificabilidad):** agregar nuevas funciones (quiz, rúbrica,
  estadísticas) sin romper el contrato existente.
- **DRIVER-03 (Disponibilidad):** el sistema debe responder sin caídas
  durante sesiones de clase activas.
- **DRIVER-05 (Testeabilidad):** el CI debe detectar regresiones antes de
  llegar a `main`.

La pregunta a responder es: ¿qué estilo arquitectónico conviene adoptar
para el backend dado el contexto actual?

---

## Alternativas evaluadas

### Alternativa A — Monolito modular (Spring Boot, paquetes por dominio)

El backend es una única aplicación Spring Boot organizada en paquetes por
dominio: `auth`, `grupos`, `actividades`, `mensajes`. Cada dominio expone
controladores, servicios y repositorios propios. La comunicación entre
dominios ocurre mediante llamadas directas a métodos Java (sin red).

**Costos:**
- Acoplamiento en tiempo de compilación: un cambio en `AuthService` puede
  afectar a `GrupoService` si comparten la entidad `User`.
- Un único artefacto desplegable: si el backend falla, caen todos los
  dominios simultáneamente.
- Escalado horizontal requiere replicar toda la aplicación, no solo el
  módulo más cargado.

**Beneficios:**
- Despliegue simple: `docker compose up` levanta todo.
- Depuración sencilla: stack trace completo sin saltar entre servicios.
- Sin latencia de red entre componentes internos.
- Equipo pequeño (4 personas) puede mantener un único codebase sin
  necesidad de contratos versionados entre servicios.
- Compatible con las restricciones de infraestructura (Docker Compose,
  sin orquestador).

### Alternativa B — Microservicios (un servicio por dominio)

Cada dominio (auth, grupos, actividades, mensajes) se convierte en un
servicio Spring Boot independiente con su propia base de datos. Los
servicios se comunican por HTTP o mensajería (RabbitMQ/Kafka).

**Costos:**
- Infraestructura adicional: service mesh, API gateway, registro de
  servicios (Eureka/Consul), o al menos un gateway Nginx.
- Complejidad operativa inasumible en presupuesto cero y en una sola
  máquina de desarrollo.
- Comunicación de red entre servicios añade latencia y puntos de fallo.
- El equipo necesitaría gestionar 4+ repositorios, 4+ pipelines de CI y
  contratos de API versionados — coste organizacional muy alto para S7.
- Las pruebas de integración se vuelven más complejas: requieren levantar
  múltiples servicios en el CI.

**Beneficios:**
- Escalado independiente por dominio.
- Despliegue independiente por servicio.
- Aislamiento de fallos: si el servicio de actividades cae, el chat sigue.

---

## Decisión

**Se adopta la Alternativa A: Monolito modular.**

La elección se basa en el análisis de costos y beneficios en el contexto
actual:

1. Las restricciones de infraestructura (Docker Compose, sin nube,
   presupuesto cero) hacen inviable la Alternativa B sin herramientas
   adicionales que el curso no contempla.
2. El equipo de 4 personas y el ritmo de entregas semanales favorecen un
   único codebase comprensible.
3. Los drivers de modificabilidad (DRIVER-02) y testeabilidad (DRIVER-05)
   se satisfacen mejor con un monolito donde los cambios son visibles en
   un solo lugar y el CI puede ejecutar pruebas de integración levantando
   un único contexto Spring.
4. El riesgo de acoplamiento (costo principal de la Alternativa A) se
   mitiga con la organización por paquetes de dominio ya presente en el
   código (`com.nevi.controller`, `com.nevi.service`, etc.).

---

## Consecuencias

### Positivas
- Simplicidad operativa mantenida durante el curso.
- El pipeline de CI solo necesita levantar un contexto de Spring Boot +
  PostgreSQL (Testcontainers o H2).
- Los nuevos endpoints (escenario ESC-04) siguen el mismo patrón MVC sin
  complejidad adicional.

### Negativas
- Si NOVI escala a producción real, la ausencia de límites de despliegue
  entre dominios dificultará escalar solo el chat o solo las actividades.
- Un bug en el módulo de `auth` puede causar un reinicio completo del
  backend, afectando temporalmente el chat y las actividades.

### Reversibilidad
Esta decisión es **revisable** si en etapas posteriores del curso se
introduce un requisito de escalado o de aislamiento de fallos que el
monolito no pueda satisfacer. La condición de revisión sería:

> "Si la latencia p95 de cualquier endpoint supera el umbral de QA-02
> bajo la carga objetivo, O si el equipo supera los 10 integrantes,
> se evaluará la extracción del módulo más cargado como servicio separado."

### Supuestos
- El sistema se ejecuta en una sola máquina durante el ciclo del curso.
- El número de usuarios concurrentes no superará los 30 durante las
  evaluaciones del curso.
- Los integrantes del equipo mantienen acceso al mismo repositorio y
  rama de trabajo.

---

## Registro de alternativas descartadas

| Alternativa | Razón de descarte |
|---|---|
| Microservicios | Inviable con presupuesto cero y Docker Compose sin orquestador |
| Arquitectura serverless | Requiere proveedor cloud; fuera del alcance del curso |
| Backend-for-Frontend (BFF) | Añade una capa sin beneficio claro en el contexto actual |
