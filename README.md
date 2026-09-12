<div align="center">
NEVI
Network Of Virtual Interaction

Plataforma educativa de comunicación y aprendizaje asistida por IA

<br>








<br>

NEVI conecta profesores y estudiantes en un entorno educativo moderno,
combinando comunicación, gestión académica e inteligencia artificial.

</div>
¿Qué es NEVI?

NEVI (Network Of Virtual Interaction) es una SPA (Single Page Application) diseñada para conectar profesores y estudiantes dentro de un entorno educativo colaborativo.

La plataforma permite a los profesores:

Crear y administrar grupos.
Crear actividades educativas.
Generar contenido utilizando inteligencia artificial.
Obtener análisis y resúmenes sobre sus grupos.
Comunicarse con los estudiantes.

Los estudiantes pueden:

Unirse a grupos mediante códigos de invitación.
Participar en chats grupales.
Consultar actividades.
Utilizar el Tutor NEVI para resolver dudas.
Obtener explicaciones simplificadas de las actividades.
Arquitectura general

El proyecto está compuesto por:

Capa	Tecnología
Frontend	React 18 + Vite
Backend	Java
Gestión	Maven
Base de datos	Local
Inteligencia Artificial	Groq API
Contenedores	Docker + Docker Compose
Funcionalidades
Para profesores
Funcionalidad	Descripción
Gestión de grupos	Crea grupos de clase con códigos de invitación únicos.
Chat grupal	Comunicación directa con los miembros del grupo.
Creación de actividades	Genera títulos y descripciones de actividades con IA.
Generador de Quiz	Crea preguntas de opción múltiple sobre cualquier tema.
Rúbricas automáticas	Genera rúbricas de evaluación con diferentes niveles de desempeño.
Retroalimentación con IA	Analiza respuestas y genera feedback constructivo.
Resumen del grupo	Analiza el estado del grupo y proporciona recomendaciones pedagógicas.
<br>
Para estudiantes
Funcionalidad	Descripción
Unirse por código	Accede a grupos mediante un código de invitación.
Chat grupal	Participa en las conversaciones del grupo.
Tutor NEVI	Asistente académico disponible para resolver dudas.
Explicaciones simplificadas	Convierte las actividades en explicaciones fáciles de comprender.
Consulta de actividades	Visualiza las actividades disponibles dentro de los grupos.
Stack tecnológico
<div align="center">
Tecnología	Uso
React 18	Interfaz de usuario
Vite	Herramienta de desarrollo y build
CSS	Estilos y diseño
Java	Backend
Maven	Gestión del backend
Base de datos local	Persistencia de información
Groq API	Funcionalidades de inteligencia artificial
Docker	Contenedores
Docker Compose	Orquestación de servicios
</div>
Capturas de pantalla
Autenticación
<div align="center">
Login	Crear cuenta
<img width="1211" height="556" alt="Login" src="https://github.com/user-attachments/assets/4a956e17-4a37-4fa2-80e4-df08c1343b59" />	<img width="399" height="571" alt="Crear cuenta" src="https://github.com/user-attachments/assets/51605225-d775-47ac-b345-6dbef15f321c" />
</div> <br>
Comunicación y asistencia
<div align="center">
Chat grupal	Tutor NEVI
<img width="838" height="590" alt="Chat grupal" src="https://github.com/user-attachments/assets/ab79fb7e-4802-4585-b421-de44d8db638c" />	<img width="1113" height="602" alt="Tutor NEVI" src="https://github.com/user-attachments/assets/6b400175-99f2-48be-a7ff-178cf566881c" />
</div> <br>
Herramientas de IA
<div align="center">
Generador de Quiz	Rúbrica automática
<img width="1920" height="939" alt="Generador de quiz" src="https://github.com/user-attachments/assets/d53bd5e8-fe64-4811-8507-142656b321cc" />	<img width="1920" height="943" alt="Rúbrica automática" src="https://github.com/user-attachments/assets/f3eb2557-2076-42be-a813-55ec561473c4" />
</div>
Estructura del proyecto
NEVI/
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/
│   │       │       └── nevi/
│   │       └── resources/
│   │
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
│   │   │   ├── Login.jsx
│   │   │   ├── Register.jsx
│   │   │   ├── Dashboard.jsx
│   │   │   ├── Grupos.jsx
│   │   │   └── GrupoDetalle.jsx
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

Instalación local
Prerrequisitos

Antes de comenzar, asegúrate de tener instalado:

Node.js 18+
npm 9+
Java JDK
Maven
Docker + Docker Compose
Una clave de API de Groq
1. Clonar el repositorio
git clone https://github.com/pinzon0930-boop/NEVI.git
cd NEVI

2. Instalar dependencias del frontend
cd frontend
npm install

3. Configurar variables de entorno

Utiliza el archivo .env.example, ubicado en la raíz del proyecto, como referencia para configurar las variables necesarias.

La configuración incluye los valores necesarios para:

La conexión con la base de datos local.
El acceso a la API de Groq.

Importante

Nunca compartas ni subas tus claves al repositorio.
Los archivos .env deben mantenerse fuera del control de versiones.

4. Ejecutar el backend

Desde la carpeta backend/:

cd ../backend
mvn spring-boot:run

5. Ejecutar el frontend

Desde la carpeta frontend/:

cd ../frontend
npm run dev


La aplicación estará disponible en:

http://localhost:5173

6. Ejecutar con Docker

El proyecto incluye docker-compose.yml para facilitar la ejecución de los servicios.

docker compose up --build


Para detener los servicios:

docker compose down

Variables de entorno

Las variables necesarias se encuentran documentadas en:

.env.example

Variable	Descripción
Variables de conexión a la base de datos	Configuración de la base de datos local.
Variable de API de Groq	Clave utilizada para acceder a los servicios de inteligencia artificial.

Nota: Los nombres exactos de las variables se encuentran en .env.example.

Integración con IA — Groq

NEVI utiliza Groq API para proporcionar diferentes funcionalidades de inteligencia artificial tanto a profesores como a estudiantes.

La integración se encuentra principalmente en:

frontend/src/services/groq.js

Funciones disponibles
Función	Propósito
preguntarIA(pregunta)	Tutor académico para estudiantes.
generarActividad(tema)	Crea actividades educativas.
generarQuiz(tema, cantidad)	Genera preguntas de opción múltiple.
generarRubrica(titulo, descripcion)	Genera rúbricas de evaluación.
generarRetroalimentacion(titulo, respuesta)	Genera feedback constructivo.
resumirActividad(titulo, descripcion)	Explica actividades en lenguaje simple.
generarResumenGrupo(actividades)	Analiza el estado del grupo.
Modelo de datos

La versión actual de NEVI utiliza una base de datos local.

La gestión y acceso a los datos se realiza mediante el backend desarrollado en Java.

A nivel conceptual, el sistema maneja información relacionada con:

                         USUARIOS
                             │
               ┌─────────────┴─────────────┐
               │                           │
               ▼                           ▼
           PERFILES                       ROLES
               │                           │
               └─────────────┬─────────────┘
                             │
                             ▼
                           GRUPOS
                             │
            ┌────────────────┼────────────────┐
            │                │                │
            ▼                ▼                ▼
       INTEGRANTES        MENSAJES       ACTIVIDADES
                                             │
                                ┌────────────┼────────────┐
                                │            │            │
                                ▼            ▼            ▼
                             QUIZZES      RÚBRICAS     FEEDBACK

Autores
<div align="center">
Nombre
Nixson Pinzón
Roberto Hernández
Camilo Flórez
Michael Lopez
<br>

Proyecto académico — Ingeniería de Sistemas

</div>
<div align="center">
NEVI

Hecho con React · Java · Maven · Docker · Groq

<br>

Si el proyecto te parece interesante, considera darle una estrella al repositorio.

</div>
