package com.example.educacaocontinuadagamificada.recomendacao.adapter.out.ia;

import com.example.educacaocontinuadagamificada.recomendacao.application.port.AssistentePort;
import com.example.educacaocontinuadagamificada.recomendacao.application.port.IaIndisponivelException;
import com.example.educacaocontinuadagamificada.recomendacao.application.port.MotorRecomendacaoPort;
import com.example.educacaocontinuadagamificada.recomendacao.domain.Recomendacao;
import com.example.educacaocontinuadagamificada.recomendacao.domain.RespostaAssistente;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Adapter HTTP/JSON (REST sincrono com timeout curto) para o ia-service em FastAPI.
 * Qualquer falha de rede, timeout ou 5xx vira IaIndisponivelException.
 * Metrica: ia_requisicoes_seconds{operacao, resultado} (contagem + latencia).
 */
public class IaHttpAdapter implements MotorRecomendacaoPort, AssistentePort {

    public record ItemIa(String cursoId, String titulo, double score, String motivo) {
    }

    public record RecomendacoesIa(List<ItemIa> itens, String modelo) {
    }

    public record AssistenteIa(String resposta, List<String> fontes) {
    }

    private final RestClient http;
    private final MeterRegistry registry;

    public IaHttpAdapter(RestClient http, MeterRegistry registry) {
        this.http = http;
        this.registry = registry;
    }

    @Override
    public List<Recomendacao> recomendar(Long alunoId, int limite) {
        RecomendacoesIa resposta = medir("recomendacao", () -> http.get()
                .uri("/recomendacoes/{alunoId}?limite={limite}", alunoId, limite)
                .retrieve()
                .body(RecomendacoesIa.class));
        if (resposta == null || resposta.itens() == null) {
            return List.of();
        }
        return resposta.itens().stream()
                .map(i -> new Recomendacao(i.cursoId(), i.titulo(), i.score(), i.motivo()))
                .toList();
    }

    @Override
    public RespostaAssistente perguntar(String pergunta) {
        AssistenteIa resposta = medir("assistente", () -> http.post()
                .uri("/assistente")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("pergunta", pergunta))
                .retrieve()
                .body(AssistenteIa.class));
        if (resposta == null) {
            throw new IaIndisponivelException("ia-service retornou corpo vazio", null);
        }
        return new RespostaAssistente(resposta.resposta(), resposta.fontes(), false);
    }

    private <T> T medir(String operacao, Supplier<T> chamada) {
        Timer.Sample amostra = Timer.start(registry);
        String resultado = "sucesso";
        try {
            return chamada.get();
        } catch (RestClientException e) {
            resultado = "falha";
            throw new IaIndisponivelException("ia-service indisponível: " + e.getMessage(), e);
        } finally {
            amostra.stop(Timer.builder("ia.requisicoes")
                    .description("Chamadas do Core ao ia-service")
                    .tag("operacao", operacao)
                    .tag("resultado", resultado)
                    .register(registry));
        }
    }
}
