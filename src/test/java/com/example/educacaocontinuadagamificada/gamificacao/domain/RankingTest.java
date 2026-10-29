package com.example.educacaocontinuadagamificada.gamificacao.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RankingTest {

    private static Aluno aluno(long id, String nome, int moedas, int concluidos) {
        return Aluno.reconstituir(id, nome, 0, moedas, concluidos, Set.of());
    }

    @Test
    public void ordenaPorMoedasECursosConcluidosComEmpateDividindoPosicao() {
        List<PosicaoRanking> ranking = Ranking.classificar(List.of(
                aluno(1, "Carla", 30, 2),
                aluno(2, "Bruno", 50, 3),
                aluno(3, "Ana", 30, 2),
                aluno(4, "Davi", 30, 4)));

        assertEquals(List.of(2L, 4L, 3L, 1L), ranking.stream().map(PosicaoRanking::alunoId).toList());
        assertEquals(List.of(1, 2, 3, 3), ranking.stream().map(PosicaoRanking::posicao).toList());
    }

    @Test
    public void listaVaziaGeraRankingVazio() {
        assertTrue(Ranking.classificar(List.of()).isEmpty());
    }
}
