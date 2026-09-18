# C4 Nivel 1 — Diagrama de Contexto

## 1. Objetivo

El modelo C4 Nivel 1 representa el contexto general del sistema **NOVI
(Network Of Virtual Interaction)**, identificando los usuarios que interactúan
con la plataforma y los sistemas externos con los que NOVI establece
comunicación.

Esta vista proporciona una representación de alto nivel del sistema, sin
mostrar detalles internos de implementación como componentes frontend,
backend, bases de datos o protocolos de comunicación.

---

## 2. Propósito de la vista

Esta vista permite comprender el sistema NOVI desde una perspectiva general,
identificando los principales actores que utilizan la plataforma, las
funcionalidades que realizan y los servicios externos con los que el sistema
interactúa.

La representación permite establecer el límite del sistema antes de
profundizar en su arquitectura interna mediante los modelos C4 Nivel 2 y
Nivel 3.

En este nivel, NOVI se considera como un único sistema,
independientemente de las tecnologías utilizadas internamente.

---

## 3. Audiencia

Esta vista está dirigida principalmente a:

* Docentes y evaluadores.
* Arquitectos de software.
* Desarrolladores.
* Integrantes del equipo del proyecto.
* Personas interesadas en comprender el propósito y contexto general de NOVI.

---

# 4. Elementos del contexto

## 4.1 Estudiante

**Tipo:** Usuario de la plataforma.

El estudiante utiliza NOVI para participar en grupos académicos, consultar
actividades, realizar entregas, comunicarse mediante el chat grupal y utilizar
las herramientas de apoyo académico disponibles en la plataforma.

Entre sus principales interacciones se encuentran:

* Iniciar sesión en la plataforma.
* Unirse a grupos mediante código.
* Consultar actividades académicas.
* Realizar entregas.
* Participar en conversaciones grupales.
* Consultar al asistente académico.
* Utilizar herramientas de IA para facilitar la comprensión de actividades.

---

## 4.2 Profesor

**Tipo:** Usuario de la plataforma.

El profesor utiliza NOVI para administrar sus grupos y gestionar actividades
académicas, además de utilizar las herramientas de inteligencia artificial
disponibles para apoyar la creación y evaluación de contenidos.

Entre sus principales interacciones se encuentran:

* Iniciar sesión en la plataforma.
* Crear y administrar grupos.
* Consultar integrantes de los grupos.
* Crear actividades académicas.
* Generar actividades mediante IA.
* Generar quizzes.
* Generar rúbricas.
* Generar retroalimentación.
* Consultar información y estado de los grupos.
* Comunicarse con los estudiantes mediante el chat grupal.

Estas funcionalidades corresponden a las capacidades descritas actualmente
en el repositorio del proyecto.

---

## 4.3 NOVI

**Tipo:** Sistema principal.

**Nombre:** Network Of Virtual Interaction.

NOVI es una plataforma educativa orientada a la interacción entre estudiantes
y profesores.

El sistema proporciona funcionalidades relacionadas con:

* Gestión de usuarios y autenticación.
* Gestión de grupos académicos.
* Gestión de actividades.
* Entrega de actividades.
* Comunicación grupal en tiempo real.
* Herramientas de inteligencia artificial.
* Apoyo académico.
* Generación de contenido educativo.

En el Nivel 1, todas las funcionalidades internas de NOVI se representan como
parte de un único sistema, sin detallar su implementación tecnológica.

La aplicación se ejecuta localmente mediante una arquitectura compuesta por
frontend, backend y PostgreSQL, utilizando Docker Compose para levantar los
servicios.

---

## 4.4 Groq API

**Tipo:** Sistema externo.

Groq API es el servicio externo utilizado por NOVI para proporcionar las
capacidades de inteligencia artificial de la plataforma.

NOVI utiliza este servicio para funcionalidades como:

* Asistencia académica.
* Generación de actividades.
* Generación de quizzes.
* Generación de rúbricas.
* Generación de retroalimentación.
* Explicación simplificada de actividades.
* Generación de resúmenes de grupos.

La integración con Groq se encuentra implementada principalmente mediante:

```text
frontend/src/services/groq.js
```

Este servicio centraliza las solicitudes relacionadas con las funcionalidades
de inteligencia artificial.

> **Nota:** Aunque la aplicación, el backend y la base de datos se ejecutan
> localmente, Groq API continúa siendo un servicio externo. Por esta razón,
> debe mantenerse en el diagrama de contexto como sistema externo.

---

# 5. Relaciones entre los elementos

Las principales relaciones identificadas en el contexto del sistema son:

| Origen     | Relación                                      | Destino  |
| ---------- | --------------------------------------------- | -------- |
| Estudiante | Utiliza la plataforma                         | NOVI     |
| Profesor   | Administra y utiliza la plataforma            | NOVI     |
| NOVI       | Solicita servicios de inteligencia artificial | Groq API |
| Groq API   | Devuelve respuestas generadas                 | NOVI     |

La comunicación con la base de datos **no se representa en este nivel**, ya que
PostgreSQL forma parte de la implementación interna de NOVI y se detallará en
el C4 Nivel 2.

---

# 6. Diagrama C4 Nivel 1

```mermaid
flowchart LR

    EST["ESTUDIANTE<br/><br/>
    Consulta actividades<br/>
    Participa en grupos<br/>
    Realiza entregas<br/>
    Utiliza herramientas de IA"]

    PROF["PROFESOR<br/><br/>
    Administra grupos<br/>
    Crea actividades<br/>
    Genera quizzes y rúbricas<br/>
    Utiliza herramientas de IA"]

    NOVI["NOVI<br/><br/>
    Network Of Virtual Interaction<br/><br/>
    Plataforma educativa<br/>
    Gestión académica<br/>
    Comunicación grupal<br/>
    Herramientas de IA"]

    GROQ["Groq API<br/><br/>
    Servicio externo de<br/>
    Inteligencia Artificial"]

    EST -->|"Utiliza"| NOVI
    PROF -->|"Administra y utiliza"| NOVI

    NOVI -->|"Solicita servicios de IA"| GROQ
    GROQ -->|"Devuelve respuestas generadas"| NOVI

    classDef person fill:#1565C0,stroke:#64B5F6,color:#FFFFFF,stroke-width:2px;
    classDef system fill:#1976D2,stroke:#90CAF9,color:#FFFFFF,stroke-width:3px;
    classDef external fill:#616161,stroke:#BDBDBD,color:#FFFFFF,stroke-width:2px;

    class EST,PROF person;
    class NOVI system;
    class GROQ external;
```

---

# 7. Trazabilidad con la implementación

Aunque el Nivel 1 no requiere mostrar archivos o tecnologías internas, los
elementos representados pueden relacionarse con la implementación actual del
repositorio.

| Elemento C4 | Evidencia en el sistema                                                                    | Estado         |
| ----------- | ------------------------------------------------------------------------------------------ | -------------- |
| Estudiante  | Funcionalidades de grupos, actividades, entregas, mensajes y asistencia académica          | **Verificado** |
| Profesor    | Funcionalidades de creación de grupos, actividades, quizzes, rúbricas y herramientas de IA | **Verificado** |
| NOVI        | Aplicación frontend + backend + PostgreSQL ejecutada localmente                            | **Verificado** |
| Groq API    | `frontend/src/services/groq.js` y funciones de generación mediante IA                      | **Verificado** |

El repositorio identifica explícitamente el frontend, backend, PostgreSQL,
WebSocket, autenticación JWT y la integración con Groq.

La base de datos PostgreSQL se ejecuta como el contenedor `nevi-db` dentro de
Docker Compose y utiliza el volumen `nevi_db_data` para conservar los datos.

---

# 8. Límite del sistema

En este nivel se establece el siguiente límite conceptual:

### Dentro del sistema

* NOVI.

### Usuarios externos

* Estudiante.
* Profesor.

### Sistema externo

* Groq API.

### Componentes internos no representados

Los siguientes elementos forman parte de la implementación interna de NOVI y
por lo tanto no se muestran como sistemas independientes en el Nivel 1:

* React + Vite.
* Spring Boot.
* Spring Security.
* JWT.
* WebSocket / STOMP.
* PostgreSQL.
* Docker / Docker Compose.
* Nginx.

Estos elementos serán detallados mediante los modelos C4 de Nivel 2 y Nivel 3.

---

# 9. Arquitectura local y límite externo

La ejecución local de NOVI puede representarse conceptualmente de la
siguiente manera:

```text
                 ┌───────────────────────┐
                 │        NOVI           │
                 │                       │
                 │  Frontend             │
                 │  Backend              │
                 │  PostgreSQL           │
                 │  WebSocket            │
                 │  Nginx                │
                 │                       │
                 │  EJECUCIÓN LOCAL      │
                 └───────────┬───────────┘
                             │
                             │ Solicitudes de IA
                             ▼
                    ┌─────────────────┐
                    │    Groq API     │
                    │ SISTEMA EXTERNO │
                    └─────────────────┘
```

La aplicación y sus principales servicios se levantan localmente mediante
Docker Compose. El repositorio especifica los contenedores `nevi-db`,
`nevi-backend` y `nevi-frontend`, mientras que Groq requiere una clave de API
externa.

---

# 10. Conclusión

El modelo C4 Nivel 1 permite representar a NOVI como un sistema educativo
orientado a la interacción entre estudiantes y profesores.

Los estudiantes utilizan la plataforma para participar en grupos, consultar
y entregar actividades, comunicarse y acceder a herramientas de apoyo
académico.

Los profesores utilizan NOVI para administrar grupos, crear y gestionar
actividades y utilizar herramientas de inteligencia artificial para apoyar
diferentes procesos académicos.

La infraestructura principal de NOVI se ejecuta localmente mediante Docker
Compose, incluyendo el frontend, backend y la base de datos PostgreSQL.

La única comunicación externa representada en este contexto corresponde a
Groq API, utilizada para las funcionalidades de inteligencia artificial.

Esta vista establece el contexto general del sistema y sirve como punto de
entrada para los modelos C4 de Nivel 2 y Nivel 3, donde se detallan
respectivamente los contenedores y componentes que conforman la implementación
de NOVI.
