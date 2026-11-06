# Registro de ejecución, observaciones y veredicto – Spike 1

## 1. Datos generales

- **Nombre:** Spike 1 – Comunicación entre módulos mediante eventos.
- **Fecha de ejecución:** 6 de noviembre de 2026.
- **Rama experimental:** `Spike`.
- **Commit inicial:** `088aa20`.
- **Commit de implementación parcial:** `e36afae`.
- **Commit final del Spike:** `43dac8e`.
- **Tiempo asignado:** 180 minutos.
- **Tiempo real empleado:** aproximadamente 50 minutos.

## 2. ¿Qué se ejecutó?

Se realizó una prueba técnica para comprobar si NOVI puede comunicar sus módulos mediante eventos internos de Spring, utilizando como ejemplo la creación de un grupo académico.

Para ello se realizaron las siguientes actividades:

1. Se creó el evento `GrupoCreadoEvent.java`, encargado de representar la información del grupo creado.
2. Se creó `GrupoCreadoListener.java`, encargado de recibir el evento.
3. Se modificó `GrupoService.java` para publicar el evento después de guardar el grupo.
4. Se registró un profesor de prueba mediante la API.
5. Se creó el grupo `Grupo Spike 01` utilizando PowerShell.
6. Se verificó la recepción del evento mediante los registros de la consola.
7. Se comparó la información recibida con los datos almacenados en PostgreSQL y la respuesta de la API.

## 3. ¿Qué se observó?

Durante la ejecución se encontraron los siguientes resultados:

### 3.1. Problemas durante la implementación

- En la primera compilación se presentó un error por el nombre incorrecto del archivo del evento. Se solucionó renombrándolo a `GrupoCreadoEvent.java`.
- En la primera prueba se creó el grupo, pero el evento no apareció en la consola porque faltaba implementar su publicación desde `GrupoService`.
- Después de corregirlo, se realizó una segunda prueba en la que el evento fue recibido correctamente.

### 3.2. Hallazgo principal

Se identificó que el campo `fechaCreacion` llegaba como `null` en el evento, aunque la base de datos sí tenía registrada la fecha.

Esto ocurrió porque Hibernate asigna la fecha durante la operación de inserción en la base de datos y el evento se estaba publicando antes de que ese valor estuviera disponible.

También se identificó que `@EventListener` ejecuta el consumidor antes de confirmar la transacción, lo que podría generar inconsistencias si posteriormente ocurre un error.

## 4. Comparación con los criterios de éxito

| Criterio | Resultado |
|---|---|
| Generar el evento al crear un grupo | Cumplido |
| Enviar la información básica del grupo | Cumplido parcialmente: la fecha llegó en `null` |
| Recibir el evento desde otro componente | Cumplido |
| Comprobar que los datos recibidos corresponden al grupo | Cumplido parcialmente: ID, nombre y profesor coincidieron, pero la fecha no |

## 5. Veredicto final

**Decisión: adoptar la comunicación mediante eventos internos de Spring con ajustes obligatorios.**

La hipótesis del Spike se validó, ya que se comprobó que NOVI puede comunicar sus componentes mediante eventos sin necesidad de incorporar herramientas externas como RabbitMQ o Kafka.

Sin embargo, la implementación presentó dos problemas que deben corregirse antes de integrar los cambios a `main`:

1. Utilizar `saveAndFlush()` para garantizar que la fecha de creación esté disponible antes de publicar el evento.
2. Utilizar `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` para que los consumidores reciban el evento únicamente después de confirmar la transacción.

Por lo tanto, la decisión es favorable, pero la implementación todavía requiere ajustes y una nueva prueba de verificación.

## 6. Limitaciones identificadas

- No se probaron todavía los dos ajustes propuestos.
- No se verificó el comportamiento ante un rollback.
- El listener únicamente imprime la información en consola; no se probaron consumidores reales de otros módulos.
- No se realizaron pruebas de rendimiento ni de carga.
- La comprobación fue manual y no mediante pruebas automatizadas.
- No se evaluó la durabilidad de los eventos ante una caída del backend.

## 7. Evidencias

- `logs-evento.txt`: evidencia de la recepción del evento.
- `registro-bd.txt`: evidencia del grupo almacenado en PostgreSQL.
- `respuesta-api.json`: respuesta obtenida al crear el grupo mediante la API.

Estas evidencias se encuentran en `experimentos/spike-01/evidencias/`.

## 8. Conclusión

El Spike permitió comprobar que la comunicación mediante eventos internos de Spring es viable para NOVI y puede reducir el acoplamiento entre sus módulos.

También permitió identificar problemas relacionados con las fechas de creación y el manejo de las transacciones. Por esta razón, se recomienda adoptar esta alternativa aplicando los ajustes identificados y verificándolos antes de fusionar los cambios a la rama `main`.
