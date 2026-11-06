# Spike 01 — Resultados y evidencia de ejecución

**Spike:** Comunicación entre módulos mediante el evento `GrupoCreado`  
**Fecha de ejecución:** 6 de noviembre de 2026  
**Decisión evaluada:** ADR-003 — Comunicación entre módulos mediante eventos internos de Spring  
**Definición del Spike:** Definición del Spike 1 y sus criterios de éxito  
**Veredicto:** Decisión ajustada.

---

## 1. Qué se ejecutó realmente

Se implementó la publicación y recepción del evento `GrupoCreado` utilizando el mecanismo de eventos internos de Spring (`ApplicationEventPublisher` y `@EventListener`), sin agregar infraestructura externa como RabbitMQ o Kafka.

| Pieza | Archivo | Función |
|---|---|---|
| Evento | `GrupoCreadoEvent.java` | Representa los datos del grupo creado mediante un `record` inmutable. |
| Productor | `GrupoService.java` | Publica el evento después de guardar el grupo. |
| Consumidor | `GrupoCreadoListener.java` | Recibe el evento e imprime su información en la consola. |

El productor, perteneciente al módulo de Grupos académicos, no tiene una referencia directa al consumidor. Spring se encarga de entregar el evento a los componentes suscritos.

La prueba se realizó contra la API real del proyecto:

- Registro e inicio de sesión de un profesor mediante `POST /api/auth/register` y `POST /api/auth/login`.
- Creación de un grupo mediante `POST /api/grupos`.
- Verificación de la recepción del evento en los registros del backend.

## 2. Rama y commits utilizados

| Concepto | Valor |
|---|---|
| Rama | `Spike` |
| Commit inicial | `088aa20` — Create Condiciones.md |
| Implementación parcial | `e36afae` — Creación del evento y listener, todavía sin publicación. |
| Implementación final | `43dac8e` — Publicación del evento y evidencia de ejecución. |

Todos los cambios del experimento se realizaron en la rama `Spike`, sin modificar `main`.

## 3. Entorno y condiciones

| Elemento | Configuración |
|---|---|
| Sistema operativo | Windows con PowerShell |
| Orquestación | Docker Compose |
| Backend | Spring Boot 3.3.4 y Java 21 |
| Base de datos | PostgreSQL 16 |
| Contenedores utilizados | `nevi-db` y backend |
| Datos de prueba | Profesor `profe.spike@nevi.com` |
| Tiempo límite | 180 minutos |
| Tiempo real empleado | Aproximadamente 50 minutos (17:37 a 18:26, hora de Bogotá). |

El frontend no fue necesario para realizar esta prueba, ya que las operaciones se ejecutaron directamente mediante la API.

## 4. Evidencias de ejecución

Las evidencias se encuentran en la carpeta `evidencias/`:

- `logs-evento.txt`: salida del listener en el backend.
- `registro-bd.txt`: registro del grupo almacenado en PostgreSQL.
- `respuesta-api.json`: respuesta del endpoint `POST /api/grupos`.

### 4.1. Evento recibido por el listener

Durante la segunda ejecución, el listener recibió correctamente el evento:

```text
[SPIKE-01] EVENTO GRUPOCREADO RECIBIDO
[SPIKE-01] ID: 4815bd6c-ffbb-40af-b9e2-849f4217cbd8
[SPIKE-01] Nombre: Grupo Spike 01 - corrida 2
[SPIKE-01] ProfesorId: 03cf2785-dfe6-40a6-8783-29edd0fafe94
[SPIKE-01] Fecha: null
```

### 4.2. Registro almacenado en PostgreSQL

El grupo sí quedó registrado correctamente en la base de datos:

```text
id                                   | name                       | teacher_id                           | created_at
-------------------------------------|----------------------------|--------------------------------------|----------------------------
4815bd6c-ffbb-40af-b9e2-849f4217cbd8 | Grupo Spike 01 - corrida 2 | 03cf2785-dfe6-40a6-8783-29edd0fafe94 | 2026-11-06 23:23:55.345112
```

### 4.3. Respuesta de la API

La API devolvió la siguiente información al crear el grupo:

```json
{
  "id": "4815bd6c-ffbb-40af-b9e2-849f4217cbd8",
  "name": "Grupo Spike 01 - corrida 2",
  "accessCode": "XCNMAH",
  "teacherId": "03cf2785-dfe6-40a6-8783-29edd0fafe94",
  "teacherName": "Profe Spike",
  "createdAt": null
}
```

## 5. Qué se observó

### 5.1. Incidencias durante la ejecución

Durante la implementación se presentaron algunos inconvenientes:

1. **Primera compilación fallida:** el archivo del evento se guardó con un nombre incorrecto (`package com.nevi.event.java`). Java requiere que el archivo de una clase pública tenga el mismo nombre de la clase. Esto provocó errores adicionales en la compilación. Se solucionó renombrándolo a `GrupoCreadoEvent.java`.

2. **Primera ejecución sin recibir el evento:** el grupo se creó correctamente en la base de datos, pero el listener no mostró ningún mensaje. Se identificó que faltaba inyectar `ApplicationEventPublisher` y llamar a `publishEvent()` desde `GrupoService`.

3. **Segunda ejecución exitosa:** después de corregir la publicación, el listener recibió el evento y mostró la información del grupo creado.

### 5.2. Hallazgo principal: la fecha de creación llega en null

Se encontró que el evento recibía `fechaCreacion` como `null`, aunque PostgreSQL sí almacenaba la fecha correctamente.

La causa está relacionada con `@CreationTimestamp` de Hibernate. El valor de `createdAt` se asigna cuando se ejecuta la operación de inserción en la base de datos. Como `save()` no fuerza necesariamente esa operación inmediatamente, el evento se estaba construyendo antes de que la fecha estuviera disponible.

También se observó que la respuesta de la API devolvía `createdAt: null`, por lo que no era un problema exclusivo del evento.

### 5.3. Observación sobre las transacciones

Se identificó que `@EventListener` ejecuta el consumidor de forma síncrona dentro del flujo de la transacción, antes de que esta se confirme.

Esto representa un riesgo: si la transacción falla después de publicar el evento, el consumidor podría procesar información de un grupo que finalmente no quedó guardado.

## 6. Comparación con los criterios de éxito

| # | Criterio | Resultado | Evidencia |
|---|---|---|---|
| 1 | Generar el evento `GrupoCreado` al crear un grupo. | Cumple | El evento aparece en los registros del backend. |
| 2 | Enviar la información básica del grupo. | Parcial | ID, nombre y profesor llegan correctamente; la fecha llega en `null`. |
| 3 | Recibir el evento desde otro componente. | Cumple | `GrupoCreadoListener` recibe el evento sin estar acoplado directamente a `GrupoService`. |
| 4 | Comprobar que la información recibida corresponde al grupo creado. | Parcial | ID, nombre y profesor coinciden, pero la fecha presenta diferencias. |

## 7. Veredicto

### Decisión ajustada

La hipótesis principal se valida: NOVI puede comunicar sus módulos mediante eventos internos de Spring, sin necesidad de incorporar infraestructura externa y sin establecer una dependencia directa entre el productor y el consumidor.

Se cumplieron dos criterios completamente y dos de forma parcial.

Sin embargo, se identificaron dos ajustes obligatorios antes de integrar la implementación a `main`:

1. **Utilizar `saveAndFlush()`:** guardar y sincronizar el grupo con la base de datos antes de construir y publicar el evento. Esto permitirá que `createdAt` tenga el valor correspondiente.

2. **Utilizar `@TransactionalEventListener`:** reemplazar `@EventListener` por `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`, para que el consumidor reaccione únicamente después de que la transacción haya sido confirmada.

Por lo tanto, se considera viable adoptar la comunicación mediante eventos internos de Spring, pero los ajustes deben implementarse y verificarse antes de fusionar los cambios a `main`.

## 8. Limitaciones pendientes de verificar

- Los ajustes con `saveAndFlush()` y `@TransactionalEventListener` todavía no fueron ejecutados en esta prueba.
- No se provocó un fallo de transacción para comprobar el comportamiento ante un rollback.
- El listener únicamente imprime información en consola; no se probó un consumidor que realice operaciones reales en otros módulos.
- No se evaluó el procesamiento asíncrono mediante `@Async`.
- No se realizaron pruebas de rendimiento ni de carga.
- La verificación fue manual y todavía no existe una prueba de integración automatizada para este comportamiento.
- Solo se probó el evento `GrupoCreado`, no los demás eventos del catálogo.
- No se evaluó la durabilidad de los eventos ante una caída del backend. Los eventos internos de Spring se manejan en memoria y no ofrecen persistencia propia.

## 9. Conclusión

El Spike permitió comprobar que la comunicación mediante eventos internos de Spring es una alternativa viable para NOVI. Esta solución permite que los módulos reaccionen a determinadas acciones sin depender directamente unos de otros y sin agregar infraestructura adicional.

Además, la ejecución permitió identificar problemas relacionados con la persistencia de las fechas y el momento en que se procesan los eventos dentro de una transacción.

La recomendación final es mantener esta alternativa arquitectónica, aplicar los ajustes identificados y realizar una nueva prueba antes de integrar los cambios a la rama principal.
