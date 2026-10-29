package com.example.educacaocontinuadagamificada.recomendacao.application;

import com.example.educacaocontinuadagamificada.recomendacao.application.port.AssistentePort;
import com.example.educacaocontinuadagamificada.recomendacao.application.port.IaIndisponivelException;
import com.example.educacaocontinuadagamificada.recomendacao.application.port.MotorRecomendacaoPort;
import com.example.educacaocontinuadagamificada.recomendacao.domain.Recomendacao;
import com.example.educacaocontinuadagamificada.recomendacao.domain.RespostaAssistente;
import com.example.educacaocontinuadagamificada.recomendacao.domain.ResultadoRecomendacao;

import java.util.List;

/**
 * Define o comportamento degradado aceitavel (ADR-003):
 * IA fora do ar nunca gera erro para o aluno nem bloqueia o fluxo essencial do Core.
 */
public class RecomendacaoService {

    static final String MENSAGEM_ASSISTENTE_INDISPONIVEL =
            "O assistente está temporariamente indisponível. Suas atividades, moedas e conquistas seguem normalmente.";

    /** Catalogo padrao (cursos mais procurados), servido pelo proprio Core quando a IA falha. */
    static final List<Recomendacao> CATALOGO_PADRAO = List.of(
            new Recomendacao("JAVA-101", "Fundamentos de Java", 0.0, "catalogo padrao"),
            new Recomendacao("GIT-101", "Git e versionamento", 0.0, "catalogo padrao"),
            new Recomendacao("SQL-101", "SQL para iniciantes", 0.0, "catalogo padrao"));

    private final MotorRecomendacaoPort motor;
    private final AssistentePort assistente;

    public RecomendacaoService(MotorRecomendacaoPort motor, AssistentePort assistente) {
        this.motor = motor;
        this.assistente = assistente;
    }

    public ResultadoRecomendacao recomendar(Long alunoId, int limite) {
        int limiteSeguro = Math.max(1, Math.min(limite, 10));
        try {
            return new ResultadoRecomendacao(alunoId, motor.recomendar(alunoId, limiteSeguro), false, "ia-service");
        } catch (IaIndisponivelException e) {
            List<Recomendacao> fallback = CATALOGO_PADRAO.subList(0, Math.min(limiteSeguro, CATALOGO_PADRAO.size()));
            return new ResultadoRecomendacao(alunoId, fallback, true, "fallback-core");
        }
    }

    public RespostaAssistente perguntar(String pergunta) {
        try {
            return assistente.perguntar(pergunta);
        } catch (IaIndisponivelException e) {
            return new RespostaAssistente(MENSAGEM_ASSISTENTE_INDISPONIVEL, List.of(), true);
        }
    }
}
