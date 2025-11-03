package br.ifsp.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Classe principal da nossa aplicação Spring Boot.
 * 
 * A anotação @SpringBootApplication habilita as configurações
 * automáticas do Spring (auto-configuration) e também indica 
 * que esta é a classe que deve ser executada para iniciar 
 * a aplicação.
 */
@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        // Método main: ponto de entrada de uma aplicação Java.
        // SpringApplication.run() inicia a aplicação Spring Boot.
        SpringApplication.run(Application.class, args);
    }

}

