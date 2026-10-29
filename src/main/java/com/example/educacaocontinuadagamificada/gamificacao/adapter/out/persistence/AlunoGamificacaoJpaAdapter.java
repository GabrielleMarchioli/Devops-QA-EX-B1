package com.example.educacaocontinuadagamificada.gamificacao.adapter.out.persistence;

import com.example.educacaocontinuadagamificada.aluno.infra.AlunoEntity;
import com.example.educacaocontinuadagamificada.aluno.infra.AlunoRepository;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.out.AlunoGamificacaoRepository;
import com.example.educacaocontinuadagamificada.gamificacao.domain.Aluno;
import com.example.educacaocontinuadagamificada.gamificacao.domain.Badge;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adapter JPA: traduz a tabela legada "alunos" (AC1) para o dominio.
 * Unico ponto do modulo Gamificacao que conhece o modulo Aluno (trade-off do ADR-001).
 */
@Component
@Profile("!memoria")
public class AlunoGamificacaoJpaAdapter implements AlunoGamificacaoRepository {

    private final AlunoRepository repository;

    public AlunoGamificacaoJpaAdapter(AlunoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Aluno> buscarPorId(Long id) {
        return repository.findById(id).map(AlunoGamificacaoJpaAdapter::paraDominio);
    }

    @Override
    public Aluno salvar(Aluno aluno) {
        AlunoEntity entity = repository.findById(aluno.getId())
                .orElseThrow(() -> new IllegalStateException("Aluno " + aluno.getId() + " não existe"));
        entity.setCursosLiberados(aluno.getCursosLiberados());
        entity.setMoedas(aluno.getMoedas());
        entity.setCursosConcluidos(aluno.getCursosConcluidos());
        entity.setBadges(aluno.getBadges().stream().map(Badge::name).collect(Collectors.toSet()));
        return paraDominio(repository.save(entity));
    }

    @Override
    public List<Aluno> topPorMoedas(int limite) {
        return repository.findAllByOrderByMoedasDescNomeAsc(PageRequest.of(0, limite))
                .stream()
                .map(AlunoGamificacaoJpaAdapter::paraDominio)
                .toList();
    }

    private static Aluno paraDominio(AlunoEntity e) {
        Set<Badge> badges = EnumSet.noneOf(Badge.class);
        e.getBadges().forEach(b -> badges.add(Badge.valueOf(b)));
        return Aluno.reconstituir(e.getId(), e.getNome(), e.getCursosLiberados(), e.getMoedas(),
                e.getCursosConcluidos(), badges);
    }
}
