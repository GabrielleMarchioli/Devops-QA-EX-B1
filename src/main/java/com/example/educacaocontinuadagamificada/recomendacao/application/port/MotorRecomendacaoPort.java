package com.example.educacaocontinuadagamificada.recomendacao.application.port;

import com.example.educacaocontinuadagamificada.recomendacao.domain.Recomendacao;

import java.util.List;

/** RF03: porta de saida para o motor de recomendacao (ML). */
public interface MotorRecomendacaoPort {

    List<Recomendacao> recomendar(Long alunoId, int limite);
}
