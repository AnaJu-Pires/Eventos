package com.ifsp.gamificacao.infrastructure.config;

import com.ifsp.gamificacao.core.ports.ScoreRepositoryPort;
import com.ifsp.gamificacao.core.usecases.AtualizarRanksUseCase;
import com.ifsp.gamificacao.core.usecases.ConsultarScoreUseCase;
import com.ifsp.gamificacao.core.usecases.ProcessarAcaoUseCase;
import com.ifsp.gamificacao.core.usecases.CriarScoreProfileUseCase;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GamificationConfig {

    @Bean
    public ProcessarAcaoUseCase processarAcaoUseCase(ScoreRepositoryPort scoreRepositoryPort) {
        return new ProcessarAcaoUseCase(scoreRepositoryPort);
    }

    @Bean
    public AtualizarRanksUseCase atualizarRanksUseCase(ScoreRepositoryPort repositoryPort) {
        return new AtualizarRanksUseCase(repositoryPort);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public ConsultarScoreUseCase consultarScoreUseCase(ScoreRepositoryPort repository) {
        return new ConsultarScoreUseCase(repository);
    }

    @Bean
    public CriarScoreProfileUseCase criarScoreProfileUseCase(ScoreRepositoryPort repository) {
        return new CriarScoreProfileUseCase(repository);
    }
}