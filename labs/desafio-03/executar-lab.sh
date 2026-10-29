#!/usr/bin/env bash
# Mini-lab Desafio 3: Core (RecomendacaoService real) consumindo o ia-service FastAPI real.
# Versao completa com Spring Boot: ver executar-lab-spring.sh
set -u
cd "$(dirname "$0")"
RAIZ=../..
OUT=$(mktemp -d)
M=$RAIZ/src/main/java/com/example/educacaocontinuadagamificada/recomendacao
javac -d "$OUT" $(find $M/domain $M/application -name '*.java' ! -name package-info.java) LabIaIndisponivel.java 2>&1 | grep -v JAVA_TOOL
lab() { java -cp "$OUT" LabIaIndisponivel http://127.0.0.1:18000 "$1" 2>&1 | grep -v JAVA_TOOL; }

(cd $RAIZ/servicos/ia-service && uvicorn app.main:app --port 18000 --log-level warning) & IA=$!
sleep 3
echo "### 1) ia-service no ar"
curl -s http://127.0.0.1:18000/recomendacoes/2?limite=3 | cut -c1-160; echo
lab 1; lab 2

echo; echo "### 2) ia-service INTERROMPIDO (kill do processo Python)"
kill $IA; sleep 1
curl -s -o /dev/null -w "  curl direto: HTTP %{http_code} (conexao recusada)\n" http://127.0.0.1:18000/health
lab 1; lab 2

echo; echo "### 3) ia-service lento (3 s, acima do timeout de 1,5 s do Core)"
(cd $RAIZ/servicos/ia-service && IA_LATENCIA_MS=3000 uvicorn app.main:app --port 18000 --log-level warning) & IA=$!
sleep 3
lab 1

echo; echo "### 4) ia-service com 100% de erro 5xx"
curl -s -X POST localhost:18000/admin/caos -H 'Content-Type: application/json' -d '{"latencia_ms":0,"taxa_erro":1.0}' > /dev/null
lab 1
kill $IA
