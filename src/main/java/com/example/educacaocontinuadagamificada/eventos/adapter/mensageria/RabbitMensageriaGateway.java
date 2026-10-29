package com.example.educacaocontinuadagamificada.eventos.adapter.mensageria;

import com.example.educacaocontinuadagamificada.eventos.application.MensageriaGateway;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Profile("rabbit")
public class RabbitMensageriaGateway implements MensageriaGateway {

    private final RabbitTemplate rabbit;

    public RabbitMensageriaGateway(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @Override
    public void enviar(String tipo, String eventoId, String payload) {
        rabbit.send(RabbitTopologia.EXCHANGE, RabbitTopologia.ROUTING_CURSO_CONCLUIDO,
                MessageBuilder.withBody(payload.getBytes(StandardCharsets.UTF_8))
                        .setContentType("application/json")
                        .setMessageId(eventoId)
                        .setType(tipo)
                        .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                        .build());
    }
}
