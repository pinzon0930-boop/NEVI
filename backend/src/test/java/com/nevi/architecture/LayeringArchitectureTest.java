package com.nevi.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Primera restricción arquitectónica ejecutable del curso.
 *
 * Protege el estilo elegido en ADR-001 (monolito modular organizado por
 * capas: controller -> service -> repository): los controllers deben
 * acceder a los datos SIEMPRE a través de la capa de servicio, nunca
 * hablando directo con un Repository.
 *
 * Para demostrar que esta restricción realmente falla ante una
 * violación (requisito del curso), ver la evidencia en el PR/commit
 * donde se introdujo temporalmente una dependencia controller->repository
 * y el pipeline de CI se puso en rojo.
 */
class LayeringArchitectureTest {

    @Test
    void controllersNoDebenAccederDirectamenteARepositories() {
        JavaClasses clases = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.nevi");

        ArchRule regla = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("..repository..")
            .because("ADR-001 exige que el acceso a datos pase siempre por com.nevi.service");

        regla.check(clases);
    }
}
