package com.example.educacaocontinuadagamificada.aluno.infra;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
// B1 / ADR-001: indice para o ranking top-N (ASR de desempenho com 100 mil alunos)
@Table(name = "alunos", indexes = @Index(name = "idx_alunos_moedas", columnList = "moedas"))
public class AlunoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private int cursosLiberados;
    private String plano;
    private int moedas;

    // B1: colunas novas sao anulaveis para nao quebrar registros legados da AC1
    private Integer cursosConcluidos;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "aluno_badges", joinColumns = @JoinColumn(name = "aluno_id"))
    @Column(name = "badge")
    private Set<String> badges = new HashSet<>();

    public AlunoEntity() {
    }

    public AlunoEntity(String nome, int cursosLiberados, String plano, int moedas) {
        this.nome = nome;
        this.cursosLiberados = cursosLiberados;
        this.plano = plano;
        this.moedas = moedas;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public int getCursosLiberados() { return cursosLiberados; }
    public void setCursosLiberados(int cursosLiberados) { this.cursosLiberados = cursosLiberados; }
    public String getPlano() { return plano; }
    public void setPlano(String plano) { this.plano = plano; }
    public int getMoedas() { return moedas; }
    public void setMoedas(int moedas) { this.moedas = moedas; }

    public int getCursosConcluidos() {
        return cursosConcluidos == null ? 0 : cursosConcluidos;
    }

    public void setCursosConcluidos(int cursosConcluidos) {
        this.cursosConcluidos = cursosConcluidos;
    }

    public Set<String> getBadges() { return badges; }

    public void setBadges(Set<String> badges) {
        this.badges = new HashSet<>(badges);
    }
}
