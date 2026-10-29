package com.example.educacaocontinuadagamificada.eventos.adapter.persistence;

import com.example.educacaocontinuadagamificada.eventos.application.StatusOutbox;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxJpaRepository extends JpaRepository<OutboxEventoEntity, String> {

    List<OutboxEventoEntity> findByStatusOrderByCriadoEmAsc(StatusOutbox status, Pageable pageable);

    long countByStatus(StatusOutbox status);
}
