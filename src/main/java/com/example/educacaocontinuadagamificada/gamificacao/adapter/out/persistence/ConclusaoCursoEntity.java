package com.example.educacaocontinuadagamificada.gamificacao.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "conclusoes_curso",
        uniqueConstraints = @UniqueConstraint(name = "uk_conclusao_aluno_curso", columnNames = {"aluno_id", "curso_id"}))
public class ConclusaoCursoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Column(name = "curso_id", nullable = false)
    private String cursoId;

    private double media;
    private Instant ocorridoEm;

    protected ConclusaoCursoEntity() {
    }

    public ConclusaoCursoEntity(Long alunoId, String cursoId, double media, Instant ocorridoEm) {
        this.alunoId = alunoId;
        this.cursoId = cursoId;
        this.media = media;
        this.ocorridoEm = ocorridoEm;
    }

    public Long getId() { return id; }
    public Long getAlunoId() { return alunoId; }
    public String getCursoId() { return cursoId; }
    public double getMedia() { return media; }
    public Instant getOcorridoEm() { return ocorridoEm; }
}
