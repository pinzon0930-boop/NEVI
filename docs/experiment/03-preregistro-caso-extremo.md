# 03 -- Preregistro de medicion (caso extremo) -- NEVI

> Al igual que en `01-preregistro.md`, este documento se escribe **antes**
> de correr la medicion. Extiende el experimento de ESC-02 con una semilla
> de datos deliberadamente sesgada, en vez de la semilla uniforme original.

**Fecha de preregistro:** 2026-09-18
**Escenario medido:** ESC-02 / QA-02 -- Latencia p95 en `GET /api/grupos`,
bajo una distribucion de datos realista (sesgada), no uniforme.

---

## Por que un segundo experimento

El preregistro original (`01-preregistro.md`) uso una semilla uniforme:
4 estudiantes, cada uno miembro de "al menos 3 grupos". Es una semilla
demasiado pequena y pareja para exponer problemas que solo aparecen con
datos reales, donde unos pocos usuarios concentran mucho mas actividad
que el resto (distribucion tipo Pareto / 80-20).

Al revisar el codigo para preparar esta semilla se encontro lo siguiente
en `GrupoService.misGrupos()` (backend/src/main/java/com/nevi/service/GrupoService.java):

```java
// Estudiante: grupos a traves de la tabla group_members.
return memberRepository.findByStudent(user)
    .stream()
    .map(gm -> GrupoResponse.desde(gm.getGrupo()))
    .toList();
```

`GroupMemberRepository.findByStudent(User)` es una consulta derivada de
Spring Data **sin** `JOIN FETCH`. Esto significa que `gm.getGrupo()` se
resuelve con **carga perezosa (lazy loading)**: por cada membresia
devuelta, Hibernate dispara una consulta SQL adicional para traer ese
`Grupo` (y `GrupoResponse.desde()` tambien accede a `g.getTeacher()`,
otra relacion `@ManyToOne(LAZY)`, sumando una tercera consulta por
membresia). Es decir, para un estudiante con **N** membresias, el
endpoint ejecuta aproximadamente **1 + 2N** consultas SQL secuenciales en
lugar de una sola consulta con `JOIN`. Este es el patron clasico conocido
como **problema N+1**.

Con la semilla original (N=3 por estudiante) este costo es insignificante
y por eso el baseline (`02-resultados-baseline.md`) no lo detecto -- de
hecho el cuello de botella hipotetizado (contencion del pool HikariCP) ni
siquiera llego a manifestarse porque las consultas eran demasiado
livianas. El repositorio incluso tiene ya escrita, sin usar, una version
con `JOIN FETCH` (`findByStudentOrderByGrupoCreatedAtDesc`), lo que
sugiere que el equipo era consciente del riesgo pero el metodo optimizado
nunca se conecto al servicio (y ademas esa consulta tiene un bug propio:
le falta el `WHERE` que filtre por el estudiante recibido como parametro,
por lo que si se usara hoy devolveria las membresias de TODOS los
estudiantes).

## Hipotesis

Con una semilla sesgada 80/20 (ver `measurements/caso-extremo/script-caso-extremo.js`
para el detalle exacto) que incluye un estudiante "extremo" miembro de
**65** grupos, y midiendo `GET /api/grupos` autenticado como ese
estudiante bajo 10 VUs concurrentes durante 60 segundos, se espera:

- **Latencia p95:** muy por encima del rango 300-800 ms medido en el
  baseline uniforme -- se predice **p95 > 800 ms**, posiblemente del
  orden de varios segundos, como consecuencia directa del problema N+1
  (66 estudiantes... 66 consultas de Grupo + 66 de teacher, mas la
  consulta inicial, ~131 consultas secuenciales por request).
- **Tasa de error HTTP:** se espera que se mantenga < 5 % (la respuesta
  deberia seguir siendo correcta, solo lenta), salvo que el pool de
  conexiones o el timeout del cliente se agoten primero.

**Razonamiento:** a diferencia del preregistro original (que asumia un
JOIN eficiente y atribuia el riesgo a la contencion del pool), esta
hipotesis se basa en lectura directa del codigo fuente, no en suposicion.
Es falsable: si p95 se mantiene <= 800 ms pese al caso extremo, la
hipotesis del problema N+1 como cuello de botella dominante queda
refutada (y habria que buscar otra explicacion, o concluir que Hibernate
o el pool absorben ese costo mejor de lo esperado).

## Distribucion de la semilla (80/20 con caso extremo)

| Grupo de estudiantes | Cantidad | Grupos por estudiante | Membresias totales | % del total |
|---|---|---|---|---|
| Extremo               | 1  | 65   | 65  | 65 % |
| Activos               | 3  | 5    | 15  | 15 % |
| Normales              | 16 | 1-2  | 20  | 20 % |
| **Total**             | **20** | -- | **100** | **100 %** |

El 20 % de los estudiantes (extremo + activos = 4 de 20) concentra el
80 % de las membresias (80 de 100); dentro de ese 20 %, el estudiante
extremo por si solo concentra el 65 %, cumpliendo el requisito de
"caso extremo" ademas de la proporcion 80/20 agregada.

Semilla creada por 1 profesor con 70 grupos en total (`Carga-01` a
`Carga-70`), suficientes para que el estudiante extremo pueda unirse a
65 sin repetir.

## Protocolo de medicion

### Herramienta
k6 (la misma version usada en el baseline original)

### Operacion medida
`GET /api/grupos`, autenticado con el token del estudiante
`extremo.carga@nevi-test.com` (65 membresias).

### Configuracion de carga
Identica al baseline original para que los resultados sean comparables:
- VUs: 10
- Duracion: 60 s
- Sin rampa

### Requisito previo: base de datos limpia
Los emails de la semilla son fijos (`profesor.carga@nevi-test.com`,
`extremo.carga@nevi-test.com`, etc.). Antes de correr, resetear el
volumen de PostgreSQL para evitar el error "ya eres miembro de este
grupo" o conflictos de email duplicado en corridas repetidas:

```
docker compose down -v
docker compose up -d --build
```

### Numero de corridas
Minimo 3 corridas (`corrida-01`, `corrida-02`, `corrida-03`), igual que
el baseline. Se reporta la mediana de los p95.

## Criterio de exito / refutacion

La hipotesis (N+1 como cuello de botella dominante) se **confirma** si:
- p95 > 800 ms en las 3 corridas, Y la tasa de error se mantiene < 5 %.

Se **refuta** si:
- p95 <= 800 ms en al menos 2 de las 3 corridas (el N+1 no pesa tanto
  como se penso), o
- la tasa de error >= 5 % (hay un problema distinto, mas grave, como
  timeouts o agotamiento del pool).

En cualquier caso, el resultado se documenta con analisis de causa en
`docs/experiment/04-resultados-caso-extremo.md`.
