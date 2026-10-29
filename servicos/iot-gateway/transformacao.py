"""Contrato do evento IoT (dispositivo -> gateway) e traducao para o formato do Core.

Payload do dispositivo (compacto, para hardware limitado):
  {"id": "...", "dev": "esp32-lab3", "t": "conc", "aluno": 1, "curso": "IOT-101", "nota": 9.0, "ts": 1761742800}
Topico: facens/labs/{laboratorio}/eventos
"""
from datetime import datetime, timezone

TIPOS = {"pres": "PRESENCA", "ini": "INICIO_ATIVIDADE", "conc": "CONCLUSAO_ATIVIDADE"}


class EventoInvalido(ValueError):
    pass


def laboratorio_do_topico(topico: str) -> str:
    partes = topico.split("/")
    if len(partes) != 4 or partes[0] != "facens" or partes[1] != "labs" or partes[3] != "eventos":
        raise EventoInvalido(f"tópico fora do padrão: {topico}")
    return partes[2].upper()


def transformar(topico: str, payload: dict) -> dict:
    for campo in ("id", "dev", "t", "aluno", "ts"):
        if campo not in payload:
            raise EventoInvalido(f"campo obrigatório ausente: {campo}")
    tipo = TIPOS.get(payload["t"])
    if tipo is None:
        raise EventoInvalido(f"tipo desconhecido: {payload['t']}")
    if tipo == "CONCLUSAO_ATIVIDADE" and ("curso" not in payload or "nota" not in payload):
        raise EventoInvalido("conclusão exige curso e nota")
    return {
        "eventoId": str(payload["id"]),
        "dispositivoId": str(payload["dev"]),
        "laboratorio": laboratorio_do_topico(topico),
        "tipo": tipo,
        "alunoId": int(payload["aluno"]),
        "cursoId": payload.get("curso"),
        "media": float(payload["nota"]) if "nota" in payload else None,
        "ocorridoEm": datetime.fromtimestamp(int(payload["ts"]), tz=timezone.utc).isoformat().replace("+00:00", "Z"),
    }


class Deduplicador:
    """Janela de ids ja encaminhados. A garantia final de idempotencia esta no Core (PK eventoId)."""

    def __init__(self, capacidade: int = 10_000):
        self.capacidade = capacidade
        self._ids: dict[str, None] = {}

    def ja_visto(self, evento_id: str) -> bool:
        return evento_id in self._ids

    def marcar(self, evento_id: str) -> None:
        self._ids[evento_id] = None
        if len(self._ids) > self.capacidade:
            self._ids.pop(next(iter(self._ids)))
