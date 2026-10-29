import json

import pytest

from worker import ProcessadorNotificacao


def evento(evento_id="e-1"):
    return json.dumps({"eventoId": evento_id, "cursoId": "JAVA-101", "moedasGanhas": 15,
                       "badgesNovas": ["PRIMEIRA_CONCLUSAO"]}).encode()


def test_envia_notificacao_e_deduplica():
    enviadas = []
    p = ProcessadorNotificacao(enviar=enviadas.append)
    assert p.processar(evento()) == "ENVIADO"
    assert p.processar(evento()) == "DUPLICADO"
    assert len(enviadas) == 1
    assert "15 moedas" in enviadas[0]


def test_mensagem_invalida_lanca_erro_para_ir_a_dlq():
    with pytest.raises(Exception):
        ProcessadorNotificacao(enviar=lambda t: None).processar(b"nao-e-json")
