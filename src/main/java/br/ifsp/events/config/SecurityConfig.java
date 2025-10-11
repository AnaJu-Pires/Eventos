package br.ifsp.events.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // --- LINHA MAIS IMPORTANTE PARA RESOLVER O ERRO 403 ---
            // Desabilita a proteção CSRF, que não é necessária para nossa API REST
            .csrf(csrf -> csrf.disable())
            
            // Define as regras de autorização para as requisições
            .authorizeHttpRequests(authorize -> authorize
                // Permite acesso público a todos os endpoints que começam com /cadastro
                .requestMatchers("/cadastro/**").permitAll()
                
                // Exige autenticação para qualquer outra requisição
                .anyRequest().authenticated()
            );

        return http.build();
    }
}