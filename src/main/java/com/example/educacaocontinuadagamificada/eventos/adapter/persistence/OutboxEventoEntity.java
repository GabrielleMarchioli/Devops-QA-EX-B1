package com.example.educacaocontinuadagamificada.eventos.adapter.persistence;

import com.example.educacaocontinuadagamificada.eventos.application.StatusOutbox;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "outbox_eventos")
public class OutboxEventoEntity {

    @Id
    private String eventoId;

    private String tipo;

    @Column(length = 4000)
    private String payload;

    @Enumerated(EnumType.STRING)
    private StatusOutbox status;

    private int tentativas;
    private Instant criadoEm;
    private Instant enviadoEm;

    @Column(length = 1000)
    private String ultimoErro;

    protected OutboxEventoEntity() {
    }

    public OutboxEventoEntity(String eventoId, String tipo, String payload, StatusOutbox status, int tentativas,
                              Instant criadoEm, Instant enviadoEm, String ultimoErro) {
        this.eventoId = eventoId;
        this.tipo = tipo;
        this.payload = payload;
        this.status = status;
        this.tentativas = tentativas;
        this.criadoEm = criadoEm;
        this.enviadoEm = enviadoEm;
        this.ultimoErro = ultimoErro;
    }

    public String getEventoId() { return eventoId; }
    public String getTipo() { return tipo; }
    public String getPayload() { return payload; }
    public StatusOutbox getStatus() { return status; }
    public int getTentativas() { return tentativas; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getEnviadoEm() { return enviadoEm; }
    public String getUltimoErro() { return ultimoErro; }
}
