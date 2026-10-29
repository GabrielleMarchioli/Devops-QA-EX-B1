package com.example.educacaocontinuadagamificada.eventos.adapter;

import com.example.educacaocontinuadagamificada.eventos.adapter.memoria.OutboxEmMemoria;
import com.example.educacaocontinuadagamificada.eventos.application.MensageriaGateway;
import com.example.educacaocontinuadagamificada.eventos.application.OutboxArmazenamento;
import com.example.educacaocontinuadagamificada.eventos.application.OutboxEventoPublisher;
import com.example.educacaocontinuadagamificada.eventos.application.ProcessadorOutbox;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@Configuration
@EnableScheduling
public class EventosConfig {

    @Bean
    OutboxEventoPublisher outboxEventoPublisher(OutboxArmazenamento outbox) {
        return new OutboxEventoPublisher(outbox);
    }

    @Bean
    ProcessadorOutbox processadorOutbox(OutboxArmazenamento outbox, MensageriaGateway gateway, Clock relogio,
                                        @Value("${outbox.relay.max-tentativas:10}") int maxTentativas) {
        return new ProcessadorOutbox(outbox, gateway, relogio, maxTentativas);
    }

    @Bean
    @Profile("memoria")
    OutboxArmazenamento outboxEmMemoria() {
        return new OutboxEmMemoria();
    }
}
