package com.example.educacaocontinuadagamificada.eventos;

import com.example.educacaocontinuadagamificada.eventos.adapter.memoria.OutboxEmMemoria;
import com.example.educacaocontinuadagamificada.eventos.application.MensageriaGateway;
import com.example.educacaocontinuadagamificada.eventos.application.OutboxEvento;
import com.example.educacaocontinuadagamificada.eventos.application.OutboxEventoPublisher;
import com.example.educacaocontinuadagamificada.eventos.application.ProcessadorOutbox;
import com.example.educacaocontinuadagamificada.eventos.application.StatusOutbox;
import com.example.educacaocontinuadagamificada.gamificacao.adapter.out.memoria.AlunoGamificacaoEmMemoria;
import com.example.educacaocontinuadagamificada.gamificacao.adapter.out.memoria.ConclusaoCursoEmMemoria;
import com.example.educacaocontinuadagamificada.gamificacao.application.ConcluirCursoService;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConcluirCursoComando;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConclusaoCursoResultado;
import com.example.educacaocontinuadagamificada.gamificacao.domain.Aluno;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Desafio 4 (evidencia): consumidor/broker indisponivel nao impede a conclusao do curso.
 * Fluxo sincrono = conclusao + recompensa; fluxo assincrono = outbox -> broker -> notificacao.
 */
public class ConsumidorIndisponivelTest {

    /** Simula o broker: fora do ar ate ser religado. */
    static class BrokerSimulado implements MensageriaGateway {
        boolean disponivel = false;
        final List<String> entregues = new ArrayList<>();

        @Override
        public void enviar(String tipo, String eventoId, String payload) {
            if (!disponivel) {
                throw new IllegalStateException("broker indisponível: Connection refused");
            }
            entregues.add(eventoId);
        }
    }

    @Test
    public void conclusaoSucedeComBrokerForaEEventoEEntregueQuandoEleVolta() {
        AlunoGamificacaoEmMemoria alunos = new AlunoGamificacaoEmMemoria();
        Long id = alunos.salvar(new Aluno("Gabrielle")).getId();
        OutboxEmMemoria outbox = new OutboxEmMemoria();
        BrokerSimulado broker = new BrokerSimulado();
        ConcluirCursoService concluir = new ConcluirCursoService(alunos, new ConclusaoCursoEmMemoria(),
                new OutboxEventoPublisher(outbox), Clock.systemUTC());
        ProcessadorOutbox relay = new ProcessadorOutbox(outbox, broker, Clock.systemUTC(), 10);

        // 1) sincrono: aluno recebe a recompensa mesmo com o broker fora
        ConclusaoCursoResultado resultado = concluir.executar(new ConcluirCursoComando(id, "JAVA-101", 8.0));
        assertEquals(15, resultado.moedasGanhas());
        assertEquals(1, outbox.contarPendentes());

        // 2) assincrono: relay tenta, falha e mantem o evento PENDENTE (nada se perde)
        ProcessadorOutbox.Resultado tentativa1 = relay.processar(50);
        assertEquals(0, tentativa1.enviados());
        assertEquals(1, tentativa1.falhas());
        OutboxEvento evento = outbox.todos().get(0);
        assertEquals(StatusOutbox.PENDENTE, evento.getStatus());
        assertEquals(1, evento.getTentativas());
        assertTrue(evento.getUltimoErro().contains("Connection refused"));

        // 3) broker volta: proximo ciclo entrega
        broker.disponivel = true;
        ProcessadorOutbox.Resultado tentativa2 = relay.processar(50);
        assertEquals(1, tentativa2.enviados());
        assertEquals(StatusOutbox.ENVIADO, outbox.buscar(evento.getEventoId()).getStatus());
        assertEquals(List.of(evento.getEventoId()), broker.entregues);
        assertEquals(0, outbox.contarPendentes());
    }

    @Test
    public void aposMaximoDeTentativasEventoVaiParaFalhaDefinitiva() {
        OutboxEmMemoria outbox = new OutboxEmMemoria();
        outbox.salvar(new OutboxEvento("e-1", "CursoConcluido", "{}", java.time.Instant.now()));
        ProcessadorOutbox relay = new ProcessadorOutbox(outbox, new BrokerSimulado(), Clock.systemUTC(), 3);

        relay.processar(10);
        relay.processar(10);
        relay.processar(10);

        assertEquals(StatusOutbox.FALHA_DEFINITIVA, outbox.buscar("e-1").getStatus());
        assertEquals(0, outbox.contarPendentes());
    }

    @Test
    public void payloadDoEventoEhJsonComCamposDoContrato() {
        AlunoGamificacaoEmMemoria alunos = new AlunoGamificacaoEmMemoria();
        Long id = alunos.salvar(new Aluno("Gabrielle")).getId();
        OutboxEmMemoria outbox = new OutboxEmMemoria();
        new ConcluirCursoService(alunos, new ConclusaoCursoEmMemoria(), new OutboxEventoPublisher(outbox),
                Clock.systemUTC()).executar(new ConcluirCursoComando(id, "CURSO-\"X\"", 9.5));

        String json = outbox.todos().get(0).getPayload();

        assertTrue(json.contains("\"tipo\":\"CursoConcluido\""));
        assertTrue(json.contains("\"alunoId\":" + id));
        assertTrue(json.contains("\"cursoId\":\"CURSO-\\\"X\\\"\""));
        assertTrue(json.contains("\"media\":9.50"));
        assertTrue(json.contains("\"badgesNovas\":[\"EXCELENCIA\",\"PRIMEIRA_CONCLUSAO\"]"));
    }
}
