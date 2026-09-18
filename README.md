<div align="center">

# NOVI
### Network Of Virtual Interaction

**Plataforma educativa de comunicación y aprendizaje asistida por IA**

</div>

## ¿Qué es NOVI?

NOVI es una SPA (Single Page Application) que conecta profesores y estudiantes en un entorno educativo. Los profesores pueden crear y administrar grupos, generar actividades con ayuda de IA y utilizar diferentes herramientas para apoyar el proceso académico.

Los estudiantes pueden unirse a grupos mediante un código, consultar actividades, participar en el espacio de comunicación en tiempo real y acceder a herramientas de apoyo académico impulsadas por inteligencia artificial.

La aplicación está compuesta por un frontend en **React + Vite**, un backend en **Java (Spring Boot)** con autenticación JWT y chat en tiempo real por WebSocket, una base de datos **PostgreSQL**, e integración con la **API de Groq** para las funcionalidades de IA. Todo el stack se levanta con **Docker Compose**.

## Funcionalidades

### Para profesores
- **Gestión de grupos** — Crea grupos de clase con código de invitación único
- **Chat grupal** — Comunicación en tiempo real con los miembros del grupo
- **Creación de actividades** — Genera títulos y descripciones de actividades con IA
- **Generador de quiz** — Crea preguntas de opción múltiple sobre cualquier tema
- **Rúbricas automáticas** — Genera rúbricas de evaluación con niveles detallados
- **Retroalimentación con IA** — Analiza respuestas de estudiantes y genera feedback constructivo
- **Resumen del grupo** — Panorama del estado del grupo con recomendaciones pedagógicas

### Para estudiantes
- **Unirse por código** — Acceso a grupos mediante un código de invitación
- **Chat grupal** — Participación en la conversación del grupo en tiempo real
- **Tutor NEVI** — Asistente académico disponible para resolver dudas
- **Explicaciones simplificadas** — Las actividades se explican en lenguaje simple
- **Consulta de actividades** — Visualización de las actividades disponibles dentro de los grupos

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Frontend | React 18 + Vite 5 |
| Estilos | Tailwind CSS 3 |
| Tiempo real | WebSocket (STOMP) vía `@stomp/stompjs` + SockJS |
| Backend | Java 21 + Spring Boot 3.3 |
| Gestión del backend | Maven |
| Autenticación | Spring Security + JWT (jjwt) |
| Base de datos | PostgreSQL 16 |
| IA | Groq API |
| Contenedores | Docker + Docker Compose |
| Servidor web (producción) | Nginx (sirve el build del frontend) |

## Capturas de pantalla

| Login | Crear cuenta |
|---|---|
| <img width="1211" height="556" alt="Login" src="https://github.com/user-attachments/assets/4a956e17-4a37-4fa2-80e4-df08c1343b59" /> | <img width="399" height="571" alt="Crear cuenta" src="https://github.com/user-attachments/assets/51605225-d775-47ac-b345-6dbef15f321c" /> |

| Chat grupal | Tutor NEVI |
|---|---|
| <img width="838" height="590" alt="Chat grupal" src="https://github.com/user-attachments/assets/ab79fb7e-4802-4585-b421-de44d8db638c" /> | <img width="1113" height="602" alt="Tutor NEVI" src="https://github.com/user-attachments/assets/6b400175-99f2-48be-a7ff-178cf566881c" /> |

| Generador de quiz | Rúbrica automática |
|---|---|
| <img width="1920" height="939" alt="Generador de quiz" src="https://github.com/user-attachments/assets/d53bd5e8-fe64-4811-8507-142656b321cc" /> | <img width="1920" height="943" alt="Rúbrica automática" src="https://github.com/user-attachments/assets/f3eb2557-2076-42be-a813-55ec561473c4" /> |

## Estructura del proyecto

```
NEVI/
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/
│   │       │       └── nevi/
│   │       └── resources/
│   ├── Dockerfile
│   └── pom.xml
├── dossier/
│   └── 01-contexto-sistema.md
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── AsistenteIA.jsx
│   │   │   ├── CrearActividad.jsx
│   │   │   ├── CrearGrupo.jsx
│   │   │   ├── ModalExplicacion.jsx
│   │   │   ├── ModalQuiz.jsx
│   │   │   ├── ModalRetroalimentacion.jsx
│   │   │   └── ModalRubrica.jsx
│   │   ├── context/
│   │   ├── pages/
│   │   │   ├── Login.jsx
│   │   │   ├── Register.jsx
│   │   │   ├── Dashboard.jsx
│   │   │   ├── Grupos.jsx
│   │   │   └── GrupoDetalle.jsx
│   │   ├── services/
│   │   │   ├── actividades.js
│   │   │   ├── api.js
│   │   │   ├── auth.js
│   │   │   ├── groq.js
│   │   │   ├── grupos.js
│   │   │   └── mensajes.js
│   │   ├── App.jsx
│   │   ├── index.css
│   │   └── main.jsx
│   └── index.html
├── .env.example
├── .gitignore
├── docker-compose.yml
└── README.md
```

## Instalación local

### Prerrequisitos

- **Docker** y **Docker Compose** (recomendado — no necesitas instalar nada más)
- Una clave de API de Groq, gratuita en [console.groq.com](https://console.groq.com)

Si prefieres correr los servicios sin Docker, adicionalmente necesitas:
- Node.js 18+ y npm 9+
- Java JDK 21
- Maven
- PostgreSQL 16 instalado y corriendo localmente

### Opción Con Docker

**1. Clonar el repositorio**
```bash
git clone https://github.com/pinzon0930-boop/NEVI.git
cd NEVI
```

**2. Configurar variables de entorno**
```bash
cp .env.example .env
```
Abre `.env` y reemplaza al menos `VITE_GROQ_API_KEY` con tu clave real. Puedes dejar los demás valores por defecto para desarrollo local.

**3. Levantar todo con Docker Compose**
```bash
docker compose up --build
```

Esto levanta tres contenedores: `nevi-db` (PostgreSQL), `nevi-backend` (Spring Boot) y `nevi-frontend` (React servido con Nginx). La primera vez tarda varios minutos mientras descarga imágenes y compila.

**4. Abrir la aplicación**

Ve a **http://localhost** en tu navegador.

Para detener todo:
```bash
docker compose down
```

## Variables de entorno

Todas las variables están documentadas en [`.env.example`](.env.example). Cópialo a `.env` antes de arrancar el proyecto y **nunca subas tu `.env` real al repositorio**.

| Variable | Descripción |
|---|---|
| `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Credenciales de la base de datos PostgreSQL |
| `JWT_SECRET` | Clave secreta para firmar los tokens JWT (usa una aleatoria de 32+ caracteres en producción) |
| `VITE_API_URL` | URL base del backend que consume el frontend (`http://localhost:8080` en desarrollo) |
| `VITE_WS_URL` | URL del endpoint WebSocket para el chat en tiempo real |
| `VITE_GROQ_API_KEY` | Clave de la API de Groq usada para las funcionalidades de IA |

## Integración con IA (Groq)

NEVI utiliza la API de Groq para proporcionar diferentes funcionalidades de inteligencia artificial a profesores y estudiantes.

La integración se encuentra principalmente en [`frontend/src/services/groq.js`](frontend/src/services/groq.js):

| Función | Propósito |
|---|---|
| `preguntarIA(pregunta)` | Tutor académico para estudiantes |
| `generarActividad(tema)` | Crea actividades educativas |
| `generarQuiz(tema, cantidad)` | Genera preguntas de opción múltiple |
| `generarRubrica(titulo, descripcion)` | Genera rúbricas de evaluación |
| `generarRetroalimentacion(titulo, respuesta)` | Genera feedback constructivo |
| `resumirActividad(titulo, descripcion)` | Explica actividades en lenguaje simple |
| `generarResumenGrupo(actividades)` | Analiza el estado del grupo |

## Modelo de datos

NEVI utiliza una base de datos **PostgreSQL**, gestionada por el backend Java mediante Spring Data JPA (Hibernate se encarga de crear/actualizar el esquema automáticamente al arrancar).

A nivel conceptual, el sistema maneja información relacionada con:

```
usuarios
   │
   ├── perfiles
   │
   └── roles
        │
        ▼
      grupos
        │
        ├── integrantes
        ├── mensajes
        └── actividades
                    │
                    ├── quizzes
                    ├── rúbricas
                    └── retroalimentación
```

## Solución de problemas comunes

**El backend nunca pasa a `healthy` en Docker / la app se queda colgada esperándolo**
Verifica que `backend/pom.xml` incluya la dependencia `spring-boot-starter-actuator`, necesaria para que exponga `/actuator/health`, el endpoint que usa el healthcheck de `docker-compose.yml`.

**Error de compilación `package jakarta.validation does not exist`**
Verifica que `backend/pom.xml` incluya la dependencia `spring-boot-starter-validation`. Si falta, agrégala y vuelve a construir con `docker compose build --no-cache backend`.

**`docker compose up --build` falla sin razón aparente en Windows**
Reinicia Docker Desktop y confirma que tiene acceso a internet (`docker run --rm alpine sh -c "apk add --no-cache curl >/dev/null 2>&1; curl -sI https://repo1.maven.org/maven2/"`). VPNs y antivirus con inspección de tráfico HTTPS pueden bloquear las descargas de Maven dentro del contenedor.

## Autores

- Nixson Pinzón
- Roberto Hernández
- Camilo Flórez
- Michael Lopez

Proyecto académico — Ingeniería de Sistemas

