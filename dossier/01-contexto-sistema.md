# 01 — Contexto del sistema

Estado: **sistema base reescrito desde cero y ejecutable localmente con Docker Compose**.

Categoría confirmada por el profesor: **Mensajería y mesa de ayuda**.

## Sistema base 

NEVI (Network Of Virtual Interaction) es una reescritura completa (frontend +
backend + base de datos), no la adopción de un proyecto existente. Es una
plataforma educativa de comunicación en tiempo real asistida por IA: los
profesores crean grupos y actividades, los estudiantes se unen con un código,
chatean en tiempo real y reciben apoyo de un asistente de IA (explicaciones,
quizzes, retroalimentación, rúbricas).

## Actores y flujos

| Actor | Objetivo | Flujo relevante |
| --- | --- | --- |
| Estudiante | Participar en grupos y actividades, recibir apoyo de IA | Inicia sesión, se une a un grupo con código, abre el chat, consulta "mis grupos" en el Dashboard |
| Profesor | Crear y administrar grupos/actividades | Inicia sesión, crea grupos, publica actividades, revisa avance |
| Equipo de desarrollo | Mantener y medir el sistema | Ejecuta el stack local, corre pruebas de carga, documenta decisiones |
| Profesor/auditor del curso | Evaluar decisiones y evidencia | Revisa PR, dossier, condiciones, datos y resultados |

## Contexto y límites

El sistema medido se ejecuta localmente mediante Docker Compose:

```text
React (Vite) + Nginx
        │ HTTP/JSON (REST) + JWT Bearer
        │ WebSocket (chat en tiempo real)
        ▼
Spring Boot 3.3.4 (API REST + WebSocket)
        │ JDBC (Hibernate, ddl-auto: update)
        ▼
PostgreSQL 16
```

El frontend también llama **directamente** a la API de Groq (asistente de IA)
sin pasar por el backend — la variable `VITE_GROQ_API_KEY` se inyecta como
build-arg del contenedor `frontend`. Esto queda registrado aquí porque tiene
implicaciones de seguridad que se retoman en `02-stakeholders-drivers.md`.

Para el escenario de "listar mis grupos" (ver hipótesis del dolor), backend y
PostgreSQL comparten el mismo equipo físico mediante Docker Desktop. La
medición cubre el trayecto API + base de datos; no incluye la red del
navegador ni el renderizado de React.

Fuera del alcance de esta entrega: despliegue público, alta disponibilidad,
balanceo de carga y almacenamiento administrado.

## Línea base operativa

Arranque:

```bash
docker compose up --build -d
curl http://localhost:8080/actuator/health
```

**Pendiente detectado (bloqueante para el checkpoint):** el healthcheck del
backend en `docker-compose.yml` apunta a `/actuator/health`, pero
`spring-boot-starter-actuator` **no está** en `backend/pom.xml`. El endpoint
no existe todavía, así que el healthcheck fallará hasta agregar la
dependencia. Se deja registrado aquí como hallazgo de la semana 1/2, a
corregir antes de dar por cumplida la "base ejecutable".

Verificación (a implementar — todavía no existe carpeta de tests):

```bash
./mvnw test
```

## Restricciones de contexto

- Presupuesto: herramientas y servicios gratuitos para esta entrega.
- Infraestructura: un computador con Docker Desktop; sin nube obligatoria.
- Tiempo: entregas semanales del curso con checkpoints fijos.
- Tecnología: Spring Boot 3.3.4 / Java, PostgreSQL 16 y React/Vite ya
  decididos; no se cambian a mitad de curso.
- Datos: únicamente semilla sintética con distribución 80/20 prevista para la
  línea base (semana 4); no se usan datos reales de estudiantes.
- Seguridad: pendiente por resolver — la llave de Groq viaja embebida en el
  bundle del frontend (ver riesgo R-02 en `02-stakeholders-drivers.md`).
