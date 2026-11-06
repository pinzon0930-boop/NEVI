# Condiciones del Spike 1 – Semana 10

## 1. Objetivo

Comprobar si NOVI puede utilizar eventos para comunicar acciones entre sus componentes, tomando como ejemplo la creación de un grupo académico mediante el evento `GrupoCreado`.

## 2. Escenario de prueba

Cuando un usuario cree un grupo académico en NOVI, el sistema deberá generar el evento `GrupoCreado` con la información básica del grupo y permitir que otro componente lo reciba.

## 3. Condiciones iniciales

- Trabajar en la rama experimental `spikes`.
- Utilizar el proyecto NOVI existente.
- Identificar cómo funciona actualmente la creación de grupos en el backend.
- No modificar la rama `main`.
- Registrar los cambios y resultados del experimento.

## 4. Tiempo límite

180 minutos (3 horas) para ejecutar el Spike.

## 5. Criterios de éxito

- Se genera el evento `GrupoCreado` al crear un grupo.
- El evento contiene la información básica del grupo.
- Otro componente recibe el evento.
- La información recibida coincide con el grupo creado.

## 6. Fuera del alcance

- Implementar todos los eventos del catálogo.
- Transformar toda la arquitectura de NOVI.
- Implementar una arquitectura completa de microservicios.
- Desarrollar un sistema avanzado de mensajería.

## 7. Resultado esperado

Determinar si la comunicación mediante eventos es viable para NOVI, utilizando la creación de grupos como prueba.
