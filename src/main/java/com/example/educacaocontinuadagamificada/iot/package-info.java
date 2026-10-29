/**
 * Modulo IoT: porta de entrada dos eventos de laboratorio ja normalizados pelo iot-gateway (ADR-005).
 * O Core nao fala MQTT: recebe HTTP do gateway e garante idempotencia por eventoId.
 */
package com.example.educacaocontinuadagamificada.iot;
