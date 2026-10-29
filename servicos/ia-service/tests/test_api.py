from fastapi.testclient import TestClient

from app.main import CAOS, app

client = TestClient(app)


def setup_function():
    CAOS.update(latencia_ms=0, taxa_erro=0.0)


def test_health():
    assert client.get("/health").json()["status"] == "UP"


def test_recomendacoes_respeitam_limite_e_contrato():
    corpo = client.get("/recomendacoes/1?limite=2").json()
    assert corpo["alunoId"] == 1
    assert len(corpo["itens"]) == 2
    assert set(corpo["itens"][0]) == {"cursoId", "titulo", "score", "motivo"}


def test_recomendacao_prioriza_trilha_do_aluno():
    itens = client.get("/recomendacoes/1?limite=3").json()["itens"]  # 1 % 4 -> trilha dados
    assert itens[0]["motivo"] == "trilha dados"


def test_aluno_invalido():
    assert client.get("/recomendacoes/0").status_code == 400


def test_assistente_recupera_trecho_relevante():
    corpo = client.post("/assistente", json={"pergunta": "Como ganho a badge de excelência?"}).json()
    assert "EXCELENCIA" in corpo["resposta"]
    assert "moedas-e-badges" in corpo["fontes"]


def test_assistente_sem_contexto():
    corpo = client.post("/assistente", json={"pergunta": "xyzzy"}).json()
    assert corpo["fontes"] == []


def test_assistente_pergunta_vazia():
    assert client.post("/assistente", json={"pergunta": "  "}).status_code == 400


def test_caos_injeta_falha_e_metricas_registram():
    client.post("/admin/caos", json={"latencia_ms": 0, "taxa_erro": 1.0})
    assert client.get("/recomendacoes/1").status_code == 503
    metricas = client.get("/metrics/").text
    assert 'ia_requisicoes_total{endpoint="/recomendacoes",status="503"}' in metricas
