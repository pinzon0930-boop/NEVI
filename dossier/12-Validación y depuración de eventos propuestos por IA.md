# Filtrado de eventos propuestos por la IA

Se revisaron los eventos propuestos para la arquitectura de NOVI, con el propósito de identificar cuáles corresponden a funciones reales del sistema, cuáles son redundantes y cuáles no tienen una justificación clara.

| Evento propuesto | Clasificación | Justificación |
|---|---|---|
| UsuarioRegistrado | Real | NOVI permite registrar nuevos usuarios. |
| UsuarioAutenticado | Redundante | El inicio de sesión ya hace parte del proceso de autenticación y no necesariamente requiere un evento independiente para la integración. |
| GrupoCreado | Real | El sistema permite crear grupos académicos. |
| EstudianteUnidoAGrupo | Real | Los estudiantes pueden ingresar a grupos mediante un código. |
| MensajeEnviado | Real | NOVI cuenta con un sistema de mensajería entre los integrantes de los grupos. |
| ActividadCreada | Real | Los profesores pueden crear actividades para sus grupos. |
| QuizGenerado | Real | El sistema contempla la creación de cuestionarios o evaluaciones. |
| RubricaGenerada | Real | NOVI contempla el uso de rúbricas para apoyar los procesos de evaluación. |
| RetroalimentacionGenerada | Real | El sistema permite generar retroalimentación para los estudiantes. |
| ConsultaTutorIA | Real | NOVI incluye un tutor con inteligencia artificial para resolver consultas académicas. |

## Resultado del filtrado

- **Eventos reales:** 9. Representan acciones relacionadas con funcionalidades contempladas en NOVI.
- **Eventos redundantes:** 1. UsuarioAutenticado, porque puede considerarse parte del proceso de autenticación.
- **Eventos inventados:** 0. En esta revisión no se identificaron eventos completamente ajenos a las funcionalidades contempladas en el sistema.

## Conclusión

El filtrado permitió identificar que la mayoría de los eventos propuestos están relacionados con las funcionalidades de NOVI. Sin embargo, no todos necesitan convertirse en eventos de integración. Se decidió dejar UsuarioAutenticado como redundante y conservar los demás como posibles eventos para la arquitectura.

**Nota:** Esta clasificación valida la relación de los eventos con las funcionalidades del sistema, pero no significa que todos estén implementados actualmente en el código. Esto deberá comprobarse durante el desarrollo del Spike 1.
