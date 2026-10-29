#!/usr/bin/env bash
# Versao completa do mini-lab (requer Maven Central e Docker). Execute na raiz do repositorio:
#   bash labs/desafio-03/executar-lab-spring.sh | tee resultado-spring.txt
set -u
docker compose up -d --build ia-service
./mvnw -q spring-boot:run -Dspring-boot.run.arguments=--ia.service.url=http://localhost:8000 > /tmp/core.log 2>&1 &
until curl -fs localhost:8080/actuator/health > /dev/null; do sleep 2; done
ID=$(curl -s -X POST localhost:8080/api/alunos -H 'Content-Type: application/json' -d '{"nome":"Gabrielle","plano":"PREMIUM"}' | sed 's/.*"id":\([0-9]*\).*/\1/')

echo "### ia-service no ar";        curl -s localhost:8080/api/alunos/$ID/recomendacoes; echo
docker compose stop ia-service
echo "### ia-service parado";       curl -s -w "  HTTP %{http_code}\n" localhost:8080/api/alunos/$ID/recomendacoes
echo "### fluxo essencial segue";   curl -s -w "  HTTP %{http_code}\n" -X POST localhost:8080/api/alunos/$ID/conclusoes -H 'Content-Type: application/json' -d '{"cursoId":"JAVA-101","media":8.5}'
echo "### assistente degradado";    curl -s -X POST localhost:8080/api/assistente -H 'Content-Type: application/json' -d '{"pergunta":"Como ganho moedas?"}'; echo
echo "### metrica de falha";        curl -s localhost:8080/actuator/prometheus | grep 'ia_requisicoes_seconds_count'
docker compose start ia-service
