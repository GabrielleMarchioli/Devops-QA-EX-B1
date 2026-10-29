package com.example.educacaocontinuadagamificada.gamificacao.adapter.out.memoria;

import com.example.educacaocontinuadagamificada.gamificacao.application.port.out.AlunoGamificacaoRepository;
import com.example.educacaocontinuadagamificada.gamificacao.domain.Aluno;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Adapter fake de persistencia (Desafio 2): troca o JPA sem mudar dominio nem caso de uso. */
public class AlunoGamificacaoEmMemoria implements AlunoGamificacaoRepository {

    private final Map<Long, Aluno> dados = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();

    @Override
    public Optional<Aluno> buscarPorId(Long id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public Aluno salvar(Aluno aluno) {
        Long id = aluno.getId() != null ? aluno.getId() : sequencia.incrementAndGet();
        Aluno copia = Aluno.reconstituir(id, aluno.getNome(), aluno.getCursosLiberados(), aluno.getMoedas(),
                aluno.getCursosConcluidos(), aluno.getBadges());
        dados.put(id, copia);
        return copia;
    }

    @Override
    public List<Aluno> topPorMoedas(int limite) {
        return dados.values().stream()
                .sorted(Comparator.comparingInt(Aluno::getMoedas).reversed())
                .limit(limite)
                .toList();
    }
}
