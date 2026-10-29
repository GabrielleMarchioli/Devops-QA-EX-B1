package com.example.educacaocontinuadagamificada.recomendacao.config;

import com.example.educacaocontinuadagamificada.recomendacao.adapter.out.ia.IaHttpAdapter;
import com.example.educacaocontinuadagamificada.recomendacao.application.RecomendacaoService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RecomendacaoConfig {

    @Bean
    IaHttpAdapter iaHttpAdapter(@Value("${ia.service.url:http://localhost:8000}") String url,
                                @Value("${ia.service.timeout-ms:1500}") long timeoutMs,
                                MeterRegistry registry) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofMillis(timeoutMs));
        fabrica.setReadTimeout(Duration.ofMillis(timeoutMs));
        RestClient http = RestClient.builder()
                .baseUrl(url)
                .requestFactory(fabrica)
                .build();
        return new IaHttpAdapter(http, registry);
    }

    @Bean
    RecomendacaoService recomendacaoService(IaHttpAdapter ia) {
        return new RecomendacaoService(ia, ia);
    }
}
