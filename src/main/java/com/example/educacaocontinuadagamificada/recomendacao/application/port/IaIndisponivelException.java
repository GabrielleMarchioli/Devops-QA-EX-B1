package com.example.educacaocontinuadagamificada.recomendacao.application.port;

/** Lancada pelo adapter quando o ia-service nao responde, excede o timeout ou retorna 5xx. */
public class IaIndisponivelException extends RuntimeException {

    public IaIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
