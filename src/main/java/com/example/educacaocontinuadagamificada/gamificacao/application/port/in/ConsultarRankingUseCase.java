package com.example.educacaocontinuadagamificada.gamificacao.application.port.in;

import com.example.educacaocontinuadagamificada.gamificacao.domain.PosicaoRanking;

import java.util.List;

/** Porta de entrada: consultar o ranking (RF01). */
public interface ConsultarRankingUseCase {

    List<PosicaoRanking> topN(int limite);
}
