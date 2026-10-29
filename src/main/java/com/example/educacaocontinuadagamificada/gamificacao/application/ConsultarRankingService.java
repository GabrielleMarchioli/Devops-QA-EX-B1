package com.example.educacaocontinuadagamificada.gamificacao.application;

import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConsultarRankingUseCase;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.out.AlunoGamificacaoRepository;
import com.example.educacaocontinuadagamificada.gamificacao.domain.PosicaoRanking;
import com.example.educacaocontinuadagamificada.gamificacao.domain.Ranking;

import java.util.List;

public class ConsultarRankingService implements ConsultarRankingUseCase {

    static final int LIMITE_MAXIMO = 100;

    private final AlunoGamificacaoRepository alunos;

    public ConsultarRankingService(AlunoGamificacaoRepository alunos) {
        this.alunos = alunos;
    }

    @Override
    public List<PosicaoRanking> topN(int limite) {
        int limiteSeguro = Math.max(1, Math.min(limite, LIMITE_MAXIMO));
        return Ranking.classificar(alunos.topPorMoedas(limiteSeguro));
    }
}
