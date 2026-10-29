"""Mini-lab Desafio 4: publica eventos CursoConcluido no RabbitMQ exatamente como o relay do outbox
(RabbitMensageriaGateway) faz: exchange gamificacao.eventos, routing key curso.concluido,
mensagem persistente com message_id = eventoId. Declara a mesma topologia de RabbitTopologia.java.
"""
import json
import sys
import uuid
from datetime import datetime, timezone

import pika

EXCHANGE, DLX = "gamificacao.eventos", "gamificacao.eventos.dlx"
FILAS = ["notificacoes.curso-concluido", "analytics.curso-concluido"]


def topologia(canal):
    canal.exchange_declare(EXCHANGE, "topic", durable=True)
    canal.exchange_declare(DLX, "fanout", durable=True)
    for fila in FILAS:
        canal.queue_declare(fila, durable=True, arguments={"x-dead-letter-exchange": DLX})
        canal.queue_bind(fila, EXCHANGE, "curso.concluido")
    canal.queue_declare("gamificacao.eventos.dlq", durable=True)
    canal.queue_bind("gamificacao.eventos.dlq", DLX)


def evento(aluno_id: int, curso: str, evento_id: str | None = None) -> dict:
    return {"eventoId": evento_id or str(uuid.uuid4()), "tipo": "CursoConcluido", "alunoId": aluno_id,
            "cursoId": curso, "media": 8.0, "moedasGanhas": 15, "cursosLiberadosGanhos": 3,
            "badgesNovas": ["PRIMEIRA_CONCLUSAO"], "ocorridoEm": datetime.now(timezone.utc).isoformat()}


def publicar(canal, corpo: bytes, evento_id: str):
    canal.basic_publish(EXCHANGE, "curso.concluido", corpo, pika.BasicProperties(
        content_type="application/json", message_id=evento_id, type="CursoConcluido", delivery_mode=2))


if __name__ == "__main__":
    modo = sys.argv[1] if len(sys.argv) > 1 else "normal"
    conexao = pika.BlockingConnection(pika.ConnectionParameters("localhost"))
    canal = conexao.channel()
    topologia(canal)
    if modo == "normal":
        for i, curso in enumerate(["JAVA-101", "GIT-101", "SQL-101"], start=1):
            e = evento(i, curso)
            publicar(canal, json.dumps(e).encode(), e["eventoId"])
            print(f"[relay] publicado {e['eventoId']} aluno={i} curso={curso}")
    elif modo == "duplicado":
        e = evento(1, "JAVA-101", sys.argv[2])
        publicar(canal, json.dumps(e).encode(), e["eventoId"])
        print(f"[relay] republicado (at-least-once) {e['eventoId']}")
    elif modo == "malformado":
        publicar(canal, b"{payload-quebrado", "x")
        print("[relay] publicada mensagem malformada")
    conexao.close()
