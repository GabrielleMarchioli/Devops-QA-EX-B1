package com.example.educacaocontinuadagamificada.iot.application;

import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConcluirCursoComando;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConcluirCursoUseCase;
import com.example.educacaocontinuadagamificada.iot.application.port.EventoIotRepository;

/**
 * Recebe eventos IoT normalizados. MQTT QoS 1 entrega "pelo menos uma vez",
 * entao duplicidade e esperada e tratada aqui por eventoId (ADR-005).
 * Conclusao de atividade reaproveita o caso de uso da Gamificacao (porta de entrada publica).
 */
public class ReceberEventoIotService implements ReceberEventoIotUseCase {

    private final EventoIotRepository eventos;
    private final ConcluirCursoUseCase concluirCurso;

    public ReceberEventoIotService(EventoIotRepository eventos, ConcluirCursoUseCase concluirCurso) {
        this.eventos = eventos;
        this.concluirCurso = concluirCurso;
    }

    @Override
    public ResultadoRecebimento receber(EventoIot evento) {
        validar(evento);
        if (eventos.jaRecebido(evento.eventoId())) {
            return ResultadoRecebimento.DUPLICADO;
        }
        if (evento.tipo() == TipoEventoIot.CONCLUSAO_ATIVIDADE) {
            concluirCurso.executar(new ConcluirCursoComando(evento.alunoId(), evento.cursoId(), evento.media()));
        }
        eventos.registrar(evento);
        return ResultadoRecebimento.PROCESSADO;
    }

    private static void validar(EventoIot e) {
        if (e.eventoId() == null || e.eventoId().isBlank()) {
            throw new EventoIotInvalidoException("eventoId é obrigatório");
        }
        if (e.tipo() == null || e.alunoId() == null) {
            throw new EventoIotInvalidoException("tipo e alunoId são obrigatórios");
        }
        if (e.tipo() == TipoEventoIot.CONCLUSAO_ATIVIDADE && (e.cursoId() == null || e.media() == null)) {
            throw new EventoIotInvalidoException("conclusão exige cursoId e media");
        }
    }
}
