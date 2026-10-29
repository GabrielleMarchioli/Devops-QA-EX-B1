package com.example.educacaocontinuadagamificada.iot.application;

/** Porta de entrada do modulo IoT. */
public interface ReceberEventoIotUseCase {

    ResultadoRecebimento receber(EventoIot evento);
}
