package com.example.educacaocontinuadagamificada.eventos.application;

import java.time.Clock;
import java.time.Instant;

/**
 * Relay do outbox: le eventos PENDENTE e tenta entrega-los ao broker.
 * Broker ou consumidor fora do ar: o evento continua PENDENTE e e reenviado no proximo ciclo
 * (entrega at-least-once; consumidores deduplicam por eventoId).
 */
public class ProcessadorOutbox {

    public record Resultado(int enviados, int falhas) {
    }

    private final OutboxArmazenamento outbox;
    private final MensageriaGateway gateway;
    private final Clock relogio;
    private final int maxTentativas;

    public ProcessadorOutbox(OutboxArmazenamento outbox, MensageriaGateway gateway, Clock relogio, int maxTentativas) {
        this.outbox = outbox;
        this.gateway = gateway;
        this.relogio = relogio;
        this.maxTentativas = maxTentativas;
    }

    public Resultado processar(int lote) {
        int enviados = 0;
        int falhas = 0;
        for (OutboxEvento evento : outbox.pendentes(lote)) {
            try {
                gateway.enviar(evento.getTipo(), evento.getEventoId(), evento.getPayload());
                evento.marcarEnviado(Instant.now(relogio));
                enviados++;
            } catch (RuntimeException e) {
                evento.registrarFalha(e.getMessage(), maxTentativas);
                falhas++;
            }
            outbox.atualizar(evento);
        }
        return new Resultado(enviados, falhas);
    }
}
