import json

from gateway import Gateway
from transformacao import Deduplicador, EventoInvalido, transformar

import pytest

TOPICO = "facens/labs/lab3/eventos"
CONC = {"id": "e-1", "dev": "esp32-lab3", "t": "conc", "aluno": 1, "curso": "IOT-101", "nota": 9, "ts": 1761742800}


def test_transforma_payload_compacto_no_contrato_do_core():
    e = transformar(TOPICO, CONC)
    assert e == {"eventoId": "e-1", "dispositivoId": "esp32-lab3", "laboratorio": "LAB3",
                 "tipo": "CONCLUSAO_ATIVIDADE", "alunoId": 1, "cursoId": "IOT-101", "media": 9.0,
                 "ocorridoEm": "2025-10-29T13:00:00Z"}


@pytest.mark.parametrize("payload", [
    {"dev": "x", "t": "pres", "aluno": 1, "ts": 1},
    {"id": "1", "dev": "x", "t": "voar", "aluno": 1, "ts": 1},
    {"id": "1", "dev": "x", "t": "conc", "aluno": 1, "ts": 1},
])
def test_rejeita_payload_invalido(payload):
    with pytest.raises(EventoInvalido):
        transformar(TOPICO, payload)


def test_rejeita_topico_fora_do_padrao():
    with pytest.raises(EventoInvalido):
        transformar("outro/topico", CONC)


def test_deduplicador_respeita_capacidade():
    d = Deduplicador(capacidade=2)
    for i in range(3):
        d.marcar(str(i))
    assert not d.ja_visto("0") and d.ja_visto("2")


def test_gateway_encaminha_uma_vez_e_ignora_duplicado():
    enviados = []
    g = Gateway(enviar=lambda e: enviados.append(e) or 202)
    corpo = json.dumps(CONC).encode()
    assert g.tratar(TOPICO, corpo) == "ENCAMINHADO"
    assert g.tratar(TOPICO, corpo) == "DUPLICADO"
    assert g.tratar(TOPICO, b"{lixo") == "INVALIDO"
    assert len(enviados) == 1


def test_core_fora_do_ar_nao_confirma_evento():
    import requests

    def fora(_):
        raise requests.ConnectionError("Connection refused")

    g = Gateway(enviar=fora, tentativas=2)
    assert g.tratar(TOPICO, json.dumps(CONC).encode()) == "CORE_INDISPONIVEL"
    assert not g.dedup.ja_visto("e-1")
