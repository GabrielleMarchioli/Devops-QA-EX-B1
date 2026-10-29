package com.example.educacaocontinuadagamificada.recomendacao.domain;

import java.util.List;

public record RespostaAssistente(String resposta, List<String> fontes, boolean degradado) {
}
