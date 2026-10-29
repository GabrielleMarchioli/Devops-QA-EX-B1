"""ia-service: fronteira de IA da plataforma (ADR-003).

Recomendacao (ML) e assistente (RAG) ficam no mesmo servico, em modulos separados,
porque hoje compartilham ciclo de deploy e equipe. Criterio de separacao no ADR-003.
"""
import asyncio
import os
import random
import time

from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import JSONResponse
from prometheus_client import Counter, Histogram, Info, make_asgi_app
from pydantic import BaseModel

from app import rag, recomendacao

app = FastAPI(title="ia-service", version="1.0.0")
app.mount("/metrics", make_asgi_app())

REQUISICOES = Counter("ia_requisicoes_total", "Requisicoes recebidas pelo ia-service", ["endpoint", "status"])
LATENCIA = Histogram("ia_latencia_segundos", "Latencia por endpoint", ["endpoint"],
                     buckets=(0.01, 0.05, 0.1, 0.25, 0.5, 1, 1.5, 2, 5))
MODELO = Info("ia_modelo", "Versao do modelo em execucao")
MODELO.info({"recomendacao": recomendacao.MODELO_VERSAO, "rag": "tfidf-extrativo-v1"})

# Injecao de falha controlada para os labs (Desafios 3 e 6). Padrao: desligada.
CAOS = {
    "latencia_ms": int(os.getenv("IA_LATENCIA_MS", "0")),
    "taxa_erro": float(os.getenv("IA_TAXA_ERRO", "0")),
}


class Pergunta(BaseModel):
    pergunta: str


class Caos(BaseModel):
    latencia_ms: int = 0
    taxa_erro: float = 0.0


@app.middleware("http")
async def observar(request: Request, call_next):
    caminho = request.url.path
    if caminho.startswith("/metrics") or caminho.startswith("/admin") or caminho == "/health":
        return await call_next(request)
    endpoint = "/recomendacoes" if caminho.startswith("/recomendacoes") else caminho
    inicio = time.perf_counter()
    if CAOS["latencia_ms"]:
        await asyncio.sleep(CAOS["latencia_ms"] / 1000)
    if CAOS["taxa_erro"] and random.random() < CAOS["taxa_erro"]:
        resposta = JSONResponse({"erro": "falha injetada (lab)"}, status_code=503)
    else:
        resposta = await call_next(request)
    LATENCIA.labels(endpoint).observe(time.perf_counter() - inicio)
    REQUISICOES.labels(endpoint, str(resposta.status_code)).inc()
    return resposta


@app.get("/health")
def health():
    return {"status": "UP", "modelo": recomendacao.MODELO_VERSAO}


@app.get("/recomendacoes/{aluno_id}")
def recomendacoes(aluno_id: int, limite: int = 3):
    if aluno_id <= 0:
        raise HTTPException(status_code=400, detail="aluno_id inválido")
    return {"alunoId": aluno_id, "modelo": recomendacao.MODELO_VERSAO,
            "itens": recomendacao.recomendar(aluno_id, limite)}


@app.post("/assistente")
def assistente(body: Pergunta):
    if not body.pergunta.strip():
        raise HTTPException(status_code=400, detail="pergunta vazia")
    return rag.perguntar(body.pergunta)


@app.post("/admin/caos")
def configurar_caos(body: Caos):
    CAOS["latencia_ms"] = max(0, body.latencia_ms)
    CAOS["taxa_erro"] = min(1.0, max(0.0, body.taxa_erro))
    return CAOS
