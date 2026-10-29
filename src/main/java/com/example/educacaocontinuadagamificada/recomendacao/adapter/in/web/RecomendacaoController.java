package com.example.educacaocontinuadagamificada.recomendacao.adapter.in.web;

import com.example.educacaocontinuadagamificada.recomendacao.application.RecomendacaoService;
import com.example.educacaocontinuadagamificada.recomendacao.domain.RespostaAssistente;
import com.example.educacaocontinuadagamificada.recomendacao.domain.ResultadoRecomendacao;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RecomendacaoController {

    public record PerguntaRequest(String pergunta) {
    }

    private final RecomendacaoService service;

    public RecomendacaoController(RecomendacaoService service) {
        this.service = service;
    }

    @GetMapping("/alunos/{alunoId}/recomendacoes")
    public ResultadoRecomendacao recomendacoes(@PathVariable Long alunoId,
                                               @RequestParam(defaultValue = "3") int limite) {
        return service.recomendar(alunoId, limite);
    }

    @PostMapping("/assistente")
    public RespostaAssistente assistente(@RequestBody PerguntaRequest request) {
        return service.perguntar(request.pergunta());
    }
}
