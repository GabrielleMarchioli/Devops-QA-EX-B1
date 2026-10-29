package com.example.educacaocontinuadagamificada.gamificacao.application.port.in;

public class AlunoNaoEncontradoException extends RuntimeException {

    public AlunoNaoEncontradoException(Long alunoId) {
        super("Aluno " + alunoId + " não encontrado");
    }
}
