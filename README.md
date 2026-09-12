<div align="center">
🎓 NEVI
Network Of Virtual Interaction
Plataforma educativa de comunicación y aprendizaje asistida por IA







</div>
¿Qué es NEVI?
NEVI es una SPA (Single Page Application) que conecta profesores y estudiantes en un entorno educativo. Los profesores pueden crear y administrar grupos, generar actividades con ayuda de IA y utilizar diferentes herramientas para apoyar el proceso académico. Los estudiantes pueden unirse a grupos mediante un código, consultar actividades, participar en el espacio de comunicación y acceder a herramientas de apoyo académico impulsadas por inteligencia artificial.

La aplicación cuenta con un frontend desarrollado en React y Vite, un backend desarrollado en Java y una base de datos ejecutada de forma local.

Funcionalidades
Para profesores
Gestión de grupos — Crea grupos de clase con código de invitación único
Chat grupal — Comunicación con los miembros del grupo
Creación de actividades — Genera títulos y descripciones de actividades con IA
Generador de quiz — Crea preguntas de opción múltiple sobre cualquier tema
Rúbricas automáticas — Genera rúbricas de evaluación con niveles detallados
Retroalimentación con IA — Analiza respuestas de estudiantes y genera feedback constructivo
Resumen del grupo — Panorama del estado del grupo con recomendaciones pedagógicas
Para estudiantes
Unirse por código — Acceso a grupos mediante un código de invitación
Chat grupal — Participación en la conversación del grupo
Tutor NEVI — Asistente académico disponible para resolver dudas
Explicaciones simplificadas — Las actividades se explican en lenguaje simple
Consulta de actividades — Visualización de las actividades disponibles dentro de los grupos
Stack tecnológico
Capa	Tecnología
Frontend	React 18 + Vite
Estilos	CSS
Backend	Java
Gestión del backend	Maven
Base de datos	Local
IA	Groq API
Contenedores	Docker + Docker Compose

Capturas de pantalla
Login	Crear cuenta
<img width="1211" height="556" alt="{A5B385FF-27D2-4C4B-8E53-06AE5AF1D7A8}" src="https://github.com/user-attachments/assets/4a956e17-4a37-4fa2-80e4-df08c1343b59" />	<img width="399" height="571" alt="{172E1BFF-0A40-4268-8616-21208DA32EFA}" src="https://github.com/user-attachments/assets/51605225-d775-47ac-b345-6dbef15f321c" />

Chat grupal	Tutor NEVI
<img width="838" height="590" alt="{B3C05C4E-1677-4964-9AE4-3553B433AF8C}" src="https://github.com/user-attachments/assets/ab79fb7e-4802-4585-b421-de44d8db638c" />	<img width="1113" height="602" alt="{37EF751D-2641-4189-863F-122D04065169}" src="https://github.com/user-attachments/assets/6b400175-99f2-48be-a7ff-178cf566881c" />

Generador de quiz	Rúbrica automática
<img width="1920" height="939" alt="image" src="https://github.com/user-attachments/assets/d53bd5e8-fe64-4811-8507-142656b321cc" />	<img width="1920" height="943" alt="image" src="https://github.com/user-attachments/assets/f3eb2557-2076-42be-a813-55ec561473c4" />

Estructura del proyecto
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

Instalación local
Prerrequisitos
Node.js 18+
npm 9+
Java JDK
Maven
Docker y Docker Compose
Una clave de API de Groq
1. Clonar el repositorio
git clone https://github.com/pinzon0930-boop/NEVI.git
cd NEVI

2. Instalar dependencias del frontend
cd frontend
npm install

3. Configurar variables de entorno
Utiliza el archivo .env.example ubicado en la raíz del proyecto como referencia para configurar las variables necesarias.

La configuración incluye los valores necesarios para la conexión con la base de datos local y el acceso a la API de Groq.

Importante: Nunca compartas ni subas tus claves al repositorio. Los archivos .env deben mantenerse fuera del control de versiones.

4. Ejecutar el backend
Desde la carpeta backend/:

cd ../backend
mvn spring-boot:run

5. Levantar el frontend
Desde la carpeta frontend/:

cd ../frontend
npm run dev

La aplicación estará disponible en:

http://localhost:5173

6. Ejecutar con Docker
El proyecto incluye un archivo docker-compose.yml para facilitar la ejecución de los servicios.

docker compose up --build

Para detener los servicios:

docker compose down

Variables de entorno
Variable	Descripción
Variables de conexión a la base de datos	Configuración de la base de datos local
Variable de API de Groq	Clave utilizada para acceder a los servicios de inteligencia artificial

Las variables disponibles y sus nombres exactos se encuentran en:

.env.example

Integración con IA (Groq)
NEVI utiliza la API de Groq para proporcionar diferentes funcionalidades de inteligencia artificial a profesores y estudiantes.

La integración se encuentra principalmente en:

frontend/src/services/groq.js

Función	Propósito
preguntarIA(pregunta)	Tutor académico para estudiantes
generarActividad(tema)	Crea actividades educativas
generarQuiz(tema, cantidad)	Genera preguntas de opción múltiple
generarRubrica(titulo, descripcion)	Genera rúbricas de evaluación
generarRetroalimentacion(titulo, respuesta)	Genera feedback constructivo
resumirActividad(titulo, descripcion)	Explica actividades en lenguaje simple
generarResumenGrupo(actividades)	Analiza el estado del grupo

Modelo de datos
La versión actual de NEVI utiliza una base de datos local.

La gestión y acceso a los datos se realiza mediante el backend Java.

A nivel conceptual, el sistema maneja información relacionada con:

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

Autores
Nixson Pinzón
Roberto Hernández
Camilo Flórez
Michael Lopez
Proyecto académico — Ingeniería de Sistemas

<div align="center"> Hecho con ❤️ usando React · Java · Maven · Docker · Groq </div>
