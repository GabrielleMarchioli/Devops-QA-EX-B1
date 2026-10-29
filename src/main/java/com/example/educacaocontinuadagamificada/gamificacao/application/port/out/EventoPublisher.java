package com.example.educacaocontinuadagamificada.gamificacao.application.port.out;

import com.example.educacaocontinuadagamificada.gamificacao.domain.CursoConcluidoEvento;

/** Porta de saida: publicacao de eventos de dominio (implementada via outbox, ADR-004). */
public interface EventoPublisher {

    void publicar(CursoConcluidoEvento evento);
}
