package com.example.educacaocontinuadagamificada.recomendacao.domain;

import java.util.List;

/** degradado=true indica que a IA falhou e o Core respondeu com o catalogo padrao. */
public record ResultadoRecomendacao(Long alunoId, List<Recomendacao> itens, boolean degradado, String origem) {
}
