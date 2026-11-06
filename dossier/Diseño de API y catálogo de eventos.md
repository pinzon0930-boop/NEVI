# 2. Diseño del contrato de API y catálogo de eventos de NOVI

## 2.1 Objetivo

Definir los principales mecanismos de comunicación entre los Bounded Contexts identificados en NOVI, estableciendo los eventos que pueden generarse cuando ocurre una acción importante dentro de la plataforma.

## 2.2 Catálogo de eventos

Se propone el siguiente catálogo de eventos para representar las acciones relevantes del sistema:

| Evento | Contexto que lo genera | Descripción |
|---|---|---|
| UsuarioRegistrado | Identidad y acceso | Se genera cuando un nuevo usuario completa su registro. |
| UsuarioAutenticado | Identidad y acceso | Se genera cuando un usuario inicia sesión correctamente. |
| GrupoCreado | Grupos académicos | Se genera cuando un profesor crea un nuevo grupo. |
| EstudianteUnidoAGrupo | Grupos académicos | Se genera cuando un estudiante se incorpora a un grupo. |
| MensajeEnviado | Mensajería | Se genera cuando un usuario envía un mensaje en el chat. |
| ActividadCreada | Actividades | Se genera cuando un profesor registra una nueva actividad. |
| QuizGenerado | Evaluación académica | Se genera cuando se crea un cuestionario. |
| RubricaGenerada | Evaluación académica | Se genera cuando se genera una rúbrica de evaluación. |
| RetroalimentacionGenerada | Evaluación académica | Se genera cuando se produce retroalimentación para un estudiante. |
| ConsultaTutorIA | Tutoría con IA | Se genera cuando un usuario realiza una consulta al tutor académico. |

## 2.3 Estructura propuesta de los eventos

Para mantener una estructura organizada, se propone que los eventos compartan algunos campos básicos:

| Campo | Descripción |
|---|---|
| eventId | Identificador único del evento. |
| eventType | Nombre o tipo de evento. |
| occurredAt | Fecha y hora en que ocurrió. |
| source | Contexto que genera el evento. |
| data | Información relacionada con el evento. |

### Ejemplo de evento: GrupoCreado

```json
{
  "eventId": "evt-001",
  "eventType": "GrupoCreado",
  "occurredAt": "2026-11-06T10:30:00",
  "source": "GruposAcademicos",
  "data": {
    "grupoId": 15,
    "nombre": "Arquitectura de Software",
    "profesorId": 8
  }
}
```

Este ejemplo representa la estructura propuesta para comunicar que se creó un grupo académico.

## 2.4 Relaciones entre productores y consumidores

| Evento | Productor | Posibles consumidores |
|---|---|---|
| UsuarioRegistrado | Identidad y acceso | Grupos académicos |
| GrupoCreado | Grupos académicos | Mensajería y Actividades |
| EstudianteUnidoAGrupo | Grupos académicos | Mensajería y Actividades |
| MensajeEnviado | Mensajería | Otros componentes que necesiten actualizar información del chat |
| ActividadCreada | Actividades | Evaluación académica y Tutoría con IA |
| QuizGenerado | Evaluación académica | Actividades |
| RubricaGenerada | Evaluación académica | Actividades |
| RetroalimentacionGenerada | Evaluación académica | Análisis académico |
| ConsultaTutorIA | Tutoría con IA | Análisis académico |

## 2.5 Consideraciones de integración

El catálogo de eventos representa una propuesta para facilitar la comunicación entre los diferentes contextos de NOVI.

Los eventos permiten informar que ocurrió una acción sin que el contexto que los recibe tenga que depender directamente de la implementación interna del contexto que los genera.

Sin embargo, estos eventos no se consideran implementados actualmente. Su funcionamiento y utilidad deberán comprobarse mediante una prueba técnica (Spike 1).

## 2.6 Conclusión

El diseño del catálogo de eventos establece una base para organizar la comunicación entre las diferentes áreas de NOVI. También permite identificar qué información podría compartirse y qué contextos podrían necesitarla, preparando el terreno para evaluar una estrategia de integración mediante eventos.
