# Evaluación de CQRS y consistencia eventual para NOVI

## 1. Pregunta a responder

Con el Spike 1 se validó que los módulos de NOVI pueden comunicarse mediante eventos internos de Spring (ADR-003). El siguiente paso es evaluar:

1. **¿Debe NOVI separar el modelo de escritura del modelo de lectura (CQRS)?**
2. **¿En qué flujos es aceptable la consistencia eventual y en cuáles no?**

La evaluación se basa en el código actual, en los escenarios de calidad (`04-escenarios-calidad.md`), en la medición baseline (`docs/experiment/02-resultados-baseline.md`) y en los resultados del Spike 1.

---

## 2. Conceptos aplicados a NOVI

**CQRS (Command Query Responsibility Segregation):** separa las operaciones que cambian el estado del sistema (comandos: crear grupo, unirse, entregar actividad) de las que solo consultan información (consultas: listar mis grupos, ver mensajes).

Tiene diferentes niveles de aplicación:

| Nivel | Qué separa | Consistencia | Ejemplo en NOVI |
|---|---|---|---|
| **0 — Sin CQRS** | El mismo servicio y las mismas entidades sirven para leer y escribir. | Fuerte | Estado actual: `GrupoService.crear()` y `GrupoService.misGrupos()` usan la entidad `Grupo`. |
| **1 — CQRS lógico** | Clases y modelos distintos para leer y escribir, pero con la misma base de datos. | Fuerte | Consultas con proyecciones (DTO directo desde JPQL) en un `GrupoQueryService`. |
| **2 — CQRS con vistas** | Tablas o vistas de lectura desnormalizadas, actualizadas mediante eventos. | Eventual | Tabla `grupo_resumen` con número de miembros y actividades, actualizada al recibir `EstudianteUnidoAGrupo`. |
| **3 — CQRS completo** | Almacén de lectura separado, como otra base de datos o caché, alimentado por eventos. | Eventual | Almacén de lectura en Redis o Elasticsearch para el dashboard. |

**Consistencia eventual:** ocurre cuando, después de ejecutar una operación, las consultas pueden mostrar temporalmente información anterior hasta que se actualicen. Es habitual cuando una parte del sistema se actualiza mediante un proceso separado después de confirmar una transacción.

---

## 3. Evidencia del sistema actual

| Evidencia | Dato | Qué implica |
|---|---|---|
| Rendimiento de lectura (ESC-02) | p95 = **18,17 ms**, p50 = 7,85 ms, 0 % errores frente a un objetivo de p95 ≤ 800 ms. | Hay un margen de aproximadamente **44 veces** respecto al objetivo. No existe un problema de lectura medido que justifique CQRS. |
| Forma de los modelos | Los DTO de respuesta (`GrupoResponse`, `ActividadResponse`, `MensajeResponse`) se construyen directamente desde sus entidades. | Los modelos de lectura y escritura son prácticamente iguales; separarlos no aporta suficiente claridad actualmente. |
| Lecturas entre módulos | Ninguna consulta actual combina datos de tres o más módulos. | No hay una pantalla que justifique una vista desnormalizada. |
| Infraestructura | Un solo backend y un solo PostgreSQL (ADR-001 y `docker-compose.yml`). | CQRS nivel 3 agregaría otro almacén que habría que operar y mantener. |
| Eventos (Spike 1) | Eventos de Spring en memoria, síncronos y no durables. | Las actualizaciones basadas en estos eventos pueden perderse si el backend falla antes de procesarlas. |
| Escala | Plataforma académica: un profesor crea pocos grupos y las escrituras son esporádicas. | El volumen absoluto de operaciones es bajo y no justifica separar los almacenes. |

### Hallazgo: el problema real de lectura no es de arquitectura

En `GrupoService.misGrupos()`, la rama de estudiante hace:

```java
return memberRepository.findByStudent(user)
    .stream()
    .map(gm -> GrupoResponse.desde(gm.getGrupo()))
    .toList();
```

`gm.getGrupo()` y luego `g.getTeacher()` (dentro de `GrupoResponse.desde`) son relaciones `LAZY`. Esto puede provocar consultas adicionales por cada grupo y profesor, generando el problema conocido como **N+1**.

Con la semilla pequeña del baseline no se nota; con datos sesgados (`03-preregistro-caso-extremo.md`) podría convertirse en el primer candidato a degradarse.

La solución sería utilizar una consulta con `JOIN FETCH` o una proyección. Esto corresponde a **CQRS nivel 1**, no a implementar un almacén de lectura separado.

Además, la consulta existente `findByStudentOrderByGrupoCreatedAtDesc` debe revisarse: según el análisis realizado, su condición `WHERE` no utiliza el parámetro `:student`, por lo que podría devolver membresías de otros estudiantes. Debe corregirse antes de utilizarla.

---

## 4. Evaluación de alternativas

Se utilizan los atributos de calidad definidos en el dossier:

| Criterio | Nivel 0 (actual) | Nivel 1 (CQRS lógico) | Nivel 2 (vistas + eventos) | Nivel 3 (CQRS completo) |
|---|---|---|---|---|
| **QA-02 Rendimiento** | Cumple con margen de 44 veces. | Mejora consultas puntuales como N+1. | Mejora lecturas agregadas que actualmente no existen. | Puede mejorar el rendimiento a gran escala, pero hoy es innecesario. |
| **QA-01 Disponibilidad** | Un solo punto de datos. | Mantiene la misma infraestructura. | Una vista puede quedar desactualizada si falla el consumidor. | Agrega componentes que también pueden fallar. |
| **QA-04 Modificabilidad** | Mantiene la estructura actual. | Permite agregar consultas sin modificar los comandos. | Cada cambio puede requerir actualizar eventos, consumidores y vistas. | Exige coordinar cambios entre dos almacenes. |
| **QA-05 Testeabilidad** | Pruebas de integración directas. | Mantiene pruebas relativamente sencillas. | Requiere probar la propagación de eventos. | Requiere pruebas con dos almacenes y tiempos de espera. |
| **QA-03 Seguridad** | Autorización contra datos actuales. | Mantiene acceso a datos actuales. | Existe riesgo de autorizar con información desactualizada. | Presenta el mismo riesgo del nivel 2. |
| **Consistencia** | Fuerte. | Fuerte. | Eventual. | Eventual. |
| **Esfuerzo para el equipo** | Ninguno adicional. | Bajo. | Medio. | Alto. |
| **Coherencia con ADR-001** | Compatible. | Compatible. | Introduce complejidad adicional. | Se aleja de la arquitectura de monolito modular. |

---

## 5. ¿Dónde es aceptable la consistencia eventual en NOVI?

La pregunta no es solamente si se debe utilizar consistencia eventual, sino cuánto retraso puede tolerar cada flujo.

| Flujo / evento | ¿Quién lee el resultado? | ¿Tolera retraso? | Consistencia requerida |
|---|---|---|---|
| `GrupoCreado` → Mensajería prepara el canal del grupo. | El profesor, al entrar al chat. | Sí, unos segundos. | **Eventual aceptable**, si el canal no es necesario para confirmar la creación del grupo. |
| `EstudianteUnidoAGrupo` → el estudiante ve el grupo en "Mis grupos". | El mismo estudiante. | No, espera verlo inmediatamente. | **Fuerte**. |
| `EstudianteUnidoAGrupo` → permiso para leer mensajes y entregar actividades. | La capa de seguridad. | No, autorizar con datos viejos puede generar un fallo de seguridad (QA-03). | **Fuerte**. |
| `MensajeEnviado` → otros miembros reciben el mensaje. | Los miembros, por WebSocket. | Puede tolerar un pequeño retraso, pero el orden importa. | Fuerte en el guardado; la entrega en tiempo real puede ser asíncrona. |
| `ActividadCreada` → Evaluación o Tutoría con IA preparan contenido. | El profesor, después. | Sí, segundos o minutos. | **Eventual aceptable**. |
| `RetroalimentacionGenerada` → análisis académico o estadísticas. | El profesor, en reportes. | Sí, minutos. | **Eventual aceptable**. |
| Entrega de una actividad (`entregar`) → el estudiante ve "entregada". | El mismo estudiante. | No, espera confirmación inmediata. | **Fuerte**. |

**Regla resultante:** todo lo que el mismo usuario necesita consultar inmediatamente después de su acción, y todo lo que se utiliza para autorizar operaciones, debe mantener consistencia fuerte. Los efectos secundarios no críticos entre módulos pueden tolerar consistencia eventual.

### Conexión con el Spike 1

El ADR-003 propone utilizar `@TransactionalEventListener(phase = AFTER_COMMIT)` para que los consumidores reaccionen después de confirmar la transacción.

Esto permite separar la reacción al evento de la operación principal, pero no garantiza por sí solo una entrega duradera ni una ejecución asíncrona.

Si el consumidor falla o el backend se cae antes de procesar el evento, el grupo puede quedar creado mientras el efecto secundario no se realiza. Los eventos internos de Spring no cuentan con almacenamiento persistente ni reintentos automáticos.

Esto es aceptable mientras los consumidores realicen tareas no críticas. Para un consumidor que requiera entrega garantizada sería necesario evaluar un mecanismo como el patrón **Transactional Outbox** o un broker de mensajería.

---

## 6. Recomendación

**No adoptar CQRS con almacén separado ni vistas actualizadas por eventos (niveles 2 y 3). Mantener el modelo actual y aplicar CQRS lógico (nivel 1) únicamente cuando exista un problema de consulta que lo justifique. La consistencia eventual debe limitarse a efectos secundarios no críticos entre módulos.**

### Justificación

- No existe actualmente un problema de lectura medido que justifique el costo: el p95 está aproximadamente 44 veces por debajo del objetivo.
- Los modelos de lectura y escritura son prácticamente iguales; separarlos en todos los módulos duplicaría código sin aportar suficiente claridad.
- La consistencia eventual basada en eventos no durables puede provocar pérdida de actualizaciones.
- El único problema de lectura identificado, N+1 en `misGrupos()`, puede resolverse optimizando la consulta sin cambiar la arquitectura.

### Acciones concretas

1. **Corregir el N+1** de `misGrupos()` (rama estudiante) mediante una consulta `JOIN FETCH` que incluya grupo y profesor, y que filtre correctamente por estudiante.
2. **Aplicar CQRS lógico de forma puntual:** cuando una consulta necesite datos de varias tablas, utilizar una proyección (`SELECT new ...Response(...)`) en lugar de cargar entidades completas.
3. **Clasificar cada consumidor de eventos** antes de implementarlo. Si requiere consistencia fuerte para lecturas inmediatas o autorización, no debe depender de una actualización eventual.
4. **Documentar la decisión** mediante el ADR-004, incluyendo las condiciones para revisar esta recomendación.

---

## 7. Cómo validarlo (experimento sugerido)

El preregistro `03-preregistro-caso-extremo.md` plantea una semilla sesgada para `GET /api/grupos`. Se propone utilizarlo para comprobar esta recomendación:

| Paso | Qué se hace | Criterio |
|---|---|---|
| 1 | Ejecutar el caso extremo con el código actual. | Registrar p95 y número de consultas SQL por petición (`spring.jpa.show-sql` o logs de Hibernate). |
| 2 | Aplicar únicamente la corrección del N+1. | Utilizar el mismo script y la misma semilla. |
| 3 | Comparar los resultados. | Si el p95 permanece holgadamente por debajo de 800 ms y las consultas por petición dejan de crecer con el número de grupos, se refuerza la conclusión de que CQRS completo no es necesario. |

Si incluso después de optimizar la consulta el p95 se acerca al objetivo, se deberá reconsiderar la aplicación del nivel 2 para ese endpoint.

**Estado del experimento:** propuesto, pendiente de ejecución. No se presentan resultados como si ya se hubiera realizado.

---

## 8. Condiciones para revisar esta decisión

Se reconsiderará CQRS de nivel 2 o 3 si ocurre alguna de estas situaciones:

- El p95 de un endpoint de lectura supera el 50 % del objetivo (400 ms) bajo una carga realista, después de optimizar sus consultas.
- Aparece una pantalla que combina datos de tres o más módulos, como un dashboard del profesor con miembros, entregas y mensajes por grupo, y su consulta se vuelve lenta o difícil de mantener.
- Las lecturas necesitan escalar de forma independiente de las escrituras, por ejemplo, mediante réplicas destinadas únicamente a consultas.
- Se implementa entrega garantizada de eventos mediante Outbox o un broker, reduciendo el riesgo de pérdida de actualizaciones.

---

## 9. Riesgos y limitaciones de esta evaluación

- La medición baseline utilizó una semilla pequeña y un entorno local sin latencia de red; por tanto, no representa necesariamente el comportamiento en producción.
- El caso extremo (`03-preregistro-caso-extremo.md`) está preregistrado, pero no ejecutado. La conclusión sobre el N+1 se basa en el análisis del código y no en una medición experimental.
- La tolerancia al retraso de cada flujo se definió por criterio del equipo y no se validó con usuarios.
- Solo se analizaron los eventos incluidos en el catálogo actual.
- La evaluación no demuestra que CQRS nunca será necesario; establece que, con las condiciones actuales de NOVI, su implementación completa no está justificada.

---

## 10. Conclusión

La evaluación concluye que NOVI debe conservar su arquitectura de monolito modular y no implementar CQRS completo por ahora. El rendimiento actual de las consultas cumple ampliamente el objetivo establecido y los problemas puntuales pueden resolverse mediante optimización de consultas.

La consistencia fuerte debe mantenerse en las operaciones que afectan directamente al usuario y en las decisiones de autorización. La consistencia eventual puede utilizarse para tareas secundarias entre módulos, siempre que un retraso o una posible pérdida de actualización no comprometa la funcionalidad principal.

Esta decisión podrá revisarse cuando aumente el volumen de datos, aparezcan nuevas necesidades de consulta o se implemente un mecanismo de entrega garantizada de eventos.
