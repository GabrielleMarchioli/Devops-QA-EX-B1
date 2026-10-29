package com.example.educacaocontinuadagamificada.recomendacao;

import com.example.educacaocontinuadagamificada.recomendacao.adapter.out.ia.IaHttpAdapter;
import com.example.educacaocontinuadagamificada.recomendacao.application.port.IaIndisponivelException;
import com.example.educacaocontinuadagamificada.recomendacao.domain.Recomendacao;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Contrato HTTP Core -> ia-service e traducao de falha em IaIndisponivelException. */
public class IaHttpAdapterTest {

    private MockRestServiceServer servidor;
    private IaHttpAdapter adapter;
    private SimpleMeterRegistry registry;

    @BeforeEach
    public void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ia-service");
        servidor = MockRestServiceServer.bindTo(builder).build();
        registry = new SimpleMeterRegistry();
        adapter = new IaHttpAdapter(builder.build(), registry);
    }

    @Test
    public void converteRespostaDoIaService() {
        servidor.expect(requestTo("http://ia-service/recomendacoes/1?limite=2"))
                .andRespond(withSuccess("""
                        {"alunoId":1,"modelo":"popularidade-v1","itens":[
                          {"cursoId":"PY-201","titulo":"Python para dados","score":0.9,"motivo":"afinidade"}]}
                        """, MediaType.APPLICATION_JSON));

        List<Recomendacao> itens = adapter.recomendar(1L, 2);

        assertEquals("PY-201", itens.get(0).cursoId());
        assertEquals(1, registry.get("ia.requisicoes").tag("resultado", "sucesso").timer().count());
    }

    @Test
    public void erro5xxViraIaIndisponivelEMetricaDeFalha() {
        servidor.expect(requestTo("http://ia-service/recomendacoes/1?limite=2")).andRespond(withServerError());

        assertThrows(IaIndisponivelException.class, () -> adapter.recomendar(1L, 2));
        assertEquals(1, registry.get("ia.requisicoes").tag("resultado", "falha").timer().count());
    }

    @Test
    public void servicoDesligadoViraIaIndisponivel() {
        IaHttpAdapter semServidor = new IaHttpAdapter(
                RestClient.builder().baseUrl("http://localhost:1").build(), new SimpleMeterRegistry());

        assertThrows(IaIndisponivelException.class, () -> semServidor.recomendar(1L, 2));
    }
}
