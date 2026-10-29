package com.example.educacaocontinuadagamificada.iot.application;

import java.time.Instant;

public record EventoIot(
        String eventoId,
        String dispositivoId,
        String laboratorio,
        TipoEventoIot tipo,
        Long alunoId,
        String cursoId,
        Double media,
        Instant ocorridoEm
) {
}
