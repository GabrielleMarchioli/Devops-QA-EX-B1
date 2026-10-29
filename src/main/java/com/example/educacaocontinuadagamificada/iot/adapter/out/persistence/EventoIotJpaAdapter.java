package com.example.educacaocontinuadagamificada.iot.adapter.out.persistence;

import com.example.educacaocontinuadagamificada.iot.application.EventoIot;
import com.example.educacaocontinuadagamificada.iot.application.port.EventoIotRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!memoria")
public class EventoIotJpaAdapter implements EventoIotRepository {

    private final EventoIotJpaRepository repository;

    public EventoIotJpaAdapter(EventoIotJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean jaRecebido(String eventoId) {
        return repository.existsById(eventoId);
    }

    @Override
    public void registrar(EventoIot e) {
        repository.save(new EventoIotEntity(e.eventoId(), e.dispositivoId(), e.laboratorio(), e.tipo(),
                e.alunoId(), e.cursoId(), e.media(), e.ocorridoEm()));
    }
}
