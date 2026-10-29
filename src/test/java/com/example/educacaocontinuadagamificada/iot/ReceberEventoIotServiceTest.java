package com.example.educacaocontinuadagamificada.iot;

import com.example.educacaocontinuadagamificada.gamificacao.adapter.out.memoria.AlunoGamificacaoEmMemoria;
import com.example.educacaocontinuadagamificada.gamificacao.adapter.out.memoria.ConclusaoCursoEmMemoria;
import com.example.educacaocontinuadagamificada.gamificacao.application.ConcluirCursoService;
import com.example.educacaocontinuadagamificada.gamificacao.domain.Aluno;
import com.example.educacaocontinuadagamificada.iot.adapter.out.memoria.EventoIotEmMemoria;
import com.example.educacaocontinuadagamificada.iot.application.EventoIot;
import com.example.educacaocontinuadagamificada.iot.application.EventoIotInvalidoException;
import com.example.educacaocontinuadagamificada.iot.application.ReceberEventoIotService;
import com.example.educacaocontinuadagamificada.iot.application.ResultadoRecebimento;
import com.example.educacaocontinuadagamificada.iot.application.TipoEventoIot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Desafio 5 (evidencia): eventos IoT duplicados (MQTT QoS 1) nao duplicam recompensa. */
public class ReceberEventoIotServiceTest {

    private AlunoGamificacaoEmMemoria alunos;
    private EventoIotEmMemoria registros;
    private List<Object> publicados;
    private ReceberEventoIotService service;
    private Long alunoId;

    @BeforeEach
    public void preparar() {
        alunos = new AlunoGamificacaoEmMemoria();
        registros = new EventoIotEmMemoria();
        publicados = new ArrayList<>();
        alunoId = alunos.salvar(new Aluno("Gabrielle")).getId();
        ConcluirCursoService concluir = new ConcluirCursoService(alunos, new ConclusaoCursoEmMemoria(),
                publicados::add, Clock.systemUTC());
        service = new ReceberEventoIotService(registros, concluir);
    }

    private EventoIot conclusao(String eventoId) {
        return new EventoIot(eventoId, "esp32-lab3", "LAB-3", TipoEventoIot.CONCLUSAO_ATIVIDADE,
                alunoId, "IOT-101", 9.0, Instant.now());
    }

    @Test
    public void conclusaoViaIotGeraRecompensaUmaVezMesmoComEventoDuplicado() {
        assertEquals(ResultadoRecebimento.PROCESSADO, service.receber(conclusao("evt-1")));
        assertEquals(ResultadoRecebimento.DUPLICADO, service.receber(conclusao("evt-1")));

        assertEquals(15, alunos.buscarPorId(alunoId).orElseThrow().getMoedas());
        assertEquals(1, publicados.size());
        assertEquals(1, registros.total());
    }

    @Test
    public void presencaERegistradaSemAlterarMoedas() {
        EventoIot presenca = new EventoIot("evt-2", "esp32-lab3", "LAB-3", TipoEventoIot.PRESENCA,
                alunoId, null, null, Instant.now());

        assertEquals(ResultadoRecebimento.PROCESSADO, service.receber(presenca));
        assertEquals(0, alunos.buscarPorId(alunoId).orElseThrow().getMoedas());
        assertEquals(1, registros.total());
    }

    @Test
    public void eventoMalformadoERejeitado() {
        assertThrows(EventoIotInvalidoException.class, () -> service.receber(conclusao(" ")));
        assertThrows(EventoIotInvalidoException.class, () -> service.receber(
                new EventoIot("evt-3", "d", "L", null, alunoId, null, null, Instant.now())));
        assertThrows(EventoIotInvalidoException.class, () -> service.receber(
                new EventoIot("evt-4", "d", "L", TipoEventoIot.CONCLUSAO_ATIVIDADE, alunoId, null, null, Instant.now())));
    }
}
