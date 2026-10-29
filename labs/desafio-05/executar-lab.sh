#!/usr/bin/env bash
# Mini-lab Desafio 5. Com Docker: docker compose up -d mosquitto app iot-gateway
# e rode apenas o simulador. Sem Docker (como registrado aqui): mosquitto local + stub do Core.
set -u
cd "$(dirname "$0")"
GW=../../servicos/iot-gateway
LOG=$(mktemp -d)
export PYTHONUNBUFFERED=1 MQTT_HOST=127.0.0.1 MQTT_PORT=1883 CORE_URL=http://127.0.0.1:18080 METRICS_PORT=19102

printf 'listener 1883 127.0.0.1\nallow_anonymous true\n' > "$LOG/mosquitto.conf"
mosquitto -c "$LOG/mosquitto.conf" > "$LOG/broker.log" 2>&1 & BROKER=$!
sleep 2

echo "### Cenario 1: tudo no ar (evento normal, duplicado e malformado)"
python3 core_stub.py 18080 > "$LOG/core1.log" 2>&1 & CORE=$!
(cd $GW && python3 gateway.py) > "$LOG/gw1.log" 2>&1 & GWPID=$!
sleep 2
(cd $GW && python3 simulador.py --duplicar)
sleep 2
echo "  metricas: $(curl -s localhost:19102/metrics | grep '^iot_gateway_eventos_total' | tr '\n' ' ')"
kill $GWPID; sleep 1
grep -h "encaminhado\|descartado\|duplicado" "$LOG/gw1.log" | sed 's/^/  gateway: /'
sed 's/^/  /' "$LOG/core1.log"

echo
echo "### Cenario 2: gateway fora do ar (conectividade intermitente) -> broker guarda QoS 1"
(cd $GW && python3 simulador.py --offline 2 --aluno 10)
echo "  (gateway desligado durante a publicacao; religando...)"
(cd $GW && python3 gateway.py) > "$LOG/gw2.log" 2>&1 & GWPID=$!
sleep 4
kill $GWPID; sleep 1
grep -h "encaminhado\|descartado\|duplicado" "$LOG/gw2.log" | sed 's/^/  gateway: /'

echo
echo "### Cenario 3: Core fora do ar -> gateway nao confirma (sem PUBACK) e evento e reentregue"
kill $CORE; sleep 1
(cd $GW && python3 gateway.py) > "$LOG/gw3.log" 2>&1 & GWPID=$!
sleep 2
(cd $GW && python3 simulador.py --aluno 20) > /dev/null
sleep 3
kill $GWPID; sleep 1
grep -h "indisponível" "$LOG/gw3.log" | head -3 | cut -c1-150 | sed 's/^/  gateway: /'
echo "  (Core religado; gateway reconecta com a mesma sessao)"
python3 core_stub.py 18080 > "$LOG/core3.log" 2>&1 & CORE=$!
sleep 1
(cd $GW && python3 gateway.py) > "$LOG/gw4.log" 2>&1 & GWPID=$!
sleep 4
grep -h "encaminhado\|descartado\|duplicado" "$LOG/gw4.log" | sed 's/^/  gateway: /'
sed 's/^/  /' "$LOG/core3.log"

kill $GWPID $CORE $BROKER 2>/dev/null
wait 2>/dev/null
