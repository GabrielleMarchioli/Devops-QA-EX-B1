package com.example.educacaocontinuadagamificada.eventos.adapter.mensageria;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Topologia RabbitMQ (ADR-004): um exchange topic, uma fila duravel por consumidor
 * (notificacao e analytics) e uma DLQ para mensagens rejeitadas.
 */
@Configuration
@Profile("rabbit")
public class RabbitTopologia {

    public static final String EXCHANGE = "gamificacao.eventos";
    public static final String DLX = "gamificacao.eventos.dlx";
    public static final String FILA_NOTIFICACOES = "notificacoes.curso-concluido";
    public static final String FILA_ANALYTICS = "analytics.curso-concluido";
    public static final String FILA_DLQ = "gamificacao.eventos.dlq";
    public static final String ROUTING_CURSO_CONCLUIDO = "curso.concluido";

    @Bean
    Declarables topologiaGamificacao() {
        TopicExchange exchange = new TopicExchange(EXCHANGE, true, false);
        FanoutExchange dlx = new FanoutExchange(DLX, true, false);
        Queue notificacoes = QueueBuilder.durable(FILA_NOTIFICACOES).deadLetterExchange(DLX).build();
        Queue analytics = QueueBuilder.durable(FILA_ANALYTICS).deadLetterExchange(DLX).build();
        Queue dlq = QueueBuilder.durable(FILA_DLQ).build();
        Binding b1 = BindingBuilder.bind(notificacoes).to(exchange).with(ROUTING_CURSO_CONCLUIDO);
        Binding b2 = BindingBuilder.bind(analytics).to(exchange).with(ROUTING_CURSO_CONCLUIDO);
        Binding b3 = BindingBuilder.bind(dlq).to(dlx);
        return new Declarables(exchange, dlx, notificacoes, analytics, dlq, b1, b2, b3);
    }
}
