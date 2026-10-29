package com.example.educacaocontinuadagamificada.gamificacao.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConclusaoCursoJpaRepository extends JpaRepository<ConclusaoCursoEntity, Long> {

    boolean existsByAlunoIdAndCursoId(Long alunoId, String cursoId);
}
