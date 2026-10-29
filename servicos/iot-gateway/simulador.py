"""Simulador de dispositivo de laboratorio (ESP32) para o mini-lab do Desafio 5.

Publica presenca, inicio e conclusao com QoS 1. Opcionalmente:
  --duplicar   reenvia a conclusao (simula retransmissao apos perda de PUBACK)
  --offline N  guarda N eventos em fila local enquanto "sem rede" e envia ao reconectar
"""
import argparse
import json
import time
import uuid

import paho.mqtt.client as mqtt


def evento(tipo: str, aluno: int, curso: str | None = None, nota: float | None = None) -> dict:
    e = {"id": str(uuid.uuid4()), "dev": "esp32-lab3", "t": tipo, "aluno": aluno, "ts": int(time.time())}
    if curso:
        e["curso"] = curso
    if nota is not None:
        e["nota"] = nota
    return e


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--host", default="localhost")
    p.add_argument("--porta", type=int, default=1883)
    p.add_argument("--lab", default="lab3")
    p.add_argument("--aluno", type=int, default=1)
    p.add_argument("--duplicar", action="store_true")
    p.add_argument("--offline", type=int, default=0)
    a = p.parse_args()

    topico = f"facens/labs/{a.lab}/eventos"
    fila = [evento("pres", a.aluno), evento("ini", a.aluno, "IOT-101"), evento("conc", a.aluno, "IOT-101", 9.0)]
    if a.duplicar:
        fila.append(dict(fila[-1]))
    fila.append({"dev": "esp32-lab3", "t": "conc"})  # payload malformado de proposito

    pendentes_offline = [evento("pres", a.aluno + i) for i in range(1, a.offline + 1)]

    c = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=f"sim-{a.lab}")
    c.connect(a.host, a.porta, keepalive=30)
    c.loop_start()
    for e in fila:
        info = c.publish(topico, json.dumps(e), qos=1)
        info.wait_for_publish()
        print(f"[simulador] publicado {e.get('t')} id={e.get('id', '-')} mid={info.mid}")
    if pendentes_offline:
        print(f"[simulador] sem rede: {len(pendentes_offline)} evento(s) guardados localmente")
        time.sleep(1)
        for e in pendentes_offline:
            c.publish(topico, json.dumps(e), qos=1).wait_for_publish()
        print(f"[simulador] rede voltou: {len(pendentes_offline)} evento(s) enviados")
    c.loop_stop()
    c.disconnect()


if __name__ == "__main__":
    main()
