"""RF03 - Recomendacao inteligente (ML).

Modelo baseline do laboratorio: popularidade do curso + afinidade com a trilha do aluno.
O contrato HTTP (GET /recomendacoes/{aluno_id}) e estavel: o modelo pode ser trocado
(ex.: filtragem colaborativa treinada) sem nenhuma mudanca no Core Java.
"""

MODELO_VERSAO = "popularidade-afinidade-v1"

CATALOGO = [
    {"cursoId": "JAVA-101", "titulo": "Fundamentos de Java", "trilha": "backend", "popularidade": 0.95},
    {"cursoId": "SPRING-201", "titulo": "APIs com Spring Boot", "trilha": "backend", "popularidade": 0.88},
    {"cursoId": "PY-201", "titulo": "Python para dados", "trilha": "dados", "popularidade": 0.90},
    {"cursoId": "SQL-101", "titulo": "SQL para iniciantes", "trilha": "dados", "popularidade": 0.85},
    {"cursoId": "ML-301", "titulo": "Machine Learning aplicado", "trilha": "dados", "popularidade": 0.72},
    {"cursoId": "GIT-101", "titulo": "Git e versionamento", "trilha": "devops", "popularidade": 0.93},
    {"cursoId": "DOCKER-201", "titulo": "Containers com Docker", "trilha": "devops", "popularidade": 0.80},
    {"cursoId": "IOT-101", "titulo": "IoT com MQTT", "trilha": "iot", "popularidade": 0.65},
]

TRILHAS = ["backend", "dados", "devops", "iot"]


def trilha_preferida(aluno_id: int) -> str:
    # No lab a preferencia e derivada do id; em producao viria do historico de eventos (analytics).
    return TRILHAS[aluno_id % len(TRILHAS)]


def recomendar(aluno_id: int, limite: int = 3) -> list[dict]:
    trilha = trilha_preferida(aluno_id)
    pontuados = []
    for curso in CATALOGO:
        afinidade = 1.0 if curso["trilha"] == trilha else 0.0
        score = round(0.6 * curso["popularidade"] + 0.4 * afinidade, 3)
        motivo = f"trilha {trilha}" if afinidade else "popular na plataforma"
        pontuados.append({"cursoId": curso["cursoId"], "titulo": curso["titulo"], "score": score, "motivo": motivo})
    pontuados.sort(key=lambda c: c["score"], reverse=True)
    return pontuados[: max(1, min(limite, 10))]
