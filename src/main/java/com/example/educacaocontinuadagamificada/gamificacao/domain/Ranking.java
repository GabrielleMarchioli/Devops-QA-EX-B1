package com.example.educacaocontinuadagamificada.gamificacao.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Servico de dominio do ranking (RF01).
 * Criterio: moedas desc, cursos concluidos desc, nome asc.
 * Empate em moedas e cursos concluidos divide a mesma posicao (ranking 1, 1, 3).
 */
public final class Ranking {

    private static final Comparator<Aluno> CRITERIO = Comparator
            .comparingInt(Aluno::getMoedas).reversed()
            .thenComparing(Comparator.comparingInt(Aluno::getCursosConcluidos).reversed())
            .thenComparing(Aluno::getNome, Comparator.nullsLast(String::compareTo));

    private Ranking() {
    }

    public static List<PosicaoRanking> classificar(List<Aluno> alunos) {
        List<Aluno> ordenados = new ArrayList<>(alunos);
        ordenados.sort(CRITERIO);

        List<PosicaoRanking> resultado = new ArrayList<>();
        int posicao = 0;
        Aluno anterior = null;
        for (int i = 0; i < ordenados.size(); i++) {
            Aluno atual = ordenados.get(i);
            boolean empatado = anterior != null
                    && anterior.getMoedas() == atual.getMoedas()
                    && anterior.getCursosConcluidos() == atual.getCursosConcluidos();
            if (!empatado) {
                posicao = i + 1;
            }
            resultado.add(new PosicaoRanking(posicao, atual.getId(), atual.getNome(),
                    atual.getMoedas(), atual.getCursosConcluidos()));
            anterior = atual;
        }
        return resultado;
    }
}
