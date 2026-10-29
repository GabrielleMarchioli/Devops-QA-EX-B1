package com.example.educacaocontinuadagamificada.gamificacao.application.port.out;

import java.time.Instant;

/** Porta de saida: historico de conclusoes, garante idempotencia aluno+curso. */
public interface ConclusaoCursoRepository {

    boolean existe(Long alunoId, String cursoId);

    void registrar(Long alunoId, String cursoId, double media, Instant ocorridoEm);
}
