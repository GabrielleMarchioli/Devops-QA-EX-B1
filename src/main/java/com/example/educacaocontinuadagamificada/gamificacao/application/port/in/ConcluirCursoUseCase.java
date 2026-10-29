package com.example.educacaocontinuadagamificada.gamificacao.application.port.in;

/** Porta de entrada: concluir um curso e calcular recompensas (RF02). */
public interface ConcluirCursoUseCase {

    ConclusaoCursoResultado executar(ConcluirCursoComando comando);
}
