# Evidencia: fitness function de arquitectura (ArchUnit)

## Objetivo

Demostrar que la restriccion arquitectonica ejecutable definida en
`LayeringArchitectureTest` (`backend/src/test/java/com/nevi/architecture/LayeringArchitectureTest.java`)
realmente **falla el pipeline de CI** cuando se introduce una violacion real,
y vuelve a pasar una vez corregida. Esto cierra la brecha senalada por el
tutor: contar con una regla de arquitectura ejecutada automaticamente
(fitness function), no solo documentada.

## Regla protegida

Definida en `LayeringArchitectureTest`, protege el estilo en capas elegido
en `ADR-001-estilo-arquitectonico.md`:

> Los controllers deben acceder a los datos siempre a traves de la capa de
> servicio (`com.nevi.service`), nunca hablando directo con un `Repository`.

```java
ArchRule regla = noClasses()
    .that().resideInAPackage("..controller..")
    .should().dependOnClassesThat().resideInAPackage("..repository..")
    .because("ADR-001 exige que el acceso a datos pase siempre por com.nevi.service");
```

## Paso 1 -- Violacion deliberada (commit `d027cc8`)

Se agrego temporalmente a `GrupoController` un campo `GrupoRepository`
y un endpoint `GET /api/grupos/debug-count` que lo usa directamente,
saltandose `GrupoService`:

```java
private final GrupoRepository grupoRepository; // VIOLACION DELIBERADA

@GetMapping("/debug-count")
public ResponseEntity<Long> debugCount() {
    return ResponseEntity.ok(grupoRepository.count());
}
```

**Commit:** `d027cc8` -- *test(arquitectura): introduce violacion deliberada controller->repository*

## Paso 2 -- CI en rojo (run del commit de merge `1da5ba6`, GitHub Actions, 2026-09-18)

El job de backend (`mvn test --batch-mode`) fallo exactamente en
`LayeringArchitectureTest`, con 3 violaciones detectadas automaticamente:

```
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 1.567 s <<< FAILURE! -- in com.nevi.architecture.LayeringArchitectureTest
[ERROR] com.nevi.architecture.LayeringArchitectureTest.controllersNoDebenAccederDirectamenteARepositories -- Time elapsed: 1.545 s <<< FAILURE!
java.lang.AssertionError:
Architecture Violation [Priority: MEDIUM] - Rule 'no classes that reside in a package '..controller..' should depend on classes that reside in a package '..repository..', because ADR-001 exige que el acceso a datos pase siempre por com.nevi.service' was violated (3 times):
Constructor <com.nevi.controller.GrupoController.<init>(com.nevi.service.GrupoService, com.nevi.repository.GrupoRepository)> has parameter of type <com.nevi.repository.GrupoRepository> in (GrupoController.java:0)
Field <com.nevi.controller.GrupoController.grupoRepository> has type <com.nevi.repository.GrupoRepository> in (GrupoController.java:0)
Method <com.nevi.controller.GrupoController.debugCount()> calls method <com.nevi.repository.GrupoRepository.count()> in (GrupoController.java:63)
...
[INFO] BUILD FAILURE
```

Resultado: pipeline en rojo, causado exclusivamente por la regla de
arquitectura (los 4 tests de `AuthFlowIntegrationTest` siguieron pasando
en el mismo run).

## Paso 3 -- Reversion (commit siguiente)

Se elimina el campo, el import y el endpoint temporal de
`GrupoController`, dejando el controller solo dependiendo de
`GrupoService`, tal como exige ADR-001.

## Paso 4 -- CI en verde

Tras la reversion, el mismo job de backend vuelve a pasar, confirmando
que la fitness function detecta violaciones reales y no genera falsos
positivos sobre el codigo correcto.

## Conclusion

La restriccion arquitectonica no es solo un documento: es un fitness
function ejecutado en cada push/PR que bloquea automaticamente cualquier
violacion de la separacion de capas definida en ADR-001. Ciclo
rojo-verde verificado en GitHub Actions el 2026-09-18.
