"""iot-gateway: consumidor MQTT que isola o Core dos dispositivos (ADR-005).

- Assina facens/labs/+/eventos com QoS 1 e sessao persistente (clean_session=False):
  enquanto o gateway estiver fora, o broker guarda as mensagens QoS 1 para ele.
- ACK manual: o PUBACK so e enviado depois que o Core aceitou o evento (202/200).
  Se o Core estiver fora, o evento nao e confirmado e o broker reentrega (at-least-once).
- Duplicidade: Deduplicador local + idempotencia por eventoId no Core.
"""
import json
import logging
import os
import time

import paho.mqtt.client as mqtt
import requests
from prometheus_client import Counter, start_http_server

from transformacao import Deduplicador, EventoInvalido, transformar

logging.basicConfig(level=logging.INFO, format="%(asctime)s [iot-gateway] %(message)s")
log = logging.getLogger(__name__)

BROKER = os.getenv("MQTT_HOST", "localhost")
PORTA = int(os.getenv("MQTT_PORT", "1883"))
TOPICO = "facens/labs/+/eventos"
CORE_URL = os.getenv("CORE_URL", "http://localhost:8080") + "/api/iot/eventos"
METRICS_PORT = int(os.getenv("METRICS_PORT", "9102"))

EVENTOS = Counter("iot_gateway_eventos_total", "Eventos MQTT tratados pelo gateway", ["resultado"])


class Gateway:
    def __init__(self, enviar=None, tentativas: int = 3):
        self.dedup = Deduplicador()
        self.enviar = enviar or self._enviar_core
        self.tentativas = tentativas

    @staticmethod
    def _enviar_core(evento: dict) -> int:
        return requests.post(CORE_URL, json=evento, timeout=3).status_code

    def tratar(self, topico: str, bruto: bytes) -> str:
        """Retorna ENCAMINHADO, DUPLICADO, INVALIDO ou CORE_INDISPONIVEL."""
        try:
            evento = transformar(topico, json.loads(bruto))
        except (EventoInvalido, ValueError, TypeError) as erro:
            log.warning("descartado (inválido): %s", erro)
            return "INVALIDO"
        if self.dedup.ja_visto(evento["eventoId"]):
            log.info("duplicado ignorado %s (retransmissao QoS 1)", evento["eventoId"])
            return "DUPLICADO"
        for tentativa in range(1, self.tentativas + 1):
            try:
                status = self.enviar(evento)
                if status in (200, 202):
                    self.dedup.marcar(evento["eventoId"])
                    log.info("encaminhado %s %s -> Core %s", evento["eventoId"], evento["tipo"], status)
                    return "ENCAMINHADO"
                if 400 <= status < 500:
                    log.warning("Core rejeitou %s (%s)", evento["eventoId"], status)
                    return "INVALIDO"
            except requests.RequestException as erro:
                log.warning("Core indisponível (tentativa %s): %s", tentativa, erro)
            time.sleep(0.2 * tentativa)
        return "CORE_INDISPONIVEL"


def main():
    start_http_server(METRICS_PORT)
    gateway = Gateway()
    cliente = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id="iot-gateway", clean_session=False)
    cliente.manual_ack_set(True)

    def ao_conectar(c, _u, _f, codigo, _p):
        log.info("conectado ao broker (%s), assinando %s", codigo, TOPICO)
        c.subscribe(TOPICO, qos=1)

    def ao_receber(c, _u, msg):
        resultado = gateway.tratar(msg.topic, msg.payload)
        EVENTOS.labels(resultado).inc()
        if resultado != "CORE_INDISPONIVEL":
            c.ack(msg.mid, msg.qos)  # sem ack: broker reentrega depois

    cliente.on_connect = ao_conectar
    cliente.on_message = ao_receber
    cliente.reconnect_delay_set(min_delay=1, max_delay=30)
    cliente.connect(BROKER, PORTA, keepalive=30)
    cliente.loop_forever(retry_first_connection=True)


if __name__ == "__main__":
    main()
