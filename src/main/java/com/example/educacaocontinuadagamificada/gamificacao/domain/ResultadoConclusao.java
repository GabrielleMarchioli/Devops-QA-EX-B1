package com.example.educacaocontinuadagamificada.gamificacao.domain;

import java.util.Set;

/** O que a conclusao de um curso gerou para o aluno. */
public record ResultadoConclusao(
        String cursoId,
        double media,
        int moedasGanhas,
        int cursosLiberadosGanhos,
        Set<Badge> badgesNovas
) {
}
