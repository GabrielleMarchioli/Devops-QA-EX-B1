"""Mini-lab Desafio 1: compara dependencias entre pacotes antes (AC1) e depois (B1).

Uso: python analisar_dependencias.py <raiz-do-projeto> <profundidade>
  profundidade 1 = agrupa por primeiro pacote (camada na AC1, modulo no B1)
  profundidade 2 = agrupa por modulo.subpacote
Le os imports de src/main/java e imprime uma tabela Markdown.
"""
import re
import sys
from collections import defaultdict
from pathlib import Path

BASE = "com.example.educacaocontinuadagamificada"
FRAMEWORK = ("org.springframework", "jakarta.persistence", "io.micrometer")


def grupo(pacote: str, profundidade: int) -> str:
    resto = pacote[len(BASE):].strip(".")
    return ".".join(resto.split(".")[:profundidade]) or "(raiz)"


def analisar(raiz: Path, profundidade: int):
    deps = defaultdict(set)
    classes = defaultdict(int)
    com_framework = defaultdict(int)
    for arq in sorted((raiz / "src/main/java").rglob("*.java")):
        if arq.name == "package-info.java":
            continue
        texto = arq.read_text(encoding="utf-8")
        pacote = re.search(r"^package\s+([\w.]+);", texto, re.M).group(1)
        g = grupo(pacote, profundidade)
        classes[g] += 1
        imports = re.findall(r"^import\s+(?:static\s+)?([\w.]+);", texto, re.M)
        if any(i.startswith(FRAMEWORK) for i in imports):
            com_framework[g] += 1
        for imp in imports:
            if imp.startswith(BASE):
                destino = grupo(imp.rsplit(".", 1)[0], profundidade)
                if destino != g:
                    deps[g].add(destino)
    return classes, deps, com_framework


def main():
    raiz, profundidade = Path(sys.argv[1]), int(sys.argv[2])
    classes, deps, fw = analisar(raiz, profundidade)
    print("| Pacote | Classes | Classes com Spring/JPA | Depende de |")
    print("|---|---:|---:|---|")
    for g in sorted(classes):
        print(f"| `{g}` | {classes[g]} | {fw[g]} | {', '.join(f'`{d}`' for d in sorted(deps[g])) or '-'} |")
    total = sum(classes.values())
    arestas = sum(len(v) for v in deps.values())
    print(f"\nTotal: {total} classes, {len(classes)} pacotes, {arestas} dependencias entre pacotes.")


if __name__ == "__main__":
    main()
