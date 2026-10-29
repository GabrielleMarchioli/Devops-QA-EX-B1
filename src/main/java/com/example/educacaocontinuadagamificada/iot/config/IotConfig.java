package com.example.educacaocontinuadagamificada.iot.config;

import com.example.educacaocontinuadagamificada.gamificacao.application.port.in.ConcluirCursoUseCase;
import com.example.educacaocontinuadagamificada.iot.adapter.out.memoria.EventoIotEmMemoria;
import com.example.educacaocontinuadagamificada.iot.application.ReceberEventoIotService;
import com.example.educacaocontinuadagamificada.iot.application.ReceberEventoIotUseCase;
import com.example.educacaocontinuadagamificada.iot.application.port.EventoIotRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class IotConfig {

    /** Registro do evento IoT e conclusao do curso na mesma transacao. */
    @Bean
    ReceberEventoIotUseCase receberEventoIotUseCase(EventoIotRepository eventos, ConcluirCursoUseCase concluirCurso,
                                                    PlatformTransactionManager transactionManager) {
        ReceberEventoIotService service = new ReceberEventoIotService(eventos, concluirCurso);
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        return evento -> tx.execute(status -> service.receber(evento));
    }

    @Bean
    @Profile("memoria")
    EventoIotRepository eventoIotEmMemoria() {
        return new EventoIotEmMemoria();
    }
}
