package com.example.educacaocontinuadagamificada.eventos.application;

import java.time.Instant;

public class OutboxEvento {

    private final String eventoId;
    private final String tipo;
    private final String payload;
    private final Instant criadoEm;
    private StatusOutbox status;
    private int tentativas;
    private Instant enviadoEm;
    private String ultimoErro;

    public OutboxEvento(String eventoId, String tipo, String payload, Instant criadoEm) {
        this(eventoId, tipo, payload, criadoEm, StatusOutbox.PENDENTE, 0, null, null);
    }

    public OutboxEvento(String eventoId, String tipo, String payload, Instant criadoEm, StatusOutbox status,
                        int tentativas, Instant enviadoEm, String ultimoErro) {
        this.eventoId = eventoId;
        this.tipo = tipo;
        this.payload = payload;
        this.criadoEm = criadoEm;
        this.status = status;
        this.tentativas = tentativas;
        this.enviadoEm = enviadoEm;
        this.ultimoErro = ultimoErro;
    }

    void marcarEnviado(Instant quando) {
        status = StatusOutbox.ENVIADO;
        tentativas++;
        enviadoEm = quando;
        ultimoErro = null;
    }

    void registrarFalha(String erro, int maxTentativas) {
        tentativas++;
        ultimoErro = erro;
        if (tentativas >= maxTentativas) {
            status = StatusOutbox.FALHA_DEFINITIVA;
        }
    }

    public String getEventoId() { return eventoId; }
    public String getTipo() { return tipo; }
    public String getPayload() { return payload; }
    public Instant getCriadoEm() { return criadoEm; }
    public StatusOutbox getStatus() { return status; }
    public int getTentativas() { return tentativas; }
    public Instant getEnviadoEm() { return enviadoEm; }
    public String getUltimoErro() { return ultimoErro; }
}
