package com.example.educacaocontinuadagamificada.eventos.adapter;

import com.example.educacaocontinuadagamificada.eventos.application.OutboxArmazenamento;
import com.example.educacaocontinuadagamificada.eventos.application.ProcessadorOutbox;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Executa o relay periodicamente e expoe metricas:
 * outbox_eventos_pendentes (gauge) e outbox_entregas_total{resultado}.
 */
@Component
public class RelayOutboxAgendado {

    private static final Logger log = LoggerFactory.getLogger(RelayOutboxAgendado.class);

    private final ProcessadorOutbox processador;
    private final Counter enviados;
    private final Counter falhas;

    public RelayOutboxAgendado(ProcessadorOutbox processador, OutboxArmazenamento outbox, MeterRegistry registry) {
        this.processador = processador;
        Gauge.builder("outbox.eventos.pendentes", outbox, OutboxArmazenamento::contarPendentes)
                .description("Eventos aguardando entrega ao broker")
                .register(registry);
        this.enviados = Counter.builder("outbox.entregas").tag("resultado", "sucesso").register(registry);
        this.falhas = Counter.builder("outbox.entregas").tag("resultado", "falha").register(registry);
    }

    @Scheduled(fixedDelayString = "${outbox.relay.intervalo-ms:5000}")
    public void executar() {
        ProcessadorOutbox.Resultado r = processador.processar(50);
        enviados.increment(r.enviados());
        falhas.increment(r.falhas());
        if (r.falhas() > 0) {
            log.warn("[outbox] {} evento(s) nao entregues, permanecem PENDENTE para nova tentativa", r.falhas());
        }
    }
}
