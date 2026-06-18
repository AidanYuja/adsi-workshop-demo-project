package com.example.attendance.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
    packages = "com.example.attendance",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class DomainDependencyTest {

    @ArchTest
    static final ArchRule domain_entities_should_not_depend_on_controllers = noClasses()
        .that().resideInAPackage("..entity..")
        .should().dependOnClassesThat().resideInAPackage("..controller..")
        .allowEmptyShould(true);

    @ArchTest
    static final ArchRule domain_entities_should_not_depend_on_services = noClasses()
        .that().resideInAPackage("..entity..")
        .should().dependOnClassesThat().resideInAPackage("..service..")
        .allowEmptyShould(true);
}
