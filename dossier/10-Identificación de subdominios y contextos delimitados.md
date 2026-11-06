# Mapeo de subdominios y Bounded Contexts de NOVI

## Objetivo

Identificar las principales áreas funcionales de NOVI, establecer sus respectivos Bounded Contexts y definir las responsabilidades de cada uno dentro del sistema.

##  Mapeo de subdominios

| Subdominio | Bounded Context | Responsabilidad |
|---|---|---|
| Gestión de usuarios | Identidad y acceso | Administrar usuarios, autenticación y roles de profesores y estudiantes. |
| Gestión de grupos | Grupos académicos | Crear grupos, generar códigos de invitación y administrar integrantes. |
| Comunicación | Mensajería | Gestionar el envío y consulta de mensajes en tiempo real. |
| Gestión académica | Actividades | Crear, administrar y consultar actividades académicas. |
| Evaluación | Evaluación académica | Generar quizzes, rúbricas y retroalimentación para los estudiantes. |
| Asistencia académica | Tutoría con IA | Resolver dudas y proporcionar explicaciones sobre temas académicos. |
| Análisis académico | Análisis de grupos | Generar resúmenes y recomendaciones relacionadas con la actividad académica de los grupos. |

## Responsabilidades de cada Bounded Context

**Identidad y acceso:** se encarga del registro, inicio de sesión y administración de los roles de profesores y estudiantes.

**Grupos académicos:** permite crear grupos, generar códigos de invitación y administrar a los estudiantes que pertenecen a cada grupo.

**Mensajería:** permite la comunicación entre profesores y estudiantes mediante mensajes en tiempo real.

**Actividades:** administra la creación y consulta de actividades académicas dentro de los grupos.

**Evaluación académica:** proporciona herramientas para generar cuestionarios, rúbricas y retroalimentación de las actividades.

**Tutoría con IA:** ofrece asistencia académica mediante inteligencia artificial para resolver preguntas y facilitar la comprensión de diferentes temas.

**Análisis de grupos:** permite obtener resúmenes y recomendaciones relacionadas con el desarrollo de las actividades académicas.

## Relaciones entre los contextos

Los diferentes contextos se relacionan para permitir el funcionamiento de NOVI:

- Identidad y acceso permite identificar a los usuarios que utilizan la plataforma.
- Grupos académicos organiza a profesores y estudiantes.
- Mensajería utiliza la información de los grupos para permitir la comunicación entre sus integrantes.
- Actividades permite organizar el trabajo académico dentro de los grupos.
- Evaluación académica utiliza las actividades como base para generar herramientas de evaluación.
- Tutoría con IA y Análisis de grupos proporcionan asistencia y recomendaciones relacionadas con el contenido académico.

## Consideraciones arquitectónicas

Los Bounded Contexts representan una separación lógica de las responsabilidades del sistema. Esto no significa que cada uno deba funcionar como un microservicio independiente.

NOVI mantiene una arquitectura basada en Java y Spring Boot en su backend, por lo que estos contextos permiten identificar las diferentes responsabilidades del sistema y establecer posibles límites para su integración.

## Conclusión

El mapeo de subdominios y Bounded Contexts permite identificar las principales áreas funcionales de NOVI, comprender sus responsabilidades y reconocer cómo se relacionan entre sí. Esta división servirá como base para definir los contratos de API y los eventos de integración que se trabajarán en las siguientes actividades.
