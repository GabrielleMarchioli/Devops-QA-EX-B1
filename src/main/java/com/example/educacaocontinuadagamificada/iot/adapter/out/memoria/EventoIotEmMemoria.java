package com.example.educacaocontinuadagamificada.iot.adapter.out.memoria;

import com.example.educacaocontinuadagamificada.iot.application.EventoIot;
import com.example.educacaocontinuadagamificada.iot.application.port.EventoIotRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventoIotEmMemoria implements EventoIotRepository {

    private final Map<String, EventoIot> eventos = new ConcurrentHashMap<>();

    @Override
    public boolean jaRecebido(String eventoId) {
        return eventos.containsKey(eventoId);
    }

    @Override
    public void registrar(EventoIot evento) {
        eventos.put(evento.eventoId(), evento);
    }

    public int total() {
        return eventos.size();
    }
}
