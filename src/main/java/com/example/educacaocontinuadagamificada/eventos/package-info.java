/**
 * Modulo Eventos: Transactional Outbox + relay para o broker (ADR-004).
 * O evento e gravado na mesma transacao da conclusao do curso; o envio ao
 * RabbitMQ acontece depois, de forma assincrona, com retentativa.
 */
package com.example.educacaocontinuadagamificada.eventos;
