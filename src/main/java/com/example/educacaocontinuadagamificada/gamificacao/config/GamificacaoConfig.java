package com.example.educacaocontinuadagamificada.gamificacao.config;

import com.example.educacaocontinuadagamificada.gamificacao.adapter.out.memoria.AlunoGamificacaoEmMemoria;
import com.example.educacaocontinuadagamificada.gamificacao.adapter.out.memoria.ConclusaoCursoEmMemoria;
import com.example.educacaocontinuadagamificada.gamificacao.application.ConcluirCursoService;
import com.example.educacaocontinuadagamificada.gamificacao.application.ConsultarRankingService;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConcluirCursoUseCase;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConsultarRankingUseCase;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.out.AlunoGamificacaoRepository;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.out.ConclusaoCursoRepository;
import com.example.educacaocontinuadagamificada.gamificacao.application.port.out.EventoPublisher;
import com.example.educacaocontinuadagamificada.gamificacao.domain.Aluno;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

/**
 * Composicao do modulo: o unico lugar onde Spring conhece os casos de uso.
 * A transacao fica aqui (decorator), mantendo application livre de @Transactional.
 */
@Configuration
public class GamificacaoConfig {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    ConcluirCursoUseCase concluirCursoUseCase(AlunoGamificacaoRepository alunos, ConclusaoCursoRepository conclusoes,
                                              EventoPublisher eventos, Clock relogio,
                                              PlatformTransactionManager transactionManager) {
        ConcluirCursoService service = new ConcluirCursoService(alunos, conclusoes, eventos, relogio);
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        return comando -> tx.execute(status -> service.executar(comando));
    }

    @Bean
    ConsultarRankingUseCase consultarRankingUseCase(AlunoGamificacaoRepository alunos) {
        return new ConsultarRankingService(alunos);
    }

    /** Perfil "memoria": troca de adapter sem banco (Desafio 2). */
    @Configuration
    @Profile("memoria")
    static class AdaptersEmMemoria {

        @Bean
        AlunoGamificacaoRepository alunoGamificacaoEmMemoria() {
            AlunoGamificacaoEmMemoria repo = new AlunoGamificacaoEmMemoria();
            repo.salvar(new Aluno("Gabrielle"));
            repo.salvar(new Aluno("Afonso"));
            repo.salvar(new Aluno("Giulianno"));
            return repo;
        }

        @Bean
        ConclusaoCursoRepository conclusaoCursoEmMemoria() {
            return new ConclusaoCursoEmMemoria();
        }
    }
}
