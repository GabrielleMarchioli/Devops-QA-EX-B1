package com.example.educacaocontinuadagamificada.gamificacao.adapter.out.persistence;

import com.example.educacaocontinuadagamificada.gamificacao.application.port.out.ConclusaoCursoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Profile("!memoria")
public class ConclusaoCursoJpaAdapter implements ConclusaoCursoRepository {

    private final ConclusaoCursoJpaRepository repository;

    public ConclusaoCursoJpaAdapter(ConclusaoCursoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existe(Long alunoId, String cursoId) {
        return repository.existsByAlunoIdAndCursoId(alunoId, cursoId);
    }

    @Override
    public void registrar(Long alunoId, String cursoId, double media, Instant ocorridoEm) {
        repository.save(new ConclusaoCursoEntity(alunoId, cursoId, media, ocorridoEm));
    }
}
