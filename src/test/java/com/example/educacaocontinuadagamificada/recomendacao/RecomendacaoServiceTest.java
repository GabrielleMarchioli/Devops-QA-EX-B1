package com.example.educacaocontinuadagamificada.recomendacao;

import com.example.educacaocontinuadagamificada.recomendacao.application.RecomendacaoService;
import com.example.educacaocontinuadagamificada.recomendacao.application.port.AssistentePort;
import com.example.educacaocontinuadagamificada.recomendacao.application.port.IaIndisponivelException;
import com.example.educacaocontinuadagamificada.recomendacao.application.port.MotorRecomendacaoPort;
import com.example.educacaocontinuadagamificada.recomendacao.domain.Recomendacao;
import com.example.educacaocontinuadagamificada.recomendacao.domain.RespostaAssistente;
import com.example.educacaocontinuadagamificada.recomendacao.domain.ResultadoRecomendacao;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Desafio 3 (evidencia): comportamento degradado quando o ia-service esta fora do ar. */
public class RecomendacaoServiceTest {

    private static final MotorRecomendacaoPort MOTOR_OK = (alunoId, limite) ->
            List.of(new Recomendacao("PY-201", "Python para dados", 0.91, "afinidade"));
    private static final AssistentePort ASSISTENTE_OK = pergunta ->
            new RespostaAssistente("Resposta da IA", List.of("faq-moedas"), false);
    private static final MotorRecomendacaoPort MOTOR_FORA = (alunoId, limite) -> {
        throw new IaIndisponivelException("Connection refused", null);
    };
    private static final AssistentePort ASSISTENTE_FORA = pergunta -> {
        throw new IaIndisponivelException("timeout", null);
    };

    @Test
    public void usaIaQuandoDisponivel() {
        ResultadoRecomendacao r = new RecomendacaoService(MOTOR_OK, ASSISTENTE_OK).recomendar(1L, 3);

        assertFalse(r.degradado());
        assertEquals("ia-service", r.origem());
        assertEquals("PY-201", r.itens().get(0).cursoId());
    }

    @Test
    public void iaForaDoArDevolveCatalogoPadraoSemErro() {
        ResultadoRecomendacao r = new RecomendacaoService(MOTOR_FORA, ASSISTENTE_FORA).recomendar(1L, 2);

        assertTrue(r.degradado());
        assertEquals("fallback-core", r.origem());
        assertEquals(2, r.itens().size());
    }

    @Test
    public void assistenteForaDoArRespondeMensagemAmigavel() {
        RespostaAssistente r = new RecomendacaoService(MOTOR_FORA, ASSISTENTE_FORA).perguntar("Como ganho moedas?");

        assertTrue(r.degradado());
        assertTrue(r.resposta().contains("temporariamente indisponível"));
        assertTrue(r.fontes().isEmpty());
    }

    @Test
    public void assistenteDisponivelRepassaResposta() {
        RespostaAssistente r = new RecomendacaoService(MOTOR_OK, ASSISTENTE_OK).perguntar("Como ganho moedas?");

        assertFalse(r.degradado());
        assertEquals("Resposta da IA", r.resposta());
    }
}
