package com.example.educacaocontinuadagamificada.gamificacao.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Testes da AC1 (BDDs da equipe), preservados apos a reorganizacao modular. */
public class AlunoTest {

    // BDD 1 - Gabrielle
    @Test
    public void deveLiberarTresCursosQuandoMediaForMaiorQueSete() {
        Aluno aluno = new Aluno("Gabrielle");
        aluno.avaliarConclusaoCurso(8.0);
        assertEquals(3, aluno.getCursosLiberados());
    }

    // BDD 2 - Afonso
    @Test
    public void naoDeveLiberarCursosQuandoMediaForIgualASete() {
        Aluno aluno = new Aluno("Afonso");
        aluno.avaliarConclusaoCurso(7.0);
        assertEquals(0, aluno.getCursosLiberados());
    }

    // BDD 3 - Giulianno
    @Test
    public void naoDeveLiberarCursosQuandoMediaForMenorQueSete() {
        Aluno aluno = new Aluno("Giulianno");
        aluno.avaliarConclusaoCurso(6.0);
        assertEquals(0, aluno.getCursosLiberados());
    }

    @Test
    public void deveCriarAlunoComNomeECursosZerados() {
        Aluno aluno = new Aluno("Gabrielle");
        assertEquals("Gabrielle", aluno.getNome());
        assertEquals(0, aluno.getCursosLiberados());
    }
}
