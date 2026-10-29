package com.example.educacaocontinuadagamificada.gamificacao.application.port.out;

import com.example.educacaocontinuadagamificada.gamificacao.domain.Aluno;

import java.util.List;
import java.util.Optional;

/** Porta de saida: persistencia do estado de gamificacao do aluno. */
public interface AlunoGamificacaoRepository {

    Optional<Aluno> buscarPorId(Long id);

    Aluno salvar(Aluno aluno);

    /** Top N ja ordenado por moedas: o banco faz o corte para suportar 100 mil alunos. */
    List<Aluno> topPorMoedas(int limite);
}
