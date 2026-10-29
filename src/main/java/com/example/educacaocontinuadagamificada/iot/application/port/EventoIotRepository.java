package com.example.educacaocontinuadagamificada.iot.application.port;

import com.example.educacaocontinuadagamificada.iot.application.EventoIot;

/** Porta de saida: registro de eventos IoT ja recebidos (chave = eventoId). */
public interface EventoIotRepository {

    boolean jaRecebido(String eventoId);

    void registrar(EventoIot evento);
}
