package com.nevi.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Fitness function de seguridad para NEVI.
 *
 * Esta clase verifica que las reglas de seguridad arquitectónicas se cumplen
 * en el código fuente. Se ejecuta en CI en cada Pull Request.
 *
 * Para demostrar una violación: agregar un método en un @RestController
 * dentro del paquete .admin. sin @PreAuthorize — el test falla con mensaje claro.
 *
 * Para ejecutar localmente:
 *   mvn test -pl backend -Dtest=NeviSecurityArchitectureTest
 */
class NeviSecurityArchitectureTest {

    private static JavaClasses importedClasses;

    // Ajustar al paquete base del backend de NEVI
    private static final String BASE_PACKAGE = "com.nevi";

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Regla 1: Todo endpoint en el paquete .admin. debe tener @PreAuthorize
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Regla de seguridad: endpoints admin requieren @PreAuthorize")
    void adminEndpointsMustHavePreAuthorize() {
        /*
         * ¿Qué protege esta regla?
         * Evita que un estudiante acceda a endpoints de administración
         * usando un JWT válido pero sin el rol ADMIN.
         *
         * ¿Qué pasa si se viola?
         * Un método en un controlador del paquete .admin. no tiene
         * @PreAuthorize — cualquier usuario autenticado puede llamarlo.
         *
         * Ejemplo de violación:
         *   @GetMapping("/users")
         *   public List<User> getAllUsers() { ... }  // ← FALTA @PreAuthorize
         *
         * Ejemplo correcto:
         *   @PreAuthorize("hasRole('ADMIN')")
         *   @GetMapping("/users")
         *   public List<User> getAllUsers() { ... }
         */
        ArchRule regla = methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..admin..")
                .and().areAnnotatedWith(GetMapping.class)
                    .or().areAnnotatedWith(PostMapping.class)
                    .or().areAnnotatedWith(PutMapping.class)
                    .or().areAnnotatedWith(DeleteMapping.class)
                    .or().areAnnotatedWith(PatchMapping.class)
                .should().beAnnotatedWith(PreAuthorize.class)
                .because(
                    "Todo endpoint en el paquete 'admin' DEBE tener @PreAuthorize. " +
                    "Sin esta anotación, cualquier usuario autenticado puede acceder " +
                    "a funcionalidad de administración (Elevación de Privilegios - STRIDE E, OWASP A01)."
                );

        regla.check(importedClasses);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Regla 2: Los controllers no deben acceder directamente al repositorio
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Regla de arquitectura: controllers no usan repositorios directamente")
    void controllersMustNotDependOnRepositories() {
        /*
         * ¿Qué protege esta regla?
         * Si un controller accede directamente al repositorio, se salta
         * la capa de servicio donde debería estar la verificación de ownership.
         *
         * ¿Qué pasa si se viola?
         * Un controller llama a groupRepository.findById() directamente
         * sin pasar por GroupService.getGroupForUser(), que es donde
         * se verifica que el grupo pertenece al usuario del JWT.
         */
        ArchRule regla = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")
                .because(
                    "Los controllers no deben acceder a repositorios directamente. " +
                    "La verificación de ownership (IDOR - OWASP A01) vive en la capa " +
                    "de servicio — saltársela expone datos de otros usuarios."
                );

        regla.check(importedClasses);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Regla 3: Los servicios no deben depender de clases HTTP (Request/Response)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Regla de arquitectura: servicios no dependen de clases HTTP")
    void servicesMustNotDependOnHttpClasses() {
        /*
         * ¿Qué protege esta regla?
         * Mantiene la separación de capas. Si un servicio conoce el
         * HttpServletRequest, es más difícil probarlo y más fácil que
         * procese datos del request sin validación adecuada.
         */
        ArchRule regla = noClasses()
                .that().resideInAPackage("..service..")
                .should().dependOnClassesThat()
                .resideInAPackage("javax.servlet..")
                .orShould().dependOnClassesThat()
                .resideInAPackage("jakarta.servlet..")
                .because(
                    "Los servicios no deben depender de clases HTTP del servlet container. " +
                    "Esta separación garantiza que la lógica de negocio es " +
                    "independiente del protocolo y más fácil de probar."
                );

        regla.check(importedClasses);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Regla 4: Nada en el paquete .security. depende de las capas de negocio
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Regla de arquitectura: seguridad no depende de servicios de negocio")
    void securityLayerMustNotDependOnBusinessServices() {
        /*
         * ¿Qué protege esta regla?
         * Evita dependencias circulares entre la capa de seguridad y los
         * servicios de negocio. JwtAuthFilter debe ser independiente — solo
         * verifica el token, no ejecuta lógica de negocio.
         */
        ArchRule regla = noClasses()
                .that().resideInAPackage("..security..")
                .should().dependOnClassesThat().resideInAPackage("..service..")
                .because(
                    "La capa de seguridad (JwtAuthFilter, etc.) no debe depender de " +
                    "servicios de negocio. Su única responsabilidad es verificar el JWT " +
                    "y poblar el SecurityContext — nada más."
                );

        regla.check(importedClasses);
    }
}
