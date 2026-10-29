package com.example.educacaocontinuadagamificada.gamificacao.application;

import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.AlunoNaoEncontradoException;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConcluirCursoComando;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConcluirCursoUseCase;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConclusaoCursoResultado;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.out.AlunoGamificacaoRepository;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.out.ConclusaoCursoRepository;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.out.EventoPublisher;
import com.example.educacaocontinuadagamificada.gamificacao.domain.Aluno;
import com.example.educacaocontinuadagamificada.gamificacao.domain.Badge;
import com.example.educacaocontinuadagamificada.gamificacao.domain.CursoConcluidoEvento;
import com.example.educacaocontinuadagamificada.gamificacao.domain.ResultadoConclusao;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso sincrono: registrar conclusao + calcular recompensas.
 * Notificacao e analytics NAO sao chamados aqui: apenas um evento e publicado (ADR-004).
 */
public class ConcluirCursoService implements ConcluirCursoUseCase {

    private final AlunoGamificacaoRepository alunos;
    private final ConclusaoCursoRepository conclusoes;
    private final EventoPublisher eventos;
    private final Clock relogio;

    public ConcluirCursoService(AlunoGamificacaoRepository alunos, ConclusaoCursoRepository conclusoes,
                                EventoPublisher eventos, Clock relogio) {
        this.alunos = alunos;
        this.conclusoes = conclusoes;
        this.eventos = eventos;
        this.relogio = relogio;
    }

    @Override
    public ConclusaoCursoResultado executar(ConcluirCursoComando comando) {
        Aluno aluno = alunos.buscarPorId(comando.alunoId())
                .orElseThrow(() -> new AlunoNaoEncontradoException(comando.alunoId()));

        if (conclusoes.existe(comando.alunoId(), comando.cursoId())) {
            // Idempotencia: reenvio (ex.: evento IoT duplicado) nao gera nova recompensa
            return new ConclusaoCursoResultado(aluno.getId(), comando.cursoId(), true, 0, 0, Set.of(),
                    aluno.getMoedas(), aluno.getCursosLiberados());
        }

        ResultadoConclusao resultado = aluno.concluirCurso(comando.cursoId(), comando.media());
        Aluno salvo = alunos.salvar(aluno);

        Instant agora = Instant.now(relogio);
        conclusoes.registrar(salvo.getId(), comando.cursoId(), comando.media(), agora);
        eventos.publicar(new CursoConcluidoEvento(
                UUID.randomUUID().toString(), salvo.getId(), comando.cursoId(), comando.media(),
                resultado.moedasGanhas(), resultado.cursosLiberadosGanhos(), resultado.badgesNovas(), agora));

        return new ConclusaoCursoResultado(salvo.getId(), comando.cursoId(), false,
                resultado.moedasGanhas(), resultado.cursosLiberadosGanhos(), nomes(resultado.badgesNovas()),
                salvo.getMoedas(), salvo.getCursosLiberados());
    }

    private static Set<String> nomes(Set<Badge> badges) {
        return badges.stream().map(Badge::name).collect(Collectors.toUnmodifiableSet());
    }
}
