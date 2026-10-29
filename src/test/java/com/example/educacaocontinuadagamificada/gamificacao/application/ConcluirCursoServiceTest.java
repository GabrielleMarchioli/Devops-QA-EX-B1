package com.example.educacaocontinuadagamificada.gamificacao.application;

import com.example.educacaocontinuadagamificada.gamificacao.adapter.out.memoria.AlunoGamificacaoEmMemoria;
import com.example.educacaocontinuadagamificada.gamificacao.adapter.out.memoria.ConclusaoCursoEmMemoria;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.AlunoNaoEncontradoException;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConcluirCursoComando;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConclusaoCursoResultado;
import com.example.educacaocontinuadagamificada.gamificacao.domain.Aluno;
import com.example.educacaocontinuadagamificada.gamificacao.domain.CursoConcluidoEvento;
import com.example.educacaocontinuadagamificada.gamificacao.domain.PosicaoRanking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Desafio 2 (evidencia): o mesmo caso de uso roda com adapters em memoria,
 * sem Spring e sem JPA. Em producao os adapters sao AlunoGamificacaoJpaAdapter e ConclusaoCursoJpaAdapter.
 */
public class ConcluirCursoServiceTest {

    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-10-29T12:00:00Z"), ZoneOffset.UTC);

    private AlunoGamificacaoEmMemoria alunos;
    private List<CursoConcluidoEvento> publicados;
    private ConcluirCursoService service;
    private Long gabrielleId;

    @BeforeEach
    public void preparar() {
        alunos = new AlunoGamificacaoEmMemoria();
        publicados = new ArrayList<>();
        service = new ConcluirCursoService(alunos, new ConclusaoCursoEmMemoria(), publicados::add, RELOGIO);
        gabrielleId = alunos.salvar(new Aluno("Gabrielle")).getId();
    }

    @Test
    public void concluiCursoPersisteRecompensaEPublicaEvento() {
        ConclusaoCursoResultado r = service.executar(new ConcluirCursoComando(gabrielleId, "JAVA-101", 8.0));

        assertFalse(r.jaRegistrada());
        assertEquals(15, r.moedasGanhas());
        assertEquals(3, r.cursosLiberadosTotal());
        assertEquals(Set.of("PRIMEIRA_CONCLUSAO"), r.badgesNovas());
        assertEquals(15, alunos.buscarPorId(gabrielleId).orElseThrow().getMoedas());

        assertEquals(1, publicados.size());
        CursoConcluidoEvento evento = publicados.get(0);
        assertEquals("JAVA-101", evento.cursoId());
        assertEquals(Instant.parse("2026-10-29T12:00:00Z"), evento.ocorridoEm());
    }

    @Test
    public void reenvioDaMesmaConclusaoEIdempotente() {
        service.executar(new ConcluirCursoComando(gabrielleId, "JAVA-101", 8.0));

        ConclusaoCursoResultado repetida = service.executar(new ConcluirCursoComando(gabrielleId, "JAVA-101", 8.0));

        assertTrue(repetida.jaRegistrada());
        assertEquals(0, repetida.moedasGanhas());
        assertEquals(15, repetida.moedasTotal());
        assertEquals(1, publicados.size());
    }

    @Test
    public void alunoInexistenteGeraExcecaoDeAplicacao() {
        assertThrows(AlunoNaoEncontradoException.class,
                () -> service.executar(new ConcluirCursoComando(999L, "JAVA-101", 8.0)));
    }

    @Test
    public void rankingRefleteAsConclusoes() {
        Long afonsoId = alunos.salvar(new Aluno("Afonso")).getId();
        service.executar(new ConcluirCursoComando(afonsoId, "JAVA-101", 9.0));
        service.executar(new ConcluirCursoComando(afonsoId, "GIT-101", 9.0));
        service.executar(new ConcluirCursoComando(gabrielleId, "JAVA-101", 6.0));

        List<PosicaoRanking> ranking = new ConsultarRankingService(alunos).topN(10);

        assertEquals("Afonso", ranking.get(0).nome());
        assertEquals(30, ranking.get(0).moedas());
        assertEquals(2, ranking.get(1).posicao());
    }

    @Test
    public void rankingLimitaQuantidadeEntreUmECem() {
        assertEquals(1, new ConsultarRankingService(alunos).topN(0).size());
        assertEquals(1, new ConsultarRankingService(alunos).topN(1000).size());
    }
}
