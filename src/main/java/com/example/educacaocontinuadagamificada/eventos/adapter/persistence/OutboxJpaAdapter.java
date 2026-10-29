package com.example.educacaocontinuadagamificada.eventos.adapter.persistence;

import com.example.educacaocontinuadagamificada.eventos.application.OutboxArmazenamento;
import com.example.educacaocontinuadagamificada.eventos.application.OutboxEvento;
import com.example.educacaocontinuadagamificada.eventos.application.StatusOutbox;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!memoria")
public class OutboxJpaAdapter implements OutboxArmazenamento {

    private final OutboxJpaRepository repository;

    public OutboxJpaAdapter(OutboxJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(OutboxEvento e) {
        repository.save(paraEntity(e));
    }

    @Override
    public List<OutboxEvento> pendentes(int limite) {
        return repository.findByStatusOrderByCriadoEmAsc(StatusOutbox.PENDENTE, PageRequest.of(0, limite))
                .stream()
                .map(x -> new OutboxEvento(x.getEventoId(), x.getTipo(), x.getPayload(), x.getCriadoEm(),
                        x.getStatus(), x.getTentativas(), x.getEnviadoEm(), x.getUltimoErro()))
                .toList();
    }

    @Override
    public void atualizar(OutboxEvento e) {
        repository.save(paraEntity(e));
    }

    @Override
    public long contarPendentes() {
        return repository.countByStatus(StatusOutbox.PENDENTE);
    }

    private static OutboxEventoEntity paraEntity(OutboxEvento e) {
        String erro = e.getUltimoErro();
        if (erro != null && erro.length() > 1000) {
            erro = erro.substring(0, 1000);
        }
        return new OutboxEventoEntity(e.getEventoId(), e.getTipo(), e.getPayload(), e.getStatus(),
                e.getTentativas(), e.getCriadoEm(), e.getEnviadoEm(), erro);
    }
}
