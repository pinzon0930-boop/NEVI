# Hipótesis, alcance y criterio de éxito del Spike 1

## Hipótesis

Se plantea que NOVI puede utilizar eventos para comunicar acciones importantes entre sus módulos. Para comprobarlo, se propone probar el evento `GrupoCreado`, de manera que cuando se cree un grupo académico se genere un evento con su información básica y pueda ser recibido por otro componente del sistema.

## Alcance

El Spike 1 se enfocará en una prueba técnica pequeña para comprobar la comunicación mediante el evento `GrupoCreado`.

Se realizarán las siguientes actividades:

- Identificar el proceso de creación de grupos en el backend de NOVI.
- Definir la estructura del evento `GrupoCreado`.
- Implementar una prueba que permita generar el evento cuando se cree un grupo.
- Comprobar que otro componente pueda recibir la información del evento.

**Fuera del alcance:**

- Implementar todos los eventos del catálogo.
- Modificar toda la arquitectura de NOVI.
- Implementar una arquitectura completa de microservicios.
- Desarrollar un sistema avanzado de mensajería entre módulos.

## Criterio de éxito

El Spike se considerará exitoso si se logra:

1. Generar el evento `GrupoCreado` al registrar un nuevo grupo.
2. Enviar la información básica del grupo dentro del evento.
3. Recibir correctamente el evento desde otro componente.
4. Comprobar que la información recibida corresponde al grupo creado.

Si no se logra alguno de estos resultados, se documentarán los problemas encontrados y se evaluará si es necesario cambiar la estrategia de comunicación.

## Resultado esperado

Determinar si el uso de eventos es una alternativa viable para comunicar los módulos de NOVI, tomando como ejemplo el proceso de creación de grupos académicos.
