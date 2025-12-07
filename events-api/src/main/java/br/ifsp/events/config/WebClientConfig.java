package br.ifsp.events.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${gamification.service.url:http://gamificacao-app:8085}")
    private String gamificationUrl;

    @Bean
    public WebClient gamificationWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(gamificationUrl)
                .build();
    }
}