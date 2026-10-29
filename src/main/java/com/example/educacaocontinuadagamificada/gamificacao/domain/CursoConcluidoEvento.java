package com.example.educacaocontinuadagamificada.gamificacao.domain;

import java.time.Instant;
import java.util.Set;

/** Evento de dominio publicado apos a conclusao de um curso (ADR-004). */
public record CursoConcluidoEvento(
        String eventoId,
        Long alunoId,
        String cursoId,
        double media,
        int moedasGanhas,
        int cursosLiberadosGanhos,
        Set<Badge> badgesNovas,
        Instant ocorridoEm
) {
    public static final String TIPO = "CursoConcluido";
}
