package com.example.educacaocontinuadagamificada.eventos.application;

import java.util.List;

/** Porta de saida: tabela outbox (JPA em producao, memoria em testes). */
public interface OutboxArmazenamento {

    void salvar(OutboxEvento evento);

    List<OutboxEvento> pendentes(int limite);

    void atualizar(OutboxEvento evento);

    long contarPendentes();
}
