package com.example.educacaocontinuadagamificada.recomendacao.application.port;

import com.example.educacaocontinuadagamificada.recomendacao.domain.RespostaAssistente;

/** RF04: porta de saida para o assistente educacional (RAG/LLM). */
public interface AssistentePort {

    RespostaAssistente perguntar(String pergunta);
}
