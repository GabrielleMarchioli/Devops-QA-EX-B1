package com.example.educacaocontinuadagamificada.gamificacao.adapter.out.memoria;

import com.example.educacaocontinuadagamificada.gamificacao.application.port.out.ConclusaoCursoRepository;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConclusaoCursoEmMemoria implements ConclusaoCursoRepository {

    private final Set<String> chaves = ConcurrentHashMap.newKeySet();

    @Override
    public boolean existe(Long alunoId, String cursoId) {
        return chaves.contains(alunoId + "|" + cursoId);
    }

    @Override
    public void registrar(Long alunoId, String cursoId, double media, Instant ocorridoEm) {
        chaves.add(alunoId + "|" + cursoId);
    }
}
