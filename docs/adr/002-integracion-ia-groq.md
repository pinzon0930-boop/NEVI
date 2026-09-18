# ADR-002 — Integración de IA: Frontend directo vs. Proxy por backend

**Estado:** Confirmada (con condición de revisión)
**Fecha:** 2026-09-18
**Autores:** Equipo NOVI
**Revisores:** Mini-comité técnico — semana 8

---

## Contexto

NOVI usa la API de Groq para cinco funcionalidades de IA: tutor académico,
generación de actividades, quiz, rúbricas y retroalimentación. La decisión
de cómo integrar esta API tiene implicaciones directas sobre **DRIVER-01
(Seguridad)** y **QA-03 (Seguridad)**.

El riesgo R-02 del dossier (`09-inventario-riesgos.md`) documenta que la
API Key de Groq actualmente viaja embebida en el bundle JavaScript del
frontend, lo que la expone a cualquier usuario con acceso a DevTools del
navegador.

La pregunta a responder es: ¿debe el frontend llamar directamente a la API
de Groq, o debe hacerlo a través de un endpoint proxy en el backend?

---

## Alternativas evaluadas

### Alternativa A — Frontend llama directamente a Groq (estado actual)

El servicio `frontend/src/services/groq.js` realiza llamadas HTTPS
directamente a `https://api.groq.com` usando `VITE_GROQ_API_KEY`.

**Costos:**
- La API Key queda embebida en el bundle JS: cualquier usuario puede
  inspeccionarla con `F12 → Sources` o `View Page Source`.
- Un atacante con la clave puede usar la cuota del equipo (abuso de API).
- No hay control sobre el uso: no se puede auditar quién usó el asistente
  ni limitar llamadas por usuario.

**Beneficios:**
- Sin cambios en el backend: el equipo no necesita añadir endpoints de
  proxy ni gestionar el costo adicional de procesamiento.
- Latencia ligeramente menor: el frontend llama directamente sin
  pasar por Spring Boot.
- Implementación ya existente y funcional.

### Alternativa B — Backend actúa como proxy de Groq

Se agrega un endpoint en el backend (por ejemplo, `POST /api/ia/consulta`)
que recibe la solicitud del frontend, la reenvía a la API de Groq con la
clave guardada como variable de entorno del servidor, y devuelve la
respuesta.

**Costos:**
- Requiere implementar al menos 5 endpoints proxy (uno por funcionalidad:
  `preguntarIA`, `generarActividad`, `generarQuiz`, `generarRubrica`,
  `generarRetroalimentacion`).
- La latencia aumenta ligeramente (un salto adicional: frontend → backend → Groq).
- Mayor carga en el backend: cada llamada de IA ahora pasa por Spring Boot.

**Beneficios:**
- La API Key nunca sale del servidor: no es visible en el frontend.
- Se puede añadir control de uso (rate limiting por usuario, auditoría).
- El frontend queda más simple: solo llama a la API interna sin gestionar
  credenciales externas.
- Elimina R-02 del inventario de riesgos activos.

---

## Decisión

**Se mantiene la Alternativa A para la entrega actual (semana 7), con
compromiso de migrar a la Alternativa B en la siguiente iteración.**

### Justificación

En el contexto académico y de tiempo disponible para S7, la migración
completa a un proxy requeriría agregar 5 endpoints nuevos al backend,
actualizar el frontend para llamarlos y agregar pruebas de integración
adicionales. El riesgo R-02 está documentado y comunicado al equipo;
el impacto real en el entorno académico (semilla de datos, sin usuarios
reales, clave de Groq en capa gratuita) es controlable para esta
iteración.

La decisión se revisará al inicio de la semana 9 según la condición
documentada abajo.

---

## Consecuencias

### Positivas
- Sin deuda técnica nueva para S7: el equipo puede concentrarse en los
  ADRs, mediciones y C4.
- El sistema funciona con la implementación actual (verificado en la
  medición baseline de `docs/experiment/02-resultados-baseline.md`).

### Negativas
- R-02 sigue activo: la API Key de Groq es visible en el código fuente
  del frontend.
- No es posible auditar el uso del asistente por usuario.
- Si la clave se comparte públicamente (por ejemplo, en un repositorio
  público), puede ser explotada.

### Reversibilidad
**Alta.** La migración a un proxy requiere:
1. Agregar un controlador `IaController.java` en `com.nevi.controller`.
2. Mover `VITE_GROQ_API_KEY` de variables del frontend a variables del
   backend.
3. Actualizar `groq.js` para apuntar a `/api/ia/` en lugar de `api.groq.com`.

**Condición de revisión:**
> "Si el repositorio pasa a ser público, O si la clave de Groq supera el
> límite de uso gratuito por el abuso de un tercero, se implementa
> inmediatamente la Alternativa B."

### Supuestos
- El repositorio permanece privado durante el ciclo del curso.
- La clave de Groq está en el nivel gratuito con límites de uso que
  mitigan el impacto de un posible abuso.
- Ningún usuario malintencionado tiene acceso al repositorio.
