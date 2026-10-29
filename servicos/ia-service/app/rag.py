"""RF04 - Assistente educacional (RAG).

Recuperacao: TF-IDF + cosseno sobre os trechos da base de conhecimento (sem dependencias pesadas).
Geracao: no laboratorio a resposta e extrativa (trecho mais relevante). O ponto de extensao
para um LLM e a funcao gerar_resposta(): trocar por uma chamada ao provedor nao altera o contrato.
"""
import math
import re
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent / "base_conhecimento"
_TOKEN = re.compile(r"[a-zà-ú0-9]+", re.IGNORECASE)
STOPWORDS = {"a", "o", "e", "de", "do", "da", "em", "um", "uma", "para", "com", "que", "os", "as", "no", "na",
             "por", "se", "como", "eu", "meu", "minha", "ao", "dos", "das", "mais", "ou", "sao", "é", "são"}


def _tokens(texto: str) -> list[str]:
    return [t for t in (m.lower() for m in _TOKEN.findall(texto)) if t not in STOPWORDS and len(t) > 1]


class Indice:
    def __init__(self, base_dir: Path = BASE_DIR):
        self.trechos: list[tuple[str, str]] = []  # (fonte, texto)
        for arquivo in sorted(base_dir.glob("*.md")):
            for bloco in arquivo.read_text(encoding="utf-8").split("\n\n"):
                bloco = bloco.strip()
                if bloco and not bloco.startswith("#"):
                    self.trechos.append((arquivo.stem, bloco))
        docs = [_tokens(t) for _, t in self.trechos]
        n = len(docs)
        df: dict[str, int] = {}
        for d in docs:
            for termo in set(d):
                df[termo] = df.get(termo, 0) + 1
        self.idf = {t: math.log((1 + n) / (1 + c)) + 1 for t, c in df.items()}
        self.vetores = [self._vetor(d) for d in docs]

    def _vetor(self, tokens: list[str]) -> dict[str, float]:
        tf: dict[str, float] = {}
        for t in tokens:
            tf[t] = tf.get(t, 0) + 1
        vet = {t: f * self.idf.get(t, 0.0) for t, f in tf.items()}
        norma = math.sqrt(sum(v * v for v in vet.values())) or 1.0
        return {t: v / norma for t, v in vet.items()}

    def buscar(self, pergunta: str, k: int = 2) -> list[tuple[float, str, str]]:
        q = self._vetor(_tokens(pergunta))
        pontuados = []
        for (fonte, texto), vet in zip(self.trechos, self.vetores):
            score = sum(q.get(t, 0.0) * v for t, v in vet.items())
            if score > 0:
                pontuados.append((round(score, 3), fonte, texto))
        pontuados.sort(key=lambda x: x[0], reverse=True)
        return pontuados[:k]


def gerar_resposta(pergunta: str, contexto: list[tuple[float, str, str]]) -> str:
    if not contexto:
        return "Não encontrei essa informação na base de conhecimento da plataforma."
    return contexto[0][2]


_indice: Indice | None = None


def perguntar(pergunta: str) -> dict:
    global _indice
    if _indice is None:
        _indice = Indice()
    contexto = _indice.buscar(pergunta)
    return {"resposta": gerar_resposta(pergunta, contexto), "fontes": sorted({c[1] for c in contexto})}
