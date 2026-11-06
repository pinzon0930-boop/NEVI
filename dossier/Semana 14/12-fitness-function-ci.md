# Fitness Function en CI — NEVI

**Proyecto:** NEVI — Network of Virtual Interaction  
**Semana:** 14 · Corte 3  
**Tiempo estimado:** 120 min  
**Autor:** Nixson Camilo Pinzón Muñoz  

---

## ¿Qué es una fitness function?

Una fitness function es una prueba automática que verifica que el sistema cumple una propiedad arquitectónica. No prueba que el código hace lo que debe hacer (eso es un test unitario), sino que el código está estructurado como debe estar.

La diferencia práctica: si un desarrollador agrega un endpoint de administración sin el `@PreAuthorize` correcto, el sistema va a funcionar — pero va a tener una vulnerabilidad de seguridad. Un test unitario normal no lo detecta. Una fitness function sí.

**En NEVI usamos ArchUnit**, una librería de Java que permite escribir estas reglas como tests JUnit normales.

---

## Reglas que se verifican

### Regla 1 — Endpoints admin requieren `@PreAuthorize`

**Archivo:** `NeviSecurityArchitectureTest.java` → método `adminEndpointsMustHavePreAuthorize()`

**¿Qué regla protege?**  
Que un estudiante no pueda acceder a un endpoint de administración usando su propio JWT válido.

**¿Qué viola esta regla?**
```java
// En cualquier clase del paquete com.nevi.controller.admin
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    // ❌ ESTO ROMPE LA FITNESS FUNCTION:
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.findAll(); // Cualquier usuario autenticado puede llamar esto
    }

    // ✅ ESTO PASA LA FITNESS FUNCTION:
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.findAll();
    }
}
```

**Mensaje de error cuando falla:**
```
Architecture Violation [Priority: MEDIUM] - Rule 'methods that are declared in classes 
that reside in a package '..admin..' and are annotated with @GetMapping [...] should be 
annotated with @PreAuthorize' was violated (1 times):
Method <com.nevi.controller.admin.AdminController.getAllUsers()> does not 
have @PreAuthorize annotation

because: Todo endpoint en el paquete 'admin' DEBE tener @PreAuthorize. Sin esta 
anotación, cualquier usuario autenticado puede acceder a funcionalidad de 
administración (Elevación de Privilegios - STRIDE E, OWASP A01).
```

---

### Regla 2 — Controllers no usan repositorios directamente

**Archivo:** `NeviSecurityArchitectureTest.java` → método `controllersMustNotDependOnRepositories()`

**¿Qué regla protege?**  
Que la verificación de ownership (que el recurso pertenece al usuario del JWT) no se pueda saltar.

**¿Qué viola esta regla?**
```java
// ❌ ESTO ROMPE LA FITNESS FUNCTION:
@RestController
public class SubmissionController {
    
    @Autowired
    private SubmissionRepository submissionRepository; // ← Acceso directo al repo

    @PutMapping("/submissions/{id}")
    public Submission update(@PathVariable Long id, @RequestBody Submission body) {
        // No hay verificación de que el id pertenece al usuario del JWT
        return submissionRepository.save(body); // IDOR posible
    }
}

// ✅ ESTO PASA LA FITNESS FUNCTION:
@RestController
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService; // ← Usa el servicio

    @PutMapping("/submissions/{id}")
    public Submission update(@PathVariable Long id, @RequestBody Submission body,
                              @AuthenticationPrincipal UserDetails user) {
        // SubmissionService.update() verifica internamente que id pertenece al user
        return submissionService.update(id, body, user.getUsername());
    }
}
```

---

### Reglas 3 y 4 — Separación de capas

Estas reglas no son de seguridad directa pero mantienen la arquitectura limpia, lo que hace más difícil introducir vulnerabilidades accidentalmente.

- **Regla 3:** Los servicios no importan clases de `javax.servlet` ni `jakarta.servlet`
- **Regla 4:** Las clases de seguridad (`JwtAuthFilter`, etc.) no importan servicios de negocio

---

## Pipeline de CI — Cómo funciona

**Archivo:** `.github/workflows/fitness-functions.yml`

Cuando alguien hace un Pull Request a `main` o `develop`:

```
Desarrollador abre PR
        │
        ▼
GitHub Actions se activa automáticamente
        │
        ├── Job 1: "architecture-fitness"
        │     ├── Compilar backend
        │     └── Ejecutar NeviSecurityArchitectureTest
        │           ├── ✅ Pasa → continúa
        │           └── ❌ Falla → PR BLOQUEADO con mensaje de error
        │
        └── Job 2: "security-scan"
              ├── Semgrep: buscar credenciales hardcodeadas
              ├── Verificar que .env no está en el commit
              └── Verificar que application.yml no tiene passwords directos
```

Si cualquiera de los jobs falla, el PR no puede mergearse hasta que se corrija la violación.

---

## Cómo demostrar la fitness function ante el docente

### Demostración 1 — Mostrar que pasa (estado normal)

```bash
# Desde la raíz del proyecto
cd backend
mvn test -Dtest=NeviSecurityArchitectureTest

# Resultado esperado:
# [INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

### Demostración 2 — Introducir una violación intencionalmente

Agregar un endpoint en el paquete admin sin `@PreAuthorize`:

```java
// backend/src/main/java/com/nevi/controller/admin/DemoViolationController.java
package com.nevi.controller.admin;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/demo")
public class DemoViolationController {

    // ← Sin @PreAuthorize: esto viola la Regla 1
    @GetMapping("/violation")
    public String violacion() {
        return "Este endpoint no tiene @PreAuthorize — cualquier usuario puede llamarlo";
    }
}
```

Correr el test de nuevo:

```bash
mvn test -Dtest=NeviSecurityArchitectureTest

# Resultado:
# [ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0
# 
# Architecture Violation - Rule 'methods [...] in package '..admin..' [...] 
# should be annotated with @PreAuthorize' was violated (1 times):
# Method <com.nevi.controller.admin.DemoViolationController.violacion()> 
# does not have @PreAuthorize annotation
#
# because: Todo endpoint en el paquete 'admin' DEBE tener @PreAuthorize...
# [INFO] BUILD FAILURE
```

**La fitness function detecta la violación de forma automática y clara.**

### Demostración 3 — Mostrar el CI en GitHub Actions

En la pestaña "Actions" del repositorio, se puede ver el historial de ejecuciones:
- ✅ Verde: todas las reglas pasaron — el PR puede mergearse
- ❌ Rojo: alguna regla fue violada — el PR está bloqueado con el detalle exacto

---

## Configuración necesaria en el proyecto NEVI

Para que la fitness function funcione, el backend necesita estas dependencias en `pom.xml`:

```xml
<!-- ArchUnit para fitness functions de arquitectura -->
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.3.0</version>
    <scope>test</scope>
</dependency>
```

Y copiar `NeviSecurityArchitectureTest.java` a:
```
backend/src/test/java/com/nevi/architecture/NeviSecurityArchitectureTest.java
```

**Nota:** Ajustar el valor de `BASE_PACKAGE` en la clase al paquete base real del proyecto NEVI.

---

## Qué regla protege y qué pasa en CI si se viola

| Regla | Amenaza que protege | Qué pasa en CI si se viola |
|---|---|---|
| Endpoints admin con `@PreAuthorize` | Elevación de Privilegios (STRIDE E, OWASP A01) | El job `architecture-fitness` falla → PR bloqueado |
| Controllers no usan repos directamente | IDOR por saltar ownership check (OWASP A01) | El job `architecture-fitness` falla → PR bloqueado |
| Servicios sin clases HTTP | Separación de capas | El job `architecture-fitness` falla → PR bloqueado |
| `.env` no commiteado | Exposición de credenciales (OWASP A02) | El job `security-scan` falla → PR bloqueado |
| Sin passwords en application.yml | Credenciales hardcodeadas (OWASP A02) | El job `security-scan` falla → PR bloqueado |

---

*Documento de Semana 14 — Corte 3*  
*Universidad Jorge Tadeo Lozano — Arquitectura de Software*
