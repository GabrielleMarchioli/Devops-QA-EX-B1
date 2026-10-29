#!/usr/bin/env bash
# Mini-lab Desafio 6: provocar condicao observavel e registrar no Prometheus.
# Com Docker: docker compose up -d ia-service prometheus grafana  (Grafana em :3000, dashboard "Evidências arquiteturais B1")
# Aqui: binario do Prometheus ($PROMETHEUS_BIN) + ia-service local.
set -u
cd "$(dirname "$0")"
RAIZ=../..
PROM=${PROMETHEUS_BIN:-prometheus}
CFG=$(mktemp -d)
cat > "$CFG/prometheus.yml" <<YML
global: { scrape_interval: 2s, evaluation_interval: 2s }
rule_files: [ alertas.yml ]
scrape_configs:
  - job_name: ia-service
    metrics_path: /metrics/
    static_configs: [ { targets: ["127.0.0.1:18000"] } ]
YML
cp $RAIZ/observabilidade/prometheus/alertas.yml "$CFG/"
q() { curl -s "localhost:9090/api/v1/query" --data-urlencode "query=$1" | python3 -c "import json,sys; r=json.load(sys.stdin)['data']['result']; print('  ', '; '.join(f\"{x['metric'].get('status', x['metric'].get('endpoint', 'valor'))}={float(x['value'][1]):.3f}\" for x in r) or 'sem dados')"; }
carga() { local fim=$((SECONDS+$1)); while [ $SECONDS -lt $fim ]; do for i in 1 2 3 4 5; do curl -s -o /dev/null "localhost:18000/recomendacoes/$i" & pids="${pids:-} $!"; done; wait $pids; pids=""; sleep 0.2; done; }

(cd $RAIZ/servicos/ia-service && uvicorn app.main:app --port 18000 --log-level warning) & IA=$!
$PROM --config.file="$CFG/prometheus.yml" --storage.tsdb.path="$CFG/data" --web.listen-address=:9090 > "$CFG/prom.log" 2>&1 & P=$!
sleep 4
echo "### Fase 1 (normal, 40 s): $(date +%T)"
carga 40
echo "  taxa por status (req/s):"; q 'sum by (status) (rate(ia_requisicoes_total[30s]))'
echo "  latencia p95 (s):";        q 'histogram_quantile(0.95, sum by (le) (rate(ia_latencia_segundos_bucket[30s])))'

echo; echo "### Fase 2 (falha provocada: 60% de 503 + 600 ms, 60 s): $(date +%T)"
curl -s -X POST localhost:18000/admin/caos -H 'Content-Type: application/json' -d '{"latencia_ms":600,"taxa_erro":0.6}'; echo
carga 60
echo "  taxa por status (req/s):"; q 'sum by (status) (rate(ia_requisicoes_total[30s]))'
echo "  latencia p95 (s):";        q 'histogram_quantile(0.95, sum by (le) (rate(ia_latencia_segundos_bucket[30s])))'
echo "  razao de erro:";           q 'sum(rate(ia_requisicoes_total{status=~"5.."}[1m])) / sum(rate(ia_requisicoes_total[1m]))'
echo "  alertas:"; curl -s localhost:9090/api/v1/alerts | python3 -c "import json,sys; [print('   ', a['labels']['alertname'], a['state'], a['activeAt']) for a in json.load(sys.stdin)['data']['alerts']]"
[ "${MANTER:-0}" = 1 ] && { echo "  (mantendo Prometheus e ia-service no ar: PIDs $IA $P)"; exit 0; }
kill $IA $P
