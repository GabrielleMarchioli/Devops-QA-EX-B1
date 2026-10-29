#!/usr/bin/env bash
# Mini-lab Desafio 4 (broker real). Com Docker: docker compose up -d rabbitmq notificacao-worker app
# Aqui: RabbitMQ local; o publicador reproduz o relay do outbox do Core.
set -u
cd "$(dirname "$0")"
WK=../../servicos/notificacao-worker
LOG=$(mktemp -d)
filas() { rabbitmqctl -q list_queues name messages_ready messages_unacknowledged | sed 's/^/  /'; }

echo "### 1) Consumidor de notificacao DESLIGADO; Core conclui 3 cursos e o relay publica"
python3 publicar_eventos.py normal | tee "$LOG/pub.txt"
sleep 1
echo "  filas (mensagens retidas, nada perdido):"; filas

echo
echo "### 2) Consumidor volta e drena a fila"
(cd $WK && PYTHONUNBUFFERED=1 python3 worker.py) > "$LOG/worker.log" 2>&1 & W=$!
sleep 3
grep -h "ENVIADA\|processado" "$LOG/worker.log" | cut -c25- | sed 's/^/  worker: /'
echo "  filas:"; filas

echo
echo "### 3) Reentrega (at-least-once) e mensagem invalida"
PRIMEIRO=$(head -1 "$LOG/pub.txt" | awk '{print $3}')
python3 publicar_eventos.py duplicado "$PRIMEIRO"
python3 publicar_eventos.py malformado
sleep 2
tail -n 3 "$LOG/worker.log" | cut -c25- | sed 's/^/  worker: /'
echo "  filas (malformada na DLQ):"; filas
kill $W
