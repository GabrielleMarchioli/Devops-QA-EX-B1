/**
 * Modulo Recomendacao: fronteira do Core com o servico Python de IA (ADR-003).
 * O Core nao contem modelo de ML nem RAG; apenas consome o ia-service via HTTP
 * e define o comportamento degradado quando a IA esta indisponivel.
 */
package com.example.educacaocontinuadagamificada.recomendacao;
