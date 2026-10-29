package com.example.educacaocontinuadagamificada.arquitetura;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Desafios 1 e 2: as decisoes de modularizacao e de Ports and Adapters viram testes.
 * Se alguem acoplar o dominio ao Spring/JPA, a esteira quebra (Jenkinsfile, estagio Testes).
 */
public class ArquiteturaTest {

    private static final String BASE = "com.example.educacaocontinuadagamificada";
    private static JavaClasses classes;

    @BeforeAll
    static void importar() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);
    }

    @Test
    void dominioEAplicacaoNaoDependemDeFramework() {
        noClasses().that().resideInAnyPackage(
                        "..gamificacao.domain..", "..gamificacao.application..",
                        "..recomendacao.domain..", "..recomendacao.application..",
                        "..eventos.application..", "..iot.application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "jakarta.persistence..", "io.micrometer..")
                .check(classes);
    }

    @Test
    void dominioNaoConheceAdapters() {
        noClasses().that().resideInAPackage("..gamificacao.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..adapter..", "..config..", "..aluno..")
                .check(classes);
    }

    @Test
    void moduloAlunoLegadoNaoDependeDosModulosNovos() {
        noClasses().that().resideInAPackage(BASE + ".aluno..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        BASE + ".gamificacao..", BASE + ".recomendacao..", BASE + ".eventos..", BASE + ".iot..")
                .check(classes);
    }

    @Test
    void outrosModulosUsamGamificacaoApenasPelasPortas() {
        noClasses().that().resideInAnyPackage(BASE + ".iot..", BASE + ".recomendacao..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        BASE + ".gamificacao.adapter..", BASE + ".gamificacao.domain..")
                .check(classes);
    }
}
