package com.example.educacaocontinuadagamificada.aluno.api;

public class AlunoDTO {

    private Long id;
    private String nome;
    private int cursosLiberados;
    private String plano;
    private int moedas;

    public AlunoDTO() {
    }

    public AlunoDTO(Long id, String nome, int cursosLiberados, String plano, int moedas) {
        this.id = id;
        this.nome = nome;
        this.cursosLiberados = cursosLiberados;
        this.plano = plano;
        this.moedas = moedas;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public int getCursosLiberados() { return cursosLiberados; }
    public void setCursosLiberados(int cursosLiberados) { this.cursosLiberados = cursosLiberados; }
    public String getPlano() { return plano; }
    public void setPlano(String plano) { this.plano = plano; }
    public int getMoedas() { return moedas; }
    public void setMoedas(int moedas) { this.moedas = moedas; }
}
