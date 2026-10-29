package com.example.educacaocontinuadagamificada.eventos.adapter.memoria;

import com.example.educacaocontinuadagamificada.eventos.application.OutboxArmazenamento;
import com.example.educacaocontinuadagamificada.eventos.application.OutboxEvento;
import com.example.educacaocontinuadagamificada.eventos.application.StatusOutbox;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Adapter em memoria do outbox (testes e perfil "memoria"). */
public class OutboxEmMemoria implements OutboxArmazenamento {

    private final Map<String, OutboxEvento> eventos = new ConcurrentHashMap<>();

    @Override
    public void salvar(OutboxEvento evento) {
        eventos.put(evento.getEventoId(), evento);
    }

    @Override
    public List<OutboxEvento> pendentes(int limite) {
        return eventos.values().stream()
                .filter(e -> e.getStatus() == StatusOutbox.PENDENTE)
                .sorted(Comparator.comparing(OutboxEvento::getCriadoEm))
                .limit(limite)
                .toList();
    }

    @Override
    public void atualizar(OutboxEvento evento) {
        eventos.put(evento.getEventoId(), evento);
    }

    @Override
    public long contarPendentes() {
        return eventos.values().stream().filter(e -> e.getStatus() == StatusOutbox.PENDENTE).count();
    }

    public OutboxEvento buscar(String eventoId) {
        return eventos.get(eventoId);
    }

    public List<OutboxEvento> todos() {
        return List.copyOf(eventos.values());
    }
}
