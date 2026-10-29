package com.example.educacaocontinuadagamificada.gamificacao.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Regras de gamificacao do B1 testadas sem Spring, sem banco e sem mocks. */
public class AlunoConclusaoTest {

    @Test
    public void conclusaoComMediaAltaGeraMoedasBonusCursosEBadges() {
        Aluno aluno = new Aluno("Gabrielle");

        ResultadoConclusao r = aluno.concluirCurso("JAVA-101", 9.5);

        assertEquals(15, r.moedasGanhas());
        assertEquals(3, r.cursosLiberadosGanhos());
        assertEquals(Set.of(Badge.PRIMEIRA_CONCLUSAO, Badge.EXCELENCIA), r.badgesNovas());
        assertEquals(15, aluno.getMoedas());
        assertEquals(1, aluno.getCursosConcluidos());
    }

    @Test
    public void conclusaoComMediaSeteGeraApenasMoedasBase() {
        Aluno aluno = new Aluno("Afonso");

        ResultadoConclusao r = aluno.concluirCurso("GIT-101", 7.0);

        assertEquals(10, r.moedasGanhas());
        assertEquals(0, r.cursosLiberadosGanhos());
        assertEquals(Set.of(Badge.PRIMEIRA_CONCLUSAO), r.badgesNovas());
    }

    @Test
    public void badgeNaoEConcedidaDuasVezesEMaratonistaChegaNaQuintaConclusao() {
        Aluno aluno = new Aluno("Giulianno");
        for (int i = 1; i <= 4; i++) {
            aluno.concluirCurso("C" + i, 6.0);
        }

        ResultadoConclusao quinta = aluno.concluirCurso("C5", 6.0);

        assertEquals(Set.of(Badge.MARATONISTA), quinta.badgesNovas());
        assertEquals(Set.of(Badge.PRIMEIRA_CONCLUSAO, Badge.MARATONISTA), aluno.getBadges());
        assertEquals(50, aluno.getMoedas());
    }

    @Test
    public void reconstituirPreservaEstadoPersistido() {
        Aluno aluno = Aluno.reconstituir(7L, "Ana", 3, 40, 2, Set.of(Badge.PRIMEIRA_CONCLUSAO));

        aluno.concluirCurso("SQL-101", 8.0);

        assertEquals(Long.valueOf(7L), aluno.getId());
        assertEquals(6, aluno.getCursosLiberados());
        assertEquals(55, aluno.getMoedas());
        assertEquals(3, aluno.getCursosConcluidos());
        assertTrue(aluno.getBadges().contains(Badge.PRIMEIRA_CONCLUSAO));
    }

    @Test
    public void mediaForaDoIntervaloOuCursoVazioViolaRegra() {
        Aluno aluno = new Aluno("Gabrielle");
        assertThrows(RegraDeNegocioException.class, () -> aluno.concluirCurso("X", 11.0));
        assertThrows(RegraDeNegocioException.class, () -> aluno.concluirCurso("X", -1.0));
        assertThrows(RegraDeNegocioException.class, () -> aluno.concluirCurso(" ", 8.0));
        assertThrows(RegraDeNegocioException.class, () -> aluno.concluirCurso(null, 8.0));
    }
}
