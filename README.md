<div align="center">
🎓 NEVI
Plataforma Educativa con Inteligencia Artificial
Plataforma educativa para la gestión de grupos, actividades académicas, comunicación y herramientas de inteligencia artificial







</div>
¿Qué es NEVI?
NEVI es una plataforma educativa orientada a facilitar la interacción entre profesores y estudiantes dentro de espacios académicos.

La plataforma permite crear y administrar grupos, gestionar actividades, intercambiar mensajes y utilizar diferentes herramientas de inteligencia artificial como apoyo al proceso educativo.

Los profesores pueden crear grupos, generar actividades, quizzes y rúbricas, además de obtener retroalimentación y análisis relacionados con el trabajo de los estudiantes.

Los estudiantes pueden unirse a grupos mediante códigos de invitación, consultar actividades, participar en los espacios de comunicación y utilizar herramientas de inteligencia artificial para apoyar su aprendizaje.

Actualmente, el sistema utiliza una arquitectura compuesta por un frontend desarrollado con React y Vite y un backend desarrollado en Java, con persistencia de datos de forma local. Para las funcionalidades de inteligencia artificial se utiliza la API de Groq.

Funcionalidades
Para profesores
Gestión de grupos — Creación y administración de grupos académicos.
Código de invitación — Generación de códigos para permitir que los estudiantes se incorporen a un grupo.
Gestión de actividades — Creación y administración de actividades académicas.
Generador de actividades con IA — Apoyo mediante inteligencia artificial para crear contenido educativo.
Generador de quizzes — Creación de preguntas de opción múltiple sobre diferentes temas.
Generación de rúbricas — Creación de criterios de evaluación con diferentes niveles de desempeño.
Retroalimentación con IA — Análisis de respuestas y generación de retroalimentación.
Resumen del grupo — Generación de información y análisis sobre las actividades del grupo.
Comunicación grupal — Interacción mediante mensajes dentro de los grupos.
Para estudiantes
Registro e inicio de sesión — Acceso individual a la plataforma.
Unirse a grupos — Incorporación a grupos mediante código de invitación.
Consulta de actividades — Visualización de las actividades asignadas.
Participación en grupos — Interacción y comunicación con los integrantes del grupo.
Tutor IA — Asistente de inteligencia artificial para resolver dudas académicas.
Explicaciones simplificadas — Conversión de descripciones de actividades a explicaciones más sencillas.
Apoyo académico mediante IA — Utilización de las herramientas de inteligencia artificial disponibles en la plataforma.
Arquitectura del sistema
NEVI está organizado en diferentes capas que separan la interfaz de usuario, la lógica de aplicación, los servicios y la persistencia de datos.

                         ┌─────────────────────┐
                         │       USUARIO       │
                         │  Profesor / Alumno  │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │      FRONTEND       │
                         │    React + Vite     │
                         └──────────┬──────────┘
                                    │
                              Servicios
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │       BACKEND       │
                         │     Java / Maven    │
                         └──────────┬──────────┘
                                    │
                         ┌──────────┴──────────┐
                         │                     │
                         ▼                     ▼
                ┌─────────────────┐   ┌─────────────────┐
                │ Base de datos   │   │    Groq API     │
                │     Local       │   │ Inteligencia IA │
                └─────────────────┘   └─────────────────┘

Frontend
El frontend es una aplicación web desarrollada con React y Vite.

Se encarga de:

Presentar la interfaz de usuario.
Gestionar las páginas y navegación.
Manejar los componentes de la aplicación.
Gestionar el estado necesario de la aplicación.
Comunicarse con los servicios internos y el backend.
Presentar las funcionalidades de inteligencia artificial.
Backend
El backend está desarrollado en Java y utiliza Maven para la gestión del proyecto y sus dependencias.

Su función es centralizar la lógica de negocio y las operaciones relacionadas con los datos de la aplicación.

El backend se encuentra dentro de la carpeta:

backend/

y cuenta con configuración para su ejecución mediante Docker.

Persistencia
A diferencia de la versión anterior del proyecto, donde la información se almacenaba mediante servicios de base de datos en la nube, la versión actual utiliza una base de datos local.

Esto permite ejecutar el sistema y sus datos dentro del entorno local de desarrollo.

Inteligencia artificial
NEVI utiliza Groq API como servicio externo para las funcionalidades relacionadas con inteligencia artificial.

La comunicación con Groq se realiza desde los servicios correspondientes de la aplicación.

Stack tecnológico
Capa	Tecnología
Frontend	React 18
Build Tool	Vite
Estilos	CSS
Backend	Java
Gestión de dependencias	Maven
Base de datos	Base de datos local
Inteligencia artificial	Groq API
Contenedores	Docker
Orquestación	Docker Compose

Estructura del proyecto
La estructura actual del proyecto se encuentra organizada de la siguiente manera:

NEVI/
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/
│   │       │       └── nevi/
│   │       └── resources/
│   ├── Dockerfile
│   └── pom.xml
│
├── dossier/
│   └── 01-contexto-sistema.md
│
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
│   │   │
│   │   ├── context/
│   │   │
│   │   ├── pages/
│   │   │   ├── Dashboard.jsx
│   │   │   ├── GrupoDetalle.jsx
│   │   │   ├── Grupos.jsx
│   │   │   ├── Login.jsx
│   │   │   └── Register.jsx
│   │   │
│   │   ├── services/
│   │   │   ├── actividades.js
│   │   │   ├── api.js
│   │   │   ├── auth.js
│   │   │   ├── groq.js
│   │   │   ├── grupos.js
│   │   │   └── mensajes.js
│   │   │
│   │   ├── App.jsx
│   │   ├── index.css
│   │   └── main.jsx
│   │
│   └── index.html
│
├── .env.example
├── .gitignore
├── docker-compose.yml
└── README.md

Descripción de las principales carpetas
frontend/src/components/
Contiene los componentes reutilizables de la interfaz y las herramientas académicas de NEVI.

Entre ellos se encuentran:

AsistenteIA.jsx — Asistente académico basado en IA.
CrearActividad.jsx — Creación de actividades.
CrearGrupo.jsx — Creación de grupos.
ModalQuiz.jsx — Generación de quizzes.
ModalRubrica.jsx — Generación de rúbricas.
ModalRetroalimentacion.jsx — Generación de retroalimentación.
ModalExplicacion.jsx — Generación de explicaciones simplificadas.
frontend/src/pages/
Contiene las páginas principales de la aplicación:

Login.jsx — Inicio de sesión.
Register.jsx — Registro de usuarios.
Dashboard.jsx — Panel principal.
Grupos.jsx — Gestión y visualización de grupos.
GrupoDetalle.jsx — Detalle y funcionalidades de un grupo.
frontend/src/services/
Contiene los servicios encargados de manejar las operaciones de la aplicación:

api.js — Comunicación con el backend.
auth.js — Operaciones relacionadas con autenticación.
grupos.js — Operaciones relacionadas con grupos.
actividades.js — Operaciones relacionadas con actividades.
mensajes.js — Operaciones relacionadas con mensajes.
groq.js — Funciones relacionadas con inteligencia artificial.
frontend/src/context/
Contiene los contextos utilizados para compartir información y estado entre diferentes componentes de la aplicación.

backend/
Contiene la implementación del servidor de NEVI.

El proyecto backend utiliza:

Java.
Maven.
Docker.
dossier/
Contiene documentación relacionada con el análisis, contexto y arquitectura del sistema.

Instalación local
Prerrequisitos
Antes de ejecutar NEVI se recomienda tener instalado:

Node.js 18 o superior
npm 9 o superior
Java JDK
Maven
Docker
Docker Compose
Una clave de API de Groq
1. Clonar el repositorio
git clone https://github.com/pinzon0930-boop/NEVI.git
cd NEVI

2. Configurar las variables de entorno
El proyecto incluye un archivo de referencia:

.env.example

Crea el archivo de variables de entorno correspondiente y configura las variables necesarias para la ejecución del sistema.

La configuración debe incluir la información necesaria para la conexión con la base de datos local y el servicio de inteligencia artificial.

Importante: Nunca compartas ni subas claves privadas al repositorio. Los archivos de entorno deben permanecer fuera del control de versiones.

3. Instalar dependencias del frontend
Ingresa a la carpeta del frontend:

cd frontend

Instala las dependencias:

npm install

4. Ejecutar el frontend
Desde la carpeta frontend:

npm run dev

La aplicación estará disponible normalmente en:

http://localhost:5173

5. Ejecutar el backend
El backend se encuentra en:

backend/

Al utilizar Maven, puede ejecutarse mediante las herramientas configuradas en el proyecto.

Desde la carpeta backend:

cd backend
mvn spring-boot:run

La forma exacta de ejecución puede variar según la configuración actual del proyecto y la clase principal definida en el backend.

6. Ejecutar mediante Docker Compose
NEVI también incluye:

docker-compose.yml

Este archivo permite centralizar la ejecución de los servicios configurados para el proyecto.

Para iniciar los servicios:

docker compose up --build

Para detenerlos:

docker compose down

Variables de entorno
Las variables de entorno utilizadas por el proyecto se encuentran documentadas en:

.env.example

Entre las configuraciones principales se encuentra la información necesaria para:

Configuración	Propósito
Base de datos	Conexión con la persistencia local
Groq API	Acceso a los servicios de inteligencia artificial
Backend	Configuración de comunicación entre frontend y backend

Las variables concretas deben mantenerse sincronizadas con .env.example y con la configuración utilizada actualmente por el backend y frontend.

Integración con IA
NEVI utiliza Groq API para proporcionar diferentes funcionalidades de inteligencia artificial.

Las funcionalidades disponibles incluyen:

Funcionalidad	Propósito
Tutor IA	Responder preguntas académicas de los estudiantes
Generación de actividades	Crear contenido para actividades educativas
Generación de quizzes	Crear preguntas de opción múltiple
Generación de rúbricas	Crear criterios de evaluación
Retroalimentación	Analizar respuestas y proporcionar feedback
Explicaciones	Simplificar la explicación de actividades
Resumen de grupo	Analizar información relacionada con el grupo

La integración se encuentra principalmente en:

frontend/src/services/groq.js

Modelo de datos
La versión actual del proyecto utiliza persistencia local.

La estructura concreta de la base de datos se encuentra asociada a la implementación del backend y debe mantenerse alineada con los modelos y servicios definidos dentro de:

backend/src/main/

A nivel conceptual, NEVI maneja información relacionada con:

Usuarios
   │
   ├── Perfiles
   │
   └── Roles
          │
          ▼
        Grupos
          │
          ├── Integrantes
          │
          ├── Mensajes
          │
          └── Actividades
                   │
                   ├── Quizzes
                   ├── Rúbricas
                   └── Retroalimentación

Comunicación entre componentes
La comunicación general del sistema sigue el siguiente flujo:

Usuario
   │
   ▼
React / Vite
   │
   ▼
Services
   │
   ▼
API
   │
   ▼
Backend Java
   │
   ├──────────────► Base de datos local
   │
   └──────────────► Groq API

El frontend se encarga principalmente de la presentación y de la interacción con el usuario, mientras que el backend centraliza las operaciones correspondientes a la lógica de la aplicación y el acceso a los datos.

Documentación
La documentación arquitectónica del proyecto se encuentra en:

dossier/

Actualmente contiene documentación relacionada con el contexto general del sistema.

La documentación seguirá la metodología C4, permitiendo representar progresivamente:

Nivel 1 — Contexto
Nivel 2 — Contenedores
Nivel 3 — Componentes
Nivel 4 — Código, cuando sea necesario
Estado actual del proyecto
NEVI se encuentra en proceso de evolución desde una arquitectura basada principalmente en servicios gestionados en la nube hacia una arquitectura con backend propio y persistencia local.

Los principales cambios respecto a la versión anterior incluyen:

Incorporación de un backend desarrollado en Java.
Incorporación de Maven para la gestión del backend.
Incorporación de Docker y Docker Compose.
Migración de la persistencia desde la nube hacia un entorno local.
Separación más clara entre frontend, backend y servicios.
Incorporación de una capa services/api.js para la comunicación con el backend.
Incorporación de un directorio context en el frontend.
Actualización de la documentación arquitectónica.
Mantenimiento de Groq como servicio externo para las funcionalidades de IA.
Autores
Nixson Pinzón
Roberto Hernández
Camilo Flórez
Michael Lopez
Proyecto académico — Ingeniería de Sistemas

<div align="center">

React · Java · Maven · Docker · Groq

</div>
