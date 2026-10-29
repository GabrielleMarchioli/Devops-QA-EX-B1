package com.example.educacaocontinuadagamificada.gamificacao.domain;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Entidade de dominio do aluno gamificado.
 * Evolucao da classe Aluno da AC1: a regra original (media > 7 libera 3 cursos)
 * foi mantida sem alteracao em avaliarConclusaoCurso e e reaproveitada por concluirCurso.
 * Nao depende de Spring, JPA ou banco.
 */
public class Aluno {

    private static final double MEDIA_MINIMA_BONUS = 7.0;
    private static final int CURSOS_BONUS = 3;
    static final int MOEDAS_POR_CONCLUSAO = 10;
    static final int MOEDAS_BONUS_MEDIA_ALTA = 5;

    private final Long id;
    private final String nome;
    private int cursosLiberados;
    private int moedas;
    private int cursosConcluidos;
    private final Set<Badge> badges;

    public Aluno(String nome) {
        this(null, nome, 0, 0, 0, Set.of());
    }

    private Aluno(Long id, String nome, int cursosLiberados, int moedas, int cursosConcluidos, Set<Badge> badges) {
        this.id = id;
        this.nome = nome;
        this.cursosLiberados = cursosLiberados;
        this.moedas = moedas;
        this.cursosConcluidos = cursosConcluidos;
        this.badges = badges.isEmpty() ? EnumSet.noneOf(Badge.class) : EnumSet.copyOf(badges);
    }

    /** Recria o aluno a partir de um estado persistido (usado pelos adapters de saida). */
    public static Aluno reconstituir(Long id, String nome, int cursosLiberados, int moedas,
                                     int cursosConcluidos, Set<Badge> badges) {
        return new Aluno(id, nome, cursosLiberados, moedas, cursosConcluidos, badges);
    }

    /** Regra da AC1 (US01), preservada. */
    public void avaliarConclusaoCurso(double media) {
        if (media > MEDIA_MINIMA_BONUS) {
            cursosLiberados += CURSOS_BONUS;
        }
    }

    /** Regra do B1: conclusao gera moedas, libera cursos (regra AC1) e avalia badges. */
    public ResultadoConclusao concluirCurso(String cursoId, double media) {
        if (cursoId == null || cursoId.isBlank()) {
            throw new RegraDeNegocioException("cursoId é obrigatório");
        }
        if (media < 0.0 || media > 10.0) {
            throw new RegraDeNegocioException("média deve estar entre 0 e 10");
        }

        int liberadosAntes = cursosLiberados;
        avaliarConclusaoCurso(media);

        int moedasGanhas = MOEDAS_POR_CONCLUSAO + (media > MEDIA_MINIMA_BONUS ? MOEDAS_BONUS_MEDIA_ALTA : 0);
        moedas += moedasGanhas;
        cursosConcluidos++;

        Set<Badge> novas = EnumSet.noneOf(Badge.class);
        for (Badge badge : Badge.values()) {
            if (!badges.contains(badge) && badge.conquistada(cursosConcluidos, media)) {
                novas.add(badge);
            }
        }
        badges.addAll(novas);

        return new ResultadoConclusao(cursoId, media, moedasGanhas, cursosLiberados - liberadosAntes,
                Collections.unmodifiableSet(novas));
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public int getCursosLiberados() { return cursosLiberados; }
    public int getMoedas() { return moedas; }
    public int getCursosConcluidos() { return cursosConcluidos; }
    public Set<Badge> getBadges() { return Collections.unmodifiableSet(badges); }
}
