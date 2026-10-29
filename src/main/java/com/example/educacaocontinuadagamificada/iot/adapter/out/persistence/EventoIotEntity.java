package com.example.educacaocontinuadagamificada.iot.adapter.out.persistence;

import com.example.educacaocontinuadagamificada.iot.application.TipoEventoIot;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "eventos_iot")
public class EventoIotEntity {

    /** PK = eventoId: o banco tambem barra duplicidade em caso de corrida. */
    @Id
    private String eventoId;

    private String dispositivoId;
    private String laboratorio;

    @Enumerated(EnumType.STRING)
    private TipoEventoIot tipo;

    private Long alunoId;
    private String cursoId;
    private Double media;
    private Instant ocorridoEm;

    protected EventoIotEntity() {
    }

    public EventoIotEntity(String eventoId, String dispositivoId, String laboratorio, TipoEventoIot tipo,
                           Long alunoId, String cursoId, Double media, Instant ocorridoEm) {
        this.eventoId = eventoId;
        this.dispositivoId = dispositivoId;
        this.laboratorio = laboratorio;
        this.tipo = tipo;
        this.alunoId = alunoId;
        this.cursoId = cursoId;
        this.media = media;
        this.ocorridoEm = ocorridoEm;
    }

    public String getEventoId() { return eventoId; }
    public TipoEventoIot getTipo() { return tipo; }
}
