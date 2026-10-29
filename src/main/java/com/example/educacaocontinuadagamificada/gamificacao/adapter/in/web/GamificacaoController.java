package com.example.educacaocontinuadagamificada.gamificacao.adapter.in.web;

import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConcluirCursoComando;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConcluirCursoUseCase;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConclusaoCursoResultado;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConsultarRankingUseCase;
import com.example.educacaocontinuadagamificada.gamificacao.domain.PosicaoRanking;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GamificacaoController {

    public record ConclusaoRequest(String cursoId, double media) {
    }

    private final ConcluirCursoUseCase concluirCurso;
    private final ConsultarRankingUseCase consultarRanking;
    private final Counter cursosConcluidos;

    public GamificacaoController(ConcluirCursoUseCase concluirCurso, ConsultarRankingUseCase consultarRanking,
                                 MeterRegistry registry) {
        this.concluirCurso = concluirCurso;
        this.consultarRanking = consultarRanking;
        this.cursosConcluidos = Counter.builder("gamificacao.cursos.concluidos")
                .description("Conclusoes de curso registradas (sem duplicadas)")
                .register(registry);
    }

    /** Fluxo sincrono (ADR-004): resposta imediata com as recompensas calculadas. */
    @PostMapping("/alunos/{alunoId}/conclusoes")
    public ResponseEntity<ConclusaoCursoResultado> concluir(@PathVariable Long alunoId,
                                                            @RequestBody ConclusaoRequest request) {
        ConclusaoCursoResultado resultado = concluirCurso.executar(
                new ConcluirCursoComando(alunoId, request.cursoId(), request.media()));
        if (resultado.jaRegistrada()) {
            return ResponseEntity.ok(resultado);
        }
        cursosConcluidos.increment();
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    @GetMapping("/ranking")
    public List<PosicaoRanking> ranking(@RequestParam(defaultValue = "10") int limite) {
        return consultarRanking.topN(limite);
    }
}
