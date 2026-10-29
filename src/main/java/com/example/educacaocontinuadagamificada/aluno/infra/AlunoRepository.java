package com.example.educacaocontinuadagamificada.aluno.infra;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio da tabela legada "alunos" (AC1).
 * Tambem e usado pelo adapter de persistencia do modulo Gamificacao
 * (tabela compartilhada aceita como trade-off no ADR-001).
 */
public interface AlunoRepository extends JpaRepository<AlunoEntity, Long> {

    List<AlunoEntity> findAllByOrderByMoedasDescNomeAsc(Pageable pageable);
}
