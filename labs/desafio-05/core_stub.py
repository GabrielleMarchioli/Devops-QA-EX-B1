"""Stub do endpoint POST /api/iot/eventos do Core, usado apenas no mini-lab sem Java.
Reproduz o contrato do EventoIotController: 202 PROCESSADO, 200 DUPLICADO, 400 invalido.
"""
import json
import sys
from http.server import BaseHTTPRequestHandler, HTTPServer

recebidos: dict[str, dict] = {}


class Handler(BaseHTTPRequestHandler):
    def do_POST(self):
        evento = json.loads(self.rfile.read(int(self.headers["Content-Length"])))
        if evento["eventoId"] in recebidos:
            status, resultado = 200, "DUPLICADO"
        else:
            recebidos[evento["eventoId"]] = evento
            status, resultado = 202, "PROCESSADO"
        print(f"[core-stub] {status} {resultado} {json.dumps(evento, ensure_ascii=False)}", flush=True)
        corpo = json.dumps({"eventoId": evento["eventoId"], "resultado": resultado}).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(corpo)

    def log_message(self, *args):
        pass


if __name__ == "__main__":
    HTTPServer(("127.0.0.1", int(sys.argv[1]) if len(sys.argv) > 1 else 8080), Handler).serve_forever()
