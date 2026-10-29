package com.example.educacaocontinuadagamificada.gamificacao.application.port.in;

import java.util.Set;

public record ConclusaoCursoResultado(
        Long alunoId,
        String cursoId,
        boolean jaRegistrada,
        int moedasGanhas,
        int cursosLiberadosGanhos,
        Set<String> badgesNovas,
        int moedasTotal,
        int cursosLiberadosTotal
) {
}
