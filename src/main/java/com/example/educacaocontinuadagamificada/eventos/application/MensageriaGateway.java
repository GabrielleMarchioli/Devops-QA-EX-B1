package com.example.educacaocontinuadagamificada.eventos.application;

/** Porta de saida: entrega do evento ao broker. Lanca RuntimeException se o broker estiver fora. */
public interface MensageriaGateway {

    void enviar(String tipo, String eventoId, String payload);
}
