package com.example.educacaocontinuadagamificada.iot.adapter.in.web;

import com.example.educacaocontinuadagamificada.iot.application.EventoIot;
import com.example.educacaocontinuadagamificada.iot.application.EventoIotInvalidoException;
import com.example.educacaocontinuadagamificada.iot.application.ReceberEventoIotUseCase;
import com.example.educacaocontinuadagamificada.iot.application.ResultadoRecebimento;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Chamado apenas pelo iot-gateway (nunca diretamente pelos dispositivos). */
@RestController
@RequestMapping("/api/iot")
public class EventoIotController {

    private final ReceberEventoIotUseCase service;
    private final MeterRegistry registry;

    public EventoIotController(ReceberEventoIotUseCase service, MeterRegistry registry) {
        this.service = service;
        this.registry = registry;
    }

    @PostMapping("/eventos")
    public ResponseEntity<Map<String, String>> receber(@RequestBody EventoIot evento) {
        ResultadoRecebimento resultado = service.receber(evento);
        registry.counter("iot.eventos.recebidos", "resultado", resultado.name()).increment();
        HttpStatus status = resultado == ResultadoRecebimento.PROCESSADO ? HttpStatus.ACCEPTED : HttpStatus.OK;
        return ResponseEntity.status(status).body(Map.of("eventoId", evento.eventoId(), "resultado", resultado.name()));
    }

    @ExceptionHandler(EventoIotInvalidoException.class)
    public ResponseEntity<Map<String, String>> invalido(EventoIotInvalidoException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
    }
}
