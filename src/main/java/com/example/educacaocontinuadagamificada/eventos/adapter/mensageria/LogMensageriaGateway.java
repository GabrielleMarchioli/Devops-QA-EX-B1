package com.example.educacaocontinuadagamificada.eventos.adapter.mensageria;

import com.example.educacaocontinuadagamificada.eventos.application.MensageriaGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Gateway usado sem broker (desenvolvimento local com H2): apenas registra o evento em log. */
@Component
@Profile("!rabbit")
public class LogMensageriaGateway implements MensageriaGateway {

    private static final Logger log = LoggerFactory.getLogger(LogMensageriaGateway.class);

    @Override
    public void enviar(String tipo, String eventoId, String payload) {
        log.info("[outbox] evento {} ({}) entregue ao log: {}", eventoId, tipo, payload);
    }
}
