package com.example.educacaocontinuadagamificada.eventos.application;

import com.example.educacaocontinuadagamificada.gamificacao.application.port.out.EventoPublisher;
import com.example.educacaocontinuadagamificada.gamificacao.domain.Badge;
import com.example.educacaocontinuadagamificada.gamificacao.domain.CursoConcluidoEvento;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Implementa a porta EventoPublisher da Gamificacao gravando no outbox.
 * Nao conversa com o broker: por isso a conclusao do curso nunca depende dele.
 */
public class OutboxEventoPublisher implements EventoPublisher {

    private final OutboxArmazenamento outbox;

    public OutboxEventoPublisher(OutboxArmazenamento outbox) {
        this.outbox = outbox;
    }

    @Override
    public void publicar(CursoConcluidoEvento e) {
        outbox.salvar(new OutboxEvento(e.eventoId(), CursoConcluidoEvento.TIPO, paraJson(e), e.ocorridoEm()));
    }

    static String paraJson(CursoConcluidoEvento e) {
        String badges = e.badgesNovas().stream()
                .map(Badge::name)
                .sorted()
                .map(b -> "\"" + b + "\"")
                .collect(Collectors.joining(","));
        return String.format(Locale.ROOT,
                "{\"eventoId\":\"%s\",\"tipo\":\"%s\",\"alunoId\":%d,\"cursoId\":\"%s\",\"media\":%.2f,"
                        + "\"moedasGanhas\":%d,\"cursosLiberadosGanhos\":%d,\"badgesNovas\":[%s],\"ocorridoEm\":\"%s\"}",
                e.eventoId(), CursoConcluidoEvento.TIPO, e.alunoId(), escapar(e.cursoId()), e.media(),
                e.moedasGanhas(), e.cursosLiberadosGanhos(), badges, e.ocorridoEm());
    }

    private static String escapar(String valor) {
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
