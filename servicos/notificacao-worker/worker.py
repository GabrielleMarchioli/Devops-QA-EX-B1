"""Consumidor assincrono de CursoConcluido (ADR-004).

Le a fila notificacoes.curso-concluido do RabbitMQ, deduplica por eventoId (entrega at-least-once)
e "envia" a notificacao. Falha de envio: nack sem requeue -> mensagem vai para a DLQ.
Se este worker estiver desligado, as mensagens ficam retidas na fila duravel e nada se perde;
a conclusao do curso no Core nao e afetada.
"""
import json
import logging
import os
import time

logging.basicConfig(level=logging.INFO, format="%(asctime)s [notificacao] %(message)s")
log = logging.getLogger(__name__)

FILA = "notificacoes.curso-concluido"


class ProcessadorNotificacao:
    def __init__(self, enviar=None, limite_memoria: int = 10_000):
        self.enviar = enviar or (lambda texto: log.info("ENVIADA: %s", texto))
        self.processados: list[str] = []
        self.limite_memoria = limite_memoria

    def processar(self, corpo: bytes) -> str:
        evento = json.loads(corpo)
        evento_id = evento["eventoId"]
        if evento_id in self.processados:
            return "DUPLICADO"
        badges = ", ".join(evento.get("badgesNovas", [])) or "nenhuma"
        self.enviar(
            f"Parabéns! Você concluiu {evento['cursoId']} e ganhou {evento['moedasGanhas']} moedas "
            f"(badges novas: {badges})."
        )
        self.processados.append(evento_id)
        if len(self.processados) > self.limite_memoria:
            self.processados.pop(0)
        return "ENVIADO"


def main():
    import pika

    processador = ProcessadorNotificacao()
    params = pika.ConnectionParameters(host=os.getenv("RABBITMQ_HOST", "localhost"), heartbeat=30)
    while True:
        try:
            conexao = pika.BlockingConnection(params)
            canal = conexao.channel()
            canal.basic_qos(prefetch_count=10)

            def ao_receber(ch, metodo, _props, corpo):
                try:
                    resultado = processador.processar(corpo)
                    log.info("evento processado: %s", resultado)
                    ch.basic_ack(metodo.delivery_tag)
                except Exception as erro:  # mensagem invalida ou falha de envio
                    log.error("falha ao notificar (%s): enviando para DLQ", erro)
                    ch.basic_nack(metodo.delivery_tag, requeue=False)

            canal.basic_consume(queue=FILA, on_message_callback=ao_receber)
            log.info("aguardando eventos em %s", FILA)
            canal.start_consuming()
        except (pika.exceptions.AMQPError, OSError) as erro:
            # AMQPError: conexao/canal perdidos; OSError: rede/DNS (ex.: container do broker parado)
            log.warning("RabbitMQ indisponível (%s), nova tentativa em 5s", erro)
            time.sleep(5)


if __name__ == "__main__":
    main()