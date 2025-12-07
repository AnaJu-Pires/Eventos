package com.ifsp.gamificacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GamificacaoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GamificacaoServiceApplication.class, args);
	}

}
